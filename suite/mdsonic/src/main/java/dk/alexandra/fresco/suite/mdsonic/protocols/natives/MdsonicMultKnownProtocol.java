package dk.alexandra.fresco.suite.mdsonic.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicCompUInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicCompUIntFactory;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicGF;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;

/**
 * Native protocol from computing the product of a secret value and a public constant.
 */
public class MdsonicMultKnownProtocol<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>>
    extends MdsonicNativeProtocol<SInt, PlainT, SecretP> {

  private final boolean useMaskedEvaluation;
  private final DRes<OInt> left;
  private final DRes<SInt> right;
  private SInt out;

  /**
   * Creates new {@link MdsonicMultKnownProtocol}.
   *
   * @param left public factor
   * @param right secret factor
   */
  public MdsonicMultKnownProtocol(DRes<OInt> left, DRes<SInt> right) {
    this.left = left;
    this.right = right;
    this.useMaskedEvaluation = false;
  }

  public MdsonicMultKnownProtocol(DRes<OInt> left, DRes<SInt> right, boolean useMaskedEvaluation) {
    this.left = left;
    this.right = right;
    this.useMaskedEvaluation = useMaskedEvaluation;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    MdsonicCompUIntFactory<PlainT> factory = resourcePool.getFactory();
    if (useMaskedEvaluation) {
      out = factory.toMdsonicMSIntArithmetic(right).multiply(factory.fromOInt(left));
    } else {
      out = factory.toMdsonicASIntArithmetic(right).multiply(factory.fromOInt(left));
    }
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return out;
  }

}
