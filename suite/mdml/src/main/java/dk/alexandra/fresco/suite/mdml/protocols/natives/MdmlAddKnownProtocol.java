package dk.alexandra.fresco.suite.mdml.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUIntFactory;
import dk.alexandra.fresco.suite.mdml.resource.MdmlResourcePool;

/**
 * Native protocol from computing the sum of a secret value and a public constant.
 */
public class MdmlAddKnownProtocol<PlainT extends MdmlCompUInt<?, ?, PlainT>>
    extends MdmlNativeProtocol<SInt, PlainT> {

  private final PlainT left;
  private final DRes<SInt> right;
  private SInt out;

  /**
   * Creates new {@link MdmlAddKnownProtocol}.
   *
   * @param left public summand
   * @param right secret summand
   */
  public MdmlAddKnownProtocol(PlainT left, DRes<SInt> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdmlResourcePool<PlainT> resourcePool,
      Network network) {
    MdmlCompUIntFactory<PlainT> factory = resourcePool.getFactory();
    out = factory.toMdmlMSIntArithmetic(right).addConstant(left);
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return out;
  }

}
