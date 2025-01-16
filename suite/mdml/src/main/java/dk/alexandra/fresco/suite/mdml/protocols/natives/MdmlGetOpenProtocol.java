package dk.alexandra.fresco.suite.mdml.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUIntFactory;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlMSIntArithmetic;
import dk.alexandra.fresco.suite.mdml.resource.MdmlResourcePool;

public class MdmlGetOpenProtocol<PlainT extends MdmlCompUInt<?, ?, PlainT>> extends
        MdmlNativeProtocol<OInt, PlainT> {

  private final DRes<SInt> input;
  private OInt result;

  public MdmlGetOpenProtocol(DRes<SInt> input) { this.input = input;}

  @Override
  public EvaluationStatus evaluate(int round, MdmlResourcePool<PlainT> resourcePool,
      Network network) {
    MdmlCompUIntFactory<PlainT> factory = resourcePool.getFactory();
    MdmlMSIntArithmetic<PlainT> outInput = factory.toMdmlMSIntArithmetic(input);
    PlainT delta = resourcePool.getDataSupplier().getNextOpenedDelta();
    result = outInput.getOpened().add(delta);
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public OInt out() {
    return result;
  }

}
