package dk.alexandra.fresco.suite.mdsonic.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;

/**
 * Native protocol for subtracting a secret value from a known public value. <p>Note that the result
 * is a secret value.</p>
 */
public class MdsonicSubtractFromKnownProtocol<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>>
    extends MdsonicNativeProtocol<SInt, PlainT, SecretP> {

  private final boolean useMaskedEvaluation;
  private final DRes<OInt> left;
  private final DRes<SInt> right;
  private SInt difference;

  /**
   * Creates new {@link MdsonicSubtractFromKnownProtocol}.
   *
   * @param left plain value
   * @param right secret value to be subtracted
   */
  public MdsonicSubtractFromKnownProtocol(DRes<OInt> left, DRes<SInt> right) {
    this.left = left;
    this.right = right;
    this.useMaskedEvaluation = false;
  }

  public MdsonicSubtractFromKnownProtocol(DRes<OInt> left, DRes<SInt> right, boolean useMaskedEvaluation) {
    this.left = left;
    this.right = right;
    this.useMaskedEvaluation = useMaskedEvaluation;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    PlainT secretSharedKey = resourcePool.getDataSupplier().getSecretSharedKey();
    PlainT zero = resourcePool.getFactory().zero();
    MdsonicCompUIntFactory<PlainT> factory = resourcePool.getFactory();
    if (useMaskedEvaluation) {
      MdsonicMSIntArithmetic<PlainT> leftSInt = new MdsonicMSIntArithmetic<>(factory.fromOInt(left),
              secretSharedKey, zero,
              resourcePool.getMyId() == 1);
      difference = leftSInt.subtract(factory.toMdsonicMSIntArithmetic(right));
    } else {
      MdsonicASIntArithmetic<PlainT> leftSInt = new MdsonicASIntArithmetic<>(factory.fromOInt(left),
              secretSharedKey, zero,
              resourcePool.getMyId() == 1);
      difference = leftSInt.subtract(factory.toMdsonicASIntArithmetic(right));
    }
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return difference;
  }

}
