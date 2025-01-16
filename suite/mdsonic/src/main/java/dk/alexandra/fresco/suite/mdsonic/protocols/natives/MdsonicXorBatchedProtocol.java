package dk.alexandra.fresco.suite.mdsonic.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;

import java.util.ArrayList;
import java.util.List;

public class MdsonicXorBatchedProtocol<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>> extends
        MdsonicNativeProtocol<List<DRes<SBool>>, PlainT, SecretP> {

  private final boolean useMaskedEvaluation;
  private final DRes<List<DRes<SBool>>> left;
  private final DRes<List<DRes<SBool>>> right;
  private List<DRes<SBool>> result;

  public MdsonicXorBatchedProtocol(
          DRes<List<DRes<SBool>>> left,
          DRes<List<DRes<SBool>>> right) {
    this.left = left;
    this.right = right;
    this.useMaskedEvaluation = false;
  }

  public MdsonicXorBatchedProtocol(
          DRes<List<DRes<SBool>>> left,
          DRes<List<DRes<SBool>>> right, boolean useMaskedEvaluation) {
    this.left = left;
    this.right = right;
    this.useMaskedEvaluation = useMaskedEvaluation;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    List<DRes<SBool>> leftOut = left.out();
    List<DRes<SBool>> rightOut = right.out();
    if (leftOut.size() != rightOut.size()) {
      throw new IllegalArgumentException("Lists must be same size");
    }
    this.result = new ArrayList<>(leftOut.size());
    for (int i = 0; i < leftOut.size(); i++) {
      if (useMaskedEvaluation) {
        MdsonicMSBoolBoolean<SecretP> leftBit = (MdsonicMSBoolBoolean<SecretP>) leftOut.get(i).out();
        MdsonicMSBoolBoolean<SecretP> rightBit = (MdsonicMSBoolBoolean<SecretP>) rightOut.get(i).out();
        MdsonicMSBoolBoolean<SecretP> xoredBit = leftBit.xor(rightBit);
        result.add(xoredBit);
      } else {
        MdsonicASBoolBoolean<SecretP> leftBit = (MdsonicASBoolBoolean<SecretP>) leftOut.get(i).out();
        MdsonicASBoolBoolean<SecretP> rightBit = (MdsonicASBoolBoolean<SecretP>) rightOut.get(i).out();
        MdsonicASBoolBoolean<SecretP> xoredBit = leftBit.xor(rightBit);
        result.add(xoredBit);
      }
    }
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public List<DRes<SBool>> out() {
    return result;
  }

}
