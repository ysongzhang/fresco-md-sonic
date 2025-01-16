package dk.alexandra.fresco.suite.mdsonic.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicASBoolBoolean;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicCompUInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicGF;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicMSBoolBoolean;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;

import java.util.List;

public class MdsonicXorListProtocol<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>> extends
        MdsonicNativeProtocol<SBool, PlainT, SecretP> {

  private final boolean useMaskedEvaluation;
  private final DRes<List<DRes<SBool>>> input;
  private SBool result;

  public MdsonicXorListProtocol(
          DRes<List<DRes<SBool>>> input) {
    this.input = input;
    this.useMaskedEvaluation = false;
  }

  public MdsonicXorListProtocol(
          DRes<List<DRes<SBool>>> input, boolean useMaskedEvaluation) {
    this.input = input;
    this.useMaskedEvaluation = useMaskedEvaluation;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    List<DRes<SBool>> inputOut = input.out();
    if (useMaskedEvaluation) {
      MdsonicMSBoolBoolean<SecretP> res = (MdsonicMSBoolBoolean<SecretP>) inputOut.get(0).out();
      for (int i = 1; i < inputOut.size(); i++) {
          MdsonicMSBoolBoolean<SecretP> nextBit = (MdsonicMSBoolBoolean<SecretP>) inputOut.get(i).out();
          res = res.xor(nextBit);
      }
      result = res;
    } else {
      MdsonicASBoolBoolean<SecretP> res = (MdsonicASBoolBoolean<SecretP>) inputOut.get(0).out();
      for (int i = 1; i < inputOut.size(); i++) {
        MdsonicASBoolBoolean<SecretP> nextBit = (MdsonicASBoolBoolean<SecretP>) inputOut.get(i).out();
        res = res.xor(nextBit);
      }
      result = res;
    }
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SBool out() {
    return result;
  }

}
