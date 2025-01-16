package dk.alexandra.fresco.suite.mdsonic.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;

public class MdsonicAndKnownProtocol<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>> extends
        MdsonicNativeProtocol<SBool, PlainT, SecretP> {

  private final boolean useMaskedEvaluation;
  private final DRes<Boolean> left;
  private final DRes<SBool> right;
  private SBool result;

  public MdsonicAndKnownProtocol(
          DRes<Boolean> left,
      DRes<SBool> right) {
    this.left = left;
    this.right = right;
    this.useMaskedEvaluation = false;
  }

  public MdsonicAndKnownProtocol(
          DRes<Boolean> left,
          DRes<SBool> right, boolean useMaskedEvaluation) {
    this.left = left;
    this.right = right;
    this.useMaskedEvaluation = useMaskedEvaluation;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    boolean leftBit = left.out();
    if (useMaskedEvaluation) {
      MdsonicMSBoolBoolean<SecretP> outRight = (MdsonicMSBoolBoolean<SecretP>) right.out();
      result = outRight.and(leftBit);
    } else {
      MdsonicASBoolBoolean<SecretP> outRight = (MdsonicASBoolBoolean<SecretP>) right.out();
      result = outRight.and(leftBit);
    }
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SBool out() {
    return result;
  }

}
