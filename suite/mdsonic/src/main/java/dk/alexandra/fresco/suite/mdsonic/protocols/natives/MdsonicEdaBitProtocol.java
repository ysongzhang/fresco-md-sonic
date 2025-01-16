package dk.alexandra.fresco.suite.mdsonic.protocols.natives;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.builder.numeric.mdsonic.FMedaBit;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicCompUInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicGF;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;

/**
 * Native protocol for generating a edaBit.
 */
public class MdsonicEdaBitProtocol<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>>
    extends MdsonicNativeProtocol<FMedaBit, PlainT, SecretP> {

  private final boolean useMaskedEvaluation;
  private FMedaBit element;

  private final int FMDegree;

  private final int FMLength;

  public MdsonicEdaBitProtocol() {
    this.useMaskedEvaluation = false;
    this.FMDegree = 0;
    this.FMLength = 0;
  }

  public MdsonicEdaBitProtocol(int FMLength, int FMDegree) {
    this.useMaskedEvaluation = true;
    this.FMLength = FMLength;
    this.FMDegree = FMDegree;

  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    if (useMaskedEvaluation) {
      this.element = resourcePool.getDataSupplier().getNextFMedaBit(FMLength, FMDegree);
    } else {
      this.element = resourcePool.getDataSupplier().getNextEdaBit();
    }
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public FMedaBit out() {
    return element;
  }

}
