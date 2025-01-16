package dk.alexandra.fresco.suite.mdml.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUIntFactory;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlMSIntArithmetic;
import dk.alexandra.fresco.suite.mdml.resource.MdmlResourcePool;

/**
 * Native protocol for subtracting a secret value from a known public value. <p>Note that the result
 * is a secret value.</p>
 */
public class MdmlSubtractFromKnownProtocol<PlainT extends MdmlCompUInt<?, ?, PlainT>>
    extends MdmlNativeProtocol<SInt, PlainT> {

  private final DRes<OInt> left;
  private final DRes<SInt> right;
  private SInt difference;

  /**
   * Creates new {@link MdmlSubtractFromKnownProtocol}.
   *
   * @param left plain value
   * @param right secret value to be subtracted
   */
  public MdmlSubtractFromKnownProtocol(DRes<OInt> left, DRes<SInt> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdmlResourcePool<PlainT> resourcePool,
      Network network) {
    PlainT secretSharedKey = resourcePool.getDataSupplier().getSecretSharedKey();
    PlainT zero = resourcePool.getFactory().zero();
    MdmlCompUIntFactory<PlainT> factory = resourcePool.getFactory();
    MdmlMSIntArithmetic<PlainT> leftSInt = new MdmlMSIntArithmetic<>(factory.fromOInt(left),
        secretSharedKey, zero,
        resourcePool.getMyId() == 1);
    difference = leftSInt.subtract(factory.toMdmlMSIntArithmetic(right));
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return difference;
  }

}
