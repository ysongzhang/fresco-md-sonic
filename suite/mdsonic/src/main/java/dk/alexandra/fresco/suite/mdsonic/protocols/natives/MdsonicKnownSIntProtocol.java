package dk.alexandra.fresco.suite.mdsonic.protocols.natives;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;
import dk.alexandra.fresco.suite.mdsonic.resource.storage.MdsonicDataSupplier;

/**
 * Native protocol for converting a public constant into a secret value.
 */
public class MdsonicKnownSIntProtocol<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>>
    extends MdsonicNativeProtocol<SInt, PlainT, SecretP> {

  private final boolean useMaskedEvaluation;
  private final PlainT input;
  private SInt out;

  /**
   * Creates new {@link MdsonicKnownSIntProtocol}.
   *
   * @param input public value to input
   */
  public MdsonicKnownSIntProtocol(PlainT input) {
    this.input = input;
    this.useMaskedEvaluation = false;
  }

  public MdsonicKnownSIntProtocol(PlainT input, boolean useMaskedEvaluation) {
    this.input = input;
    this.useMaskedEvaluation = useMaskedEvaluation;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    MdsonicCompUIntFactory<PlainT> factory = resourcePool.getFactory();
    MdsonicDataSupplier<PlainT, SecretP> dataSupplier = resourcePool.getDataSupplier();
    boolean isPartyOne = (resourcePool.getMyId() == 1);
    if (useMaskedEvaluation) {
      out = new MdsonicMSIntArithmetic<>(input, dataSupplier.getSecretSharedKey(), factory.zero(), isPartyOne);
    } else {
      out = new MdsonicASIntArithmetic<>(input, dataSupplier.getSecretSharedKey(), factory.zero(), isPartyOne);
    }
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return out;
  }

}
