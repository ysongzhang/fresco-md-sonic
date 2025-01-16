package dk.alexandra.fresco.suite.mdml.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.ByteAndBitConverter;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUIntFactory;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlSIntBoolean;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlTriple;
import dk.alexandra.fresco.suite.mdml.resource.MdmlResourcePool;

import java.util.Arrays;

/**
 * Native protocol for computing logical AND of two values in boolean form.
 */
public class MdmlAndProtocol<PlainT extends MdmlCompUInt<?, ?, PlainT>> extends
        MdmlNativeProtocol<SInt, PlainT> {

  private final DRes<SInt> left;
  private final DRes<SInt> right;
  private MdmlTriple<PlainT, MdmlSIntBoolean<PlainT>> triple;
  private MdmlSIntBoolean<PlainT> epsilon;
  private MdmlSIntBoolean<PlainT> delta;
  private SInt product;

  /**
   * Creates new {@link dk.alexandra.fresco.suite.mdml.protocols.natives.MdmlAndProtocol}.
   *
   * @param left left factor
   * @param right right factor
   */
  public MdmlAndProtocol(DRes<SInt> left, DRes<SInt> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdmlResourcePool<PlainT> resourcePool,
      Network network) {
    PlainT macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
    MdmlCompUIntFactory<PlainT> factory = resourcePool.getFactory();
    if (round == 0) {
      triple = resourcePool.getDataSupplier().getNextBitTripleShares();
      epsilon = factory.toMdmlSIntBoolean(left).xor(triple.getLeft());
      delta = factory.toMdmlSIntBoolean(right).xor(triple.getRight());
      int sLengthByte = factory.getHighBitLength() / Byte.SIZE;
      byte[] epsilonByte = epsilon.serializeShare();
      byte[] deltaByte = delta.serializeShare();
      int packed = epsilonByte[sLengthByte] ^ (deltaByte[sLengthByte] << 1);

      final byte[] bytes = new byte[2 * sLengthByte + 1];
      System.arraycopy(deltaByte, 0, bytes, 0, sLengthByte);
      System.arraycopy(epsilonByte, 0, bytes, sLengthByte, sLengthByte);
      bytes[2 * sLengthByte] = (byte) packed;
      network.sendToAll(bytes);
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      Pair<PlainT, PlainT> epsilonAndDelta = receiveAndReconstruct(network,
          resourcePool.getNoOfParties(),
          factory);
      PlainT e = epsilonAndDelta.getFirst();
      PlainT d = epsilonAndDelta.getSecond();
      resourcePool.getOpenedValueStore().pushOpenedValues(
          Arrays.asList(
              epsilon.asArithmetic(),
              delta.asArithmetic()
          ),
          Arrays.asList(
              e.toArithmeticRep(),
              d.toArithmeticRep()
          )
      );
      int eBit = e.bitValue();
      int dBit = d.bitValue();
      PlainT ed = e.multiply(d);
      // compute [prod] = [c] XOR epsilon AND [b] XOR delta AND [a] XOR epsilon AND delta
      MdmlSIntBoolean<PlainT> tripleLeft = triple.getLeft();
      MdmlSIntBoolean<PlainT> tripleRight = triple.getRight();
      MdmlSIntBoolean<PlainT> tripleProduct = triple.getProduct();
      this.product = tripleProduct
          .xor(tripleRight.and(eBit))
          .xor(tripleLeft.and(dBit))
          .xorOpen(ed,
              macKeyShare,
              factory.zero().toBitRep(),
              resourcePool.getMyId() == 1);
      return EvaluationStatus.IS_DONE;
    }
  }

  /**
   * Retrieves shares for epsilon and delta and reconstructs each.
   */
  private Pair<PlainT, PlainT> receiveAndReconstruct(Network network,
      int noOfParties, MdmlCompUIntFactory<PlainT> factory) {
    int sLengthByte = factory.getHighBitLength() / Byte.SIZE;
    byte[] received = network.receive(1);
    int info = received[2 * sLengthByte];
    int eBit = (info & 1);
    int dBit = ((info & 2) >>> 1);
    long dHigh = ByteAndBitConverter.toLong(received, sLengthByte + 1, sLengthByte);
    long eHigh = ByteAndBitConverter.toLong(received, 1, sLengthByte);
    PlainT e = factory.fromBitAndHigh(eHigh, eBit);
    PlainT d = factory.fromBitAndHigh(dHigh, dBit);
    for (int i = 2; i <= noOfParties; i++) {
      received = network.receive(i);
      info = received[2 * sLengthByte];
      eBit = (info & 1);
      dBit = ((info & 2) >>> 1);
      dHigh = ByteAndBitConverter.toLong(received, sLengthByte + 1, sLengthByte);
      eHigh = ByteAndBitConverter.toLong(received, 1, sLengthByte);
      e = e.add(factory.fromBitAndHigh(eHigh, eBit));
      d = d.add(factory.fromBitAndHigh(dHigh, dBit));
    }
    return new Pair<>(e, d);
  }

  @Override
  public SInt out() {
    return product;
  }
}
