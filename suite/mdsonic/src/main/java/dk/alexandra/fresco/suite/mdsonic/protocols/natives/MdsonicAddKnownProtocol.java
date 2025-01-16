package dk.alexandra.fresco.suite.mdsonic.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicCompUInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicCompUIntFactory;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicGF;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;
import dk.alexandra.fresco.suite.mdsonic.resource.storage.MdsonicDataSupplier;

/**
 * Native protocol from computing the sum of a secret value and a public constant.
 * For Arithmetic secret sharing.
 */
public class MdsonicAddKnownProtocol<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>>
    extends MdsonicNativeProtocol<SInt, PlainT, SecretP> {

  private final boolean useMaskedEvaluation;
  private final PlainT left;
  private final DRes<SInt> right;
  private SInt out;

  /**
   * Creates new {@link MdsonicAddKnownProtocol}.
   *
   * @param left public summand
   * @param right secret summand
   */
  public MdsonicAddKnownProtocol(PlainT left, DRes<SInt> right) {
    this.left = left;
    this.right = right;
    this.useMaskedEvaluation = false;
  }

  public MdsonicAddKnownProtocol(PlainT left, DRes<SInt> right, boolean useMaskedEvaluation) {
    this.left = left;
    this.right = right;
    this.useMaskedEvaluation = useMaskedEvaluation;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    MdsonicCompUIntFactory<PlainT> factory = resourcePool.getFactory();
    if (useMaskedEvaluation) {
      out = factory.toMdsonicMSIntArithmetic(right).addConstant(left);
      return EvaluationStatus.IS_DONE;
    } else {
      MdsonicDataSupplier<PlainT, SecretP> dataSupplier = resourcePool.getDataSupplier();
      out = factory.toMdsonicASIntArithmetic(right).addConstant(left,
              dataSupplier.getSecretSharedKey(),
              factory.zero(),
              resourcePool.getMyId() == 1);
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public SInt out() {
    return out;
  }

}
