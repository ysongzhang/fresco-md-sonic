package dk.alexandra.fresco.suite.mdml.protocols.natives;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUInt;
import dk.alexandra.fresco.suite.mdml.resource.MdmlResourcePool;

/**
 * Native protocol for generating a random shared bit.
 */
public class MdmlRandomBitProtocol<PlainT extends MdmlCompUInt<?, ?, PlainT>>
    extends MdmlNativeProtocol<SInt, PlainT> {

  private SInt bit;

  @Override
  public EvaluationStatus evaluate(int round, MdmlResourcePool<PlainT> resourcePool,
      Network network) {
    this.bit = resourcePool.getDataSupplier().getNextBitShare();
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return bit;
  }

}
