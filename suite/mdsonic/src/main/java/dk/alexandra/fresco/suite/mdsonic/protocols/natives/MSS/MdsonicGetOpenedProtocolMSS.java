package dk.alexandra.fresco.suite.mdsonic.protocols.natives.MSS;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;
import dk.alexandra.fresco.suite.mdsonic.protocols.natives.MdsonicNativeProtocol;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;

/**
 * Native protocol for return Opened value of an MSS Secret.
 */
public class MdsonicGetOpenedProtocolMSS<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>> extends
        MdsonicNativeProtocol<OInt, PlainT, SecretP> {

  private final DRes<SInt> input;
  private OInt out;

  /**
   * Creates new {@link MdsonicGetOpenedProtocolMSS}.
   */
  public MdsonicGetOpenedProtocolMSS(DRes<SInt> input) {
    this.input = input;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    MdsonicCompUIntFactory<PlainT> factory = resourcePool.getFactory();
    MdsonicMSIntArithmetic<PlainT> outInput = factory.toMdsonicMSIntArithmetic(input);
    out = outInput.getOpened();
    return EvaluationStatus.IS_DONE;
  }


  @Override
  public OInt out() {
    return out;
  }

}
