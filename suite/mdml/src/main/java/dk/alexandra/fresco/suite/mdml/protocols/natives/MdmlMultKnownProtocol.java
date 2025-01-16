package dk.alexandra.fresco.suite.mdml.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUIntFactory;
import dk.alexandra.fresco.suite.mdml.resource.MdmlResourcePool;

/**
 * Native protocol from computing the product of a secret value and a public constant.
 */
public class MdmlMultKnownProtocol<PlainT extends MdmlCompUInt<?, ?, PlainT>>
    extends MdmlNativeProtocol<SInt, PlainT> {

  private final DRes<OInt> left;
  private final DRes<SInt> right;
  private SInt out;

  /**
   * Creates new {@link MdmlMultKnownProtocol}.
   *
   * @param left public factor
   * @param right secret factor
   */
  public MdmlMultKnownProtocol(DRes<OInt> left, DRes<SInt> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdmlResourcePool<PlainT> resourcePool,
      Network network) {
    MdmlCompUIntFactory<PlainT> factory = resourcePool.getFactory();
    out = factory.toMdmlMSIntArithmetic(right).multiply(factory.fromOInt(left));
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return out;
  }

}
