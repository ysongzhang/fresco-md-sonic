package dk.alexandra.fresco.suite.mdsonic.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.BooleanSerializer;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;
import dk.alexandra.fresco.suite.mdsonic.util.BoolUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Native protocol for computing logical AND of two values in boolean form.
 */
public class MdsonicAndProtocol<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>> extends
        MdsonicNativeProtocol<SBool, PlainT, SecretP> {

  private final boolean useMaskedEvaluation;

  // Input:
  private final DRes<SBool> left;
  private final DRes<SBool> right;

  // ASS:
  private MdsonicBitTriple<SecretP, MdsonicASBoolBoolean<SecretP>> triple;
  private MdsonicASBoolBoolean<SecretP> epsilon;
  private MdsonicASBoolBoolean<SecretP> delta;

  // MSS:
  private MdsonicASBoolBoolean<SecretP> innerProduct;

  // ASS to MSS:
  private MdsonicASBoolBoolean<SecretP> maskedSecret;
  private MdsonicASBoolBoolean<SecretP> openedSecret;
  private boolean opened;

  // Output:
  private SBool product;

  /**
   * Creates new {@link MdsonicAndProtocol}.
   *
   * @param left left factor
   * @param right right factor
   */
  public MdsonicAndProtocol(DRes<SBool> left, DRes<SBool> right) {
    this.left = left;
    this.right = right;
    this.useMaskedEvaluation = false;
  }

  public MdsonicAndProtocol(DRes<SBool> left, DRes<SBool> right, boolean useMaskedEvaluation) {
    this.left = left;
    this.right = right;
    this.useMaskedEvaluation = useMaskedEvaluation;
    this.maskedSecret = null;
  }

  public MdsonicAndProtocol(DRes<SBool> left, DRes<SBool> right, boolean useMaskedEvaluation, DRes<SBool> maskedSecret) {
    this.left = left;
    this.right = right;
    this.useMaskedEvaluation = useMaskedEvaluation;
    this.maskedSecret = (MdsonicASBoolBoolean<SecretP>) maskedSecret.out();
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    SecretP macKeyShareBoolean = resourcePool.getDataSupplier().getSecretSharedKeyBoolean();
    MdsonicGFFactory<SecretP> factoryBoolean = resourcePool.getBooleanFactory();
    if (useMaskedEvaluation) {
      if (round == 0) {
        innerProduct = resourcePool.getDataSupplier().getNextBitTripleProductShare();
        MdsonicMSBoolBoolean<SecretP> leftBit = (MdsonicMSBoolBoolean<SecretP>) left.out();
        MdsonicMSBoolBoolean<SecretP> rightBit = (MdsonicMSBoolBoolean<SecretP>) right.out();
        boolean crossOpen = (leftBit.getOpened() & rightBit.getOpened());
        MdsonicASBoolBoolean<SecretP> input = innerProduct.xorOpen(crossOpen, macKeyShareBoolean, factoryBoolean.zero(), resourcePool.getMyId() == 1)
                .xor(leftBit.getMaskedSecret().and(rightBit.getOpened()))
                .xor(rightBit.getMaskedSecret().and(leftBit.getOpened()));
        // ASS to MSS:
        if (maskedSecret == null) {
          maskedSecret = resourcePool.getDataSupplier().getNextBitShare();
        }
        openedSecret = input.xor(maskedSecret);
        network.sendToAll(new byte[]{BooleanSerializer.toBytes(openedSecret.getShare())});
        return EvaluationStatus.HAS_MORE_ROUNDS;
      } else {
        List<byte[]> buffers = network.receiveFromAll();
        List<Boolean> shares = new ArrayList<>();
        for (byte[] buffer : buffers) {
          shares.add(BooleanSerializer.fromBytes(buffer[0]));
        }
        opened = BoolUtils.xor(shares);

        OpenedValueStore<MdsonicASBoolBoolean<SecretP>, Boolean> openedBooleanValueStore = resourcePool.getOpenedBooleanValueStore();
        openedBooleanValueStore.pushOpenedValue(openedSecret, opened);

        this.product = new MdsonicMSBoolBoolean<>(maskedSecret, opened);
        return EvaluationStatus.IS_DONE;
      }
    } else {
      if (round == 0) {
        triple = resourcePool.getDataSupplier().getNextBitTripleShares();
        MdsonicASBoolBoolean<SecretP> leftBit = (MdsonicASBoolBoolean<SecretP>) left.out();
        MdsonicASBoolBoolean<SecretP> rightBit = (MdsonicASBoolBoolean<SecretP>) right.out();
        epsilon = leftBit.xor(triple.getLeft());
        delta = rightBit.xor(triple.getRight());
        byte epsilonByte = BooleanSerializer.toBytes(epsilon.getShare());
        byte deltaByte = BooleanSerializer.toBytes(delta.getShare());
        int packed = epsilonByte ^ (deltaByte << 1);  // first d and then e, only 2 bits
        final byte[] bytes = new byte[]{(byte) packed};
        network.sendToAll(bytes);
        return EvaluationStatus.HAS_MORE_ROUNDS;
      } else {
        Pair<Boolean, Boolean> epsilonAndDelta = receiveAndReconstruct(network, resourcePool.getNoOfParties());
        boolean e = epsilonAndDelta.getFirst();
        boolean d = epsilonAndDelta.getSecond();

        // push openValue channel
        OpenedValueStore<MdsonicASBoolBoolean<SecretP>, Boolean> openedBooleanValueStore = resourcePool.getOpenedBooleanValueStore();
        openedBooleanValueStore.pushOpenedValue(epsilon, e);
        openedBooleanValueStore.pushOpenedValue(delta, d);

        boolean ed = (e & d);
        // compute [prod] = [c] XOR epsilon AND [b] XOR delta AND [a] XOR epsilon AND delta
        MdsonicASBoolBoolean<SecretP> tripleLeft = triple.getLeft();
        MdsonicASBoolBoolean<SecretP> tripleRight = triple.getRight();
        MdsonicASBoolBoolean<SecretP> tripleProduct = triple.getProduct();
        this.product = tripleProduct
                .xor(tripleRight.and(e))
                .xor(tripleLeft.and(d))
                .xorOpen(ed, macKeyShareBoolean, factoryBoolean.zero(), resourcePool.getMyId() == 1);
        return EvaluationStatus.IS_DONE;
      }
    }

  }

  /**
   * Retrieves shares for epsilon and delta and reconstructs each.
   */
  private Pair<Boolean, Boolean> receiveAndReconstruct(Network network, int noOfParties) {
    int received = network.receive(1)[0];
    boolean e = BooleanSerializer.fromBytes((byte) (received & 1));
    boolean d = BooleanSerializer.fromBytes((byte) ((received & 2) >>> 1));
    for (int i = 2; i <= noOfParties; i++) {
      received = network.receive(i)[0];
      e ^= BooleanSerializer.fromBytes((byte) (received & 1));
      d ^= BooleanSerializer.fromBytes((byte) ((received & 2) >>> 1));
    }
    return new Pair<>(e, d);
  }

  @Override
  public SBool out() {
    return product;
  }
}
