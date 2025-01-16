package dk.alexandra.fresco.suite.mdsonic.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;

import java.util.Arrays;
import java.util.List;

/**
 * Native protocol for computing product of two secret numbers.
 */
public class MdsonicMultiplyProtocol<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>> extends
        MdsonicNativeProtocol<SInt, PlainT, SecretP> {

  private final boolean useMaskedEvaluation;

  // input:
  private final DRes<SInt> left;
  private final DRes<SInt> right;

  // ASS:
  private MdsonicTriple<PlainT, MdsonicASIntArithmetic<PlainT>> triple;
  private MdsonicASIntArithmetic<PlainT> epsilon;
  private MdsonicASIntArithmetic<PlainT> delta;

  // MSS:
  private MdsonicASIntArithmetic<PlainT> innerProduct;

  // ASSToMSS:
  private MdsonicASIntArithmetic<PlainT> maskedSecret;
  private MdsonicASIntArithmetic<PlainT> openedSecret;
  private PlainT opened;

  // Output:
  private SInt product;

  /**
   * Creates new {@link MdsonicMultiplyProtocol}.
   *
   * @param left left factor
   * @param right right factor
   */
  public MdsonicMultiplyProtocol(DRes<SInt> left, DRes<SInt> right) {
    this.left = left;
    this.right = right;
    this.useMaskedEvaluation = false;
  }

  public MdsonicMultiplyProtocol(DRes<SInt> left, DRes<SInt> right, boolean useMaskedEvaluation) {
    this.left = left;
    this.right = right;
    this.useMaskedEvaluation = useMaskedEvaluation;
    this.maskedSecret = null;
  }

  public MdsonicMultiplyProtocol(DRes<SInt> left, DRes<SInt> right, boolean useMaskedEvaluation, DRes<SInt> maskedSecret) {
    this.left = left;
    this.right = right;
    this.useMaskedEvaluation = useMaskedEvaluation;
    this.maskedSecret = (MdsonicASIntArithmetic<PlainT>) maskedSecret.out();
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    final PlainT macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
    MdsonicCompUIntFactory<PlainT> factory = resourcePool.getFactory();
    OpenedValueStore<MdsonicASIntArithmetic<PlainT>, PlainT> openedValueStore = resourcePool.getOpenedValueStore();

    if (useMaskedEvaluation) {  //
      if (round == 0) {
        innerProduct = resourcePool.getDataSupplier().getNextTripleProductShare();
        MdsonicMSIntArithmetic<PlainT> outLeft = factory.toMdsonicMSIntArithmetic(left);
        MdsonicMSIntArithmetic<PlainT> outRight = factory.toMdsonicMSIntArithmetic(right);
        PlainT crossOpen = outLeft.getOpened().multiply(outRight.getOpened());
        MdsonicASIntArithmetic<PlainT> input = innerProduct.addConstant(crossOpen, macKeyShare, factory.zero(), resourcePool.getMyId() == 1)
                .add(outLeft.getMaskedSecret().multiply(outRight.getOpened()))
                .add(outRight.getMaskedSecret().multiply(outLeft.getOpened()));
        // ASS to MSS:
        if (maskedSecret == null) {
          maskedSecret = resourcePool.getDataSupplier().getNextRandomElementShare();
        }
        openedSecret = input.subtract(maskedSecret);
        network.sendToAll(openedSecret.serializeShare());
        return EvaluationStatus.HAS_MORE_ROUNDS;
      } else {
        ByteSerializer<PlainT> serializer = resourcePool.getPlainSerializer();
        List<PlainT> shares = serializer.deserializeList(network.receiveFromAll());
        opened = MdsonicUInt.sum(shares);
        openedValueStore.pushOpenedValue(openedSecret, opened);
        this.product = new MdsonicMSIntArithmetic<>(maskedSecret, opened);
        return EvaluationStatus.IS_DONE;
      }
    } else {
      ByteSerializer<PlainT> serializer = resourcePool.getPlainSerializer();
      if (round == 0) {
        triple = resourcePool.getDataSupplier().getNextTripleSharesFull();
        epsilon = factory.toMdsonicASIntArithmetic(left).subtract(triple.getLeft());
        delta = factory.toMdsonicASIntArithmetic(right).subtract(triple.getRight());
        network.sendToAll(epsilon.serializeShare());  // or: serializer.serialize(epsilon.getShare())
        network.sendToAll(delta.serializeShare());  // or: serializer.serialize(delta.getShare())
        return EvaluationStatus.HAS_MORE_ROUNDS;
      } else {
        Pair<PlainT, PlainT> epsilonAndDelta = receiveAndReconstruct(network,
                factory,
                resourcePool.getNoOfParties(),
                serializer);
        // compute [prod] = [c] + epsilon * [b] + delta * [a] + epsilon * delta
        PlainT e = epsilonAndDelta.getFirst();
        PlainT d = epsilonAndDelta.getSecond();
        PlainT ed = e.multiply(d);
        MdsonicASIntArithmetic<PlainT> tripleRight = triple.getRight();
        MdsonicASIntArithmetic<PlainT> tripleLeft = triple.getLeft();
        MdsonicASIntArithmetic<PlainT> tripleProduct = triple.getProduct();
        this.product = tripleProduct
                .add(tripleRight.multiply(e))
                .add(tripleLeft.multiply(d))
                .addConstant(ed,
                        macKeyShare,
                        factory.zero(),
                        resourcePool.getMyId() == 1);
        openedValueStore.pushOpenedValues(
                Arrays.asList(epsilon, delta),
                Arrays.asList(e, d)
        );
        return EvaluationStatus.IS_DONE;
      }
    }
  }

  /**
   * Retrieves shares for epsilon and delta and reconstructs each.
   */
  private Pair<PlainT, PlainT> receiveAndReconstruct(Network network,
                                                     MdsonicCompUIntFactory<PlainT> factory, int noOfParties,
                                                     ByteSerializer<PlainT> serializer) {
    PlainT e = factory.zero();
    PlainT d = factory.zero();
    for (int i = 1; i <= noOfParties; i++) {
      e = e.add(serializer.deserialize(network.receive(i)));
      d = d.add(serializer.deserialize(network.receive(i)));
    }
    return new Pair<>(e, d);
  }

  @Override
  public SInt out() {
    return product;
  }

}
