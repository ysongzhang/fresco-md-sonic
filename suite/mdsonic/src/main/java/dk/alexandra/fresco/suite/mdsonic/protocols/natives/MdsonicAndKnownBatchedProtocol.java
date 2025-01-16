package dk.alexandra.fresco.suite.mdsonic.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;

import java.util.ArrayList;
import java.util.List;

public class MdsonicAndKnownBatchedProtocol<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>> extends
        MdsonicNativeProtocol<List<DRes<SBool>>, PlainT, SecretP> {

  private final boolean useMaskedEvaluation;
  private final DRes<List<Boolean>> left;
  private final DRes<List<DRes<SBool>>> right;
  private List<DRes<SBool>> result;

  public MdsonicAndKnownBatchedProtocol(
          DRes<List<Boolean>> left,
      DRes<List<DRes<SBool>>> right) {
    this.left = left;
    this.right = right;
    this.useMaskedEvaluation = false;
  }

  public MdsonicAndKnownBatchedProtocol(
          DRes<List<Boolean>> left,
          DRes<List<DRes<SBool>>> right, boolean useMaskedEvaluation) {
    this.left = left;
    this.right = right;
    this.useMaskedEvaluation = useMaskedEvaluation;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    List<DRes<SBool>> rightOut = right.out();
    List<Boolean> leftOut = left.out();
    if (leftOut.size() != rightOut.size()) {
      throw new IllegalArgumentException("Lists must be same size");
    }
    this.result = new ArrayList<>(leftOut.size());

    if (useMaskedEvaluation) {
      for (int i = 0; i < leftOut.size(); i++) {
        boolean knownBit = leftOut.get(i);
        MdsonicMSBoolBoolean<SecretP> secretBit = (MdsonicMSBoolBoolean<SecretP>) rightOut.get(i).out();
        MdsonicMSBoolBoolean<SecretP> andedBit = secretBit.and(knownBit);
        result.add(andedBit);
      }
      return EvaluationStatus.IS_DONE;
    } else {
      for (int i = 0; i < leftOut.size(); i++) {
        boolean knownBit = leftOut.get(i);
        MdsonicASBoolBoolean<SecretP> secretBit = (MdsonicASBoolBoolean<SecretP>) rightOut.get(i).out();
        MdsonicASBoolBoolean<SecretP> andedBit = secretBit.and(knownBit);
        result.add(andedBit);
      }
      return EvaluationStatus.IS_DONE;
    }

  }

  @Override
  public List<DRes<SBool>> out() {
    return result;
  }

}
