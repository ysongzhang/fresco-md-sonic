package dk.alexandra.fresco.suite.mdsonic.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;
import dk.alexandra.fresco.suite.mdsonic.resource.storage.MdsonicDataSupplier;

/**
 * Native protocol for construct a secret value from a public constant and a secret random.
 */
public class MdsonicSecretProtocol<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>>
    extends MdsonicNativeProtocol<SInt, PlainT, SecretP> {

  private final boolean useMaskedEvaluation;
  private final PlainT opened;
  private final DRes<SInt> random;
  private SInt out;

  /**
   * Creates new {@link MdsonicSecretProtocol}.
   *
   * @param input public value to input
   */
  public MdsonicSecretProtocol(PlainT input, DRes<SInt> random) {
    this.opened = input;
    this.random = random;
    this.useMaskedEvaluation = false;
  }

  public MdsonicSecretProtocol(PlainT input, DRes<SInt> random, boolean useMaskedEvaluation) {
    this.opened = input;
    this.random = random;
    this.useMaskedEvaluation = useMaskedEvaluation;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    MdsonicCompUIntFactory<PlainT> factory = resourcePool.getFactory();
    MdsonicASIntArithmetic<PlainT> randomASS = factory.toMdsonicASIntArithmetic(random);
    MdsonicDataSupplier<PlainT, SecretP> dataSupplier = resourcePool.getDataSupplier();
    boolean isPartyOne = (resourcePool.getMyId() == 1);
    if (useMaskedEvaluation) {
      out = new MdsonicMSIntArithmetic<>(randomASS, opened);
    } else {
      out = randomASS.addConstant(opened, dataSupplier.getSecretSharedKey(), factory.zero(), isPartyOne);
    }
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return out;
  }

}
