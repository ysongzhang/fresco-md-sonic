package dk.alexandra.fresco.suite.mdsonic.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;

public class MdsonicXorProtocol<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>> extends
        MdsonicNativeProtocol<SBool, PlainT, SecretP> {

  private final boolean useMaskedEvaluation;
  private final DRes<SBool> left;
  private final DRes<SBool> right;
  private SBool result;

  public MdsonicXorProtocol(
      DRes<SBool> left,
      DRes<SBool> right) {
    this.left = left;
    this.right = right;
    this.useMaskedEvaluation = false;
  }

  public MdsonicXorProtocol(
          DRes<SBool> left,
          DRes<SBool> right, boolean useMaskedEvaluation) {
    this.left = left;
    this.right = right;
    this.useMaskedEvaluation = useMaskedEvaluation;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    if (useMaskedEvaluation) {
      MdsonicMSBoolBoolean<SecretP> leftBit = (MdsonicMSBoolBoolean<SecretP>) left.out();
      MdsonicMSBoolBoolean<SecretP> rightBit = (MdsonicMSBoolBoolean<SecretP>) right.out();
      result = leftBit.xor(rightBit);
    } else {
      MdsonicASBoolBoolean<SecretP> leftBit = (MdsonicASBoolBoolean<SecretP>) left.out();
      MdsonicASBoolBoolean<SecretP> rightBit = (MdsonicASBoolBoolean<SecretP>) right.out();
      result = leftBit.xor(rightBit);
    }
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SBool out() {
    return result;
  }

}
