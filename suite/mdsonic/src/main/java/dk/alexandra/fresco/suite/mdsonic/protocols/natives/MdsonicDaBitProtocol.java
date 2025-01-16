package dk.alexandra.fresco.suite.mdsonic.protocols.natives;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.builder.numeric.mdsonic.DaBit;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicCompUInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicGF;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;

/**
 * Native protocol for generating a daBit.
 */
public class MdsonicDaBitProtocol<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>>
    extends MdsonicNativeProtocol<DaBit, PlainT, SecretP> {

  private final boolean useMaskedEvaluation;
  private DaBit element;

  public MdsonicDaBitProtocol(boolean useMaskedEvaluation) {
    this.useMaskedEvaluation = useMaskedEvaluation;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
//    if (useMaskedEvaluation) {
//      this.element = resourcePool.getDataSupplier().getNextMaskedDaBit();
//    } else {
//      this.element = resourcePool.getDataSupplier().getNextDaBit();
//    }
    this.element = resourcePool.getDataSupplier().getNextDaBit();
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public DaBit out() {
    return element;
  }

}
