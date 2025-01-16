package dk.alexandra.fresco.suite.mdml.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlMSIntArithmetic;
import dk.alexandra.fresco.suite.mdml.resource.MdmlResourcePool;

public class MdmlAddProtocol<PlainT extends MdmlCompUInt<?, ?, PlainT>> extends
        MdmlNativeProtocol<SInt, PlainT> {

  private final DRes<SInt> left;
  private final DRes<SInt> right;
  private SInt result;

  public MdmlAddProtocol(
      DRes<SInt> left,
      DRes<SInt> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdmlResourcePool<PlainT> resourcePool,
      Network network) {
    MdmlMSIntArithmetic<PlainT> leftVal = resourcePool.getFactory().toMdmlMSIntArithmetic(left);
    MdmlMSIntArithmetic<PlainT> rightVal = resourcePool.getFactory().toMdmlMSIntArithmetic(right);
    result = leftVal.add(rightVal);
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return result;
  }

}
