package dk.alexandra.fresco.suite.mdsonic.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;

import java.util.ArrayList;
import java.util.List;

public class MdsonicNotBatchedProtocol<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>> extends
        MdsonicNativeProtocol<List<DRes<SBool>>, PlainT, SecretP> {

  private final boolean useMaskedEvaluation;
  private final DRes<List<DRes<SBool>>> bits;
  private List<DRes<SBool>> result;

  public MdsonicNotBatchedProtocol(DRes<List<DRes<SBool>>> bits) {
    this.bits = bits;
    this.useMaskedEvaluation = false;
  }

  public MdsonicNotBatchedProtocol(DRes<List<DRes<SBool>>> bits, boolean useMaskedEvaluation) {
    this.bits = bits;
    this.useMaskedEvaluation = useMaskedEvaluation;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    List<DRes<SBool>> bitsOut = bits.out();
    this.result = new ArrayList<>(bitsOut.size());
    if (useMaskedEvaluation) {
      for (DRes<SBool> secretBit : bitsOut) {
        MdsonicMSBoolBoolean<SecretP> secretBitOut = (MdsonicMSBoolBoolean<SecretP>) secretBit.out();
        MdsonicMSBoolBoolean<SecretP> notBit = secretBitOut.xorOpen(true);
        result.add(notBit);
      }
    } else {
      SecretP macKeyShareBoolean = resourcePool.getDataSupplier().getSecretSharedKeyBoolean();
      MdsonicGFFactory<SecretP> factoryBoolean = resourcePool.getBooleanFactory();
      for (DRes<SBool> secretBit : bitsOut) {
        MdsonicASBoolBoolean<SecretP> secretBitOut = (MdsonicASBoolBoolean<SecretP>) secretBit.out();
        MdsonicASBoolBoolean<SecretP> notBit = secretBitOut.xorOpen(true, macKeyShareBoolean, factoryBoolean.zero(), resourcePool.getMyId() == 1);
        result.add(notBit);
      }
    }
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public List<DRes<SBool>> out() {
    return result;
  }

}
