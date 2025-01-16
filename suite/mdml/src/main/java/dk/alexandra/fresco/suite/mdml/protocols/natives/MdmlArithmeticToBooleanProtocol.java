package dk.alexandra.fresco.suite.mdml.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlASIntArithmetic;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlSIntBoolean;
import dk.alexandra.fresco.suite.mdml.resource.MdmlResourcePool;

public class MdmlArithmeticToBooleanProtocol<PlainT extends MdmlCompUInt<?, ?, PlainT>> extends
    MdmlNativeProtocol<SInt, PlainT> {

  private final DRes<SInt> arithmetic;  // ASS
  private SInt bool;

  public MdmlArithmeticToBooleanProtocol(DRes<SInt> arithmetic) {
    this.arithmetic = arithmetic;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdmlResourcePool<PlainT> resourcePool,
      Network network) {
    MdmlASIntArithmetic<PlainT> value = resourcePool.getFactory().toMdmlASIntArithmetic(arithmetic);
    bool = new MdmlSIntBoolean<>(
        value.getShare().toBitRep(),
        value.getMacShare().toBitRep().toArithmeticRep() // results in shift
    );
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return bool;
  }

}
