package dk.alexandra.fresco.suite.mdsonic.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;

public class MdsonicXorKnownProtocol<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>> extends
        MdsonicNativeProtocol<SBool, PlainT, SecretP> {

  private final boolean useMaskedEvaluation;
  private final DRes<Boolean> left;
  private final DRes<SBool> right;
  private SBool result;

  public MdsonicXorKnownProtocol(
          DRes<Boolean> left,
      DRes<SBool> right) {
    this.left = left;
    this.right = right;
    this.useMaskedEvaluation = false;
  }

  public MdsonicXorKnownProtocol(
          DRes<Boolean> left,
          DRes<SBool> right, boolean useMaskedEvaluation) {
    this.left = left;
    this.right = right;
    this.useMaskedEvaluation = useMaskedEvaluation;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    boolean leftOut = left.out();
    if (useMaskedEvaluation) {
      MdsonicMSBoolBoolean<SecretP> rightBit = (MdsonicMSBoolBoolean<SecretP>) right.out();
      result = rightBit.xorOpen(leftOut);
    } else {
      SecretP macKeyShareBoolean = resourcePool.getDataSupplier().getSecretSharedKeyBoolean();
      MdsonicGFFactory<SecretP> factoryBoolean = resourcePool.getBooleanFactory();
      MdsonicASBoolBoolean<SecretP> rightBit = (MdsonicASBoolBoolean<SecretP>) right.out();
      result = rightBit.xorOpen(leftOut, macKeyShareBoolean, factoryBoolean.zero(), resourcePool.getMyId() == 1);
    }
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SBool out() {
    return result;
  }

}
