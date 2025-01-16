package dk.alexandra.fresco.suite.mdml.protocols.natives;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUInt;
import dk.alexandra.fresco.suite.mdml.resource.MdmlResourcePool;

/**
 * Native protocol for generating a random shared value.
 */
public class MdmlRandomElementProtocol<PlainT extends MdmlCompUInt<?, ?, PlainT>>
    extends MdmlNativeProtocol<SInt, PlainT> {

  private SInt element;

  @Override
  public EvaluationStatus evaluate(int round, MdmlResourcePool<PlainT> resourcePool,
      Network network) {
    this.element = resourcePool.getDataSupplier().getNextRandomElementShare();
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return element;
  }

}
