package dk.alexandra.fresco.suite.mdml.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.ByteAndBitConverter;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUIntFactory;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlASIntArithmetic;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlSIntBoolean;
import dk.alexandra.fresco.suite.mdml.resource.MdmlResourcePool;

/**
 * Native protocol for converting boolean shares to arithmetic shares.
 */
public class MdmlBooleanToArithmeticProtocol<PlainT extends MdmlCompUInt<?, ?, PlainT>> extends
    MdmlNativeProtocol<SInt, PlainT> {

  private final DRes<SInt> bool;
  private SInt arithmetic;  // ASS
  private MdmlASIntArithmetic<PlainT> arithmeticR;
  private MdmlSIntBoolean<PlainT> c;

  public MdmlBooleanToArithmeticProtocol(DRes<SInt> bool) {
    this.bool = bool;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdmlResourcePool<PlainT> resourcePool,
      Network network) {
    MdmlCompUIntFactory<PlainT> factory = resourcePool.getFactory();
    if (round == 0) {
      arithmeticR = resourcePool.getDataSupplier().getNextBitShare();
      MdmlSIntBoolean<PlainT> booleanR = arithmeticR.toBoolean();
      c = factory.toMdmlSIntBoolean(bool).xor(booleanR);
      network.sendToAll(c.serializeShare());
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      PlainT openC = receiveAndReconstruct(network, resourcePool.getNoOfParties(), factory);
      resourcePool.getOpenedValueStore().pushOpenedValue(
          c.asArithmetic(),
          openC.toArithmeticRep()
      );

      PlainT openCBit = openC.bitValue() == 1 ? factory.one() : factory.zero();
      PlainT macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
      boolean isPartyOne = resourcePool.getMyId() == 1;
      arithmetic = arithmeticR.addConstant(openCBit, macKeyShare, factory.zero(),
          isPartyOne).subtract(arithmeticR.multiply(factory.two().multiplyByBit(openC.bitValue())));
      return EvaluationStatus.IS_DONE;
    }
  }

  /**
   * Receive shares of value and reconstruct. <p>Note that this includes overflow into top s
   * bits.</p>
   */
  private PlainT receiveAndReconstruct(Network network, int noOfParties,
                                       MdmlCompUIntFactory<PlainT> factory) {
    int sLengthByte = factory.getHighBitLength() / Byte.SIZE;
    byte[] received = network.receive(1);
    int info = received[sLengthByte];
    int bit = (info & 1);
    long high = ByteAndBitConverter.toLong(received, 1, sLengthByte);
    PlainT opened = factory.fromBitAndHigh(high, bit);
    for (int i = 2; i <= noOfParties; i++) {
      received = network.receive(i);
      info = received[sLengthByte];
      bit = (info & 1);
      high = ByteAndBitConverter.toLong(received, 1, sLengthByte);
      opened = opened.add(factory.fromBitAndHigh(high, bit));
    }
    return opened;
  }

  @Override
  public SInt out() {
    return arithmetic;
  }

}
