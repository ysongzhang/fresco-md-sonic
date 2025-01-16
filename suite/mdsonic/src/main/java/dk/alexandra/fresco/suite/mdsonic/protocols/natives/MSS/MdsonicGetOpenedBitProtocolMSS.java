package dk.alexandra.fresco.suite.mdsonic.protocols.natives.MSS;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;
import dk.alexandra.fresco.suite.mdsonic.protocols.natives.MdsonicNativeProtocol;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;

/**
 * Native protocol for return Opened value of an MSS Secret bit.
 */
public class MdsonicGetOpenedBitProtocolMSS<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>> extends
        MdsonicNativeProtocol<Boolean, PlainT, SecretP> {

  private final DRes<SBool> input;
  private Boolean out;

  /**
   * Creates new {@link MdsonicGetOpenedBitProtocolMSS}.
   */
  public MdsonicGetOpenedBitProtocolMSS(DRes<SBool> input) {
    this.input = input;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    MdsonicMSBoolBoolean<SecretP> outInput = (MdsonicMSBoolBoolean<SecretP>) input.out();;
    out = outInput.getOpened();
    return EvaluationStatus.IS_DONE;
  }


  @Override
  public Boolean out() {
    return out;
  }

}
