package dk.alexandra.fresco.suite.mdsonic.protocols.natives;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicCompUInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicGF;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;

/**
 * Native protocol for generating a random shared value.
 */
public class MdsonicRandomElementProtocol<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>>
    extends MdsonicNativeProtocol<SInt, PlainT, SecretP> {

  private SInt element;

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    this.element = resourcePool.getDataSupplier().getNextRandomElementShare();
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return element;
  }

}
