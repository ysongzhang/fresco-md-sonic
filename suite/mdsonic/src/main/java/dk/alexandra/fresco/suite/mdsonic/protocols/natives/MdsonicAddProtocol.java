package dk.alexandra.fresco.suite.mdsonic.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;

public class MdsonicAddProtocol<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>> extends
        MdsonicNativeProtocol<SInt, PlainT, SecretP> {

  private final boolean useMaskedEvaluation;
  private final DRes<SInt> left;
  private final DRes<SInt> right;
  private SInt result;

  public MdsonicAddProtocol(DRes<SInt> left, DRes<SInt> right) {
    this.left = left;
    this.right = right;
    this.useMaskedEvaluation = false;
  }

  public MdsonicAddProtocol(DRes<SInt> left, DRes<SInt> right, boolean useMaskedEvaluation) {
    this.left = left;
    this.right = right;
    this.useMaskedEvaluation = useMaskedEvaluation;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    if (useMaskedEvaluation) {
      MdsonicMSIntArithmetic<PlainT> leftVal = resourcePool.getFactory().toMdsonicMSIntArithmetic(left);
      MdsonicMSIntArithmetic<PlainT> rightVal = resourcePool.getFactory().toMdsonicMSIntArithmetic(right);
      result = leftVal.add(rightVal);
    } else {
      MdsonicASIntArithmetic<PlainT> leftVal = resourcePool.getFactory().toMdsonicASIntArithmetic(left);
      MdsonicASIntArithmetic<PlainT> rightVal = resourcePool.getFactory().toMdsonicASIntArithmetic(right);
      result = leftVal.add(rightVal);
    }
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return result;
  }

}
