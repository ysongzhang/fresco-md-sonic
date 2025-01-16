package dk.alexandra.fresco.suite.mdsonic.protocols.natives.MSS;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;
import dk.alexandra.fresco.suite.mdsonic.protocols.natives.MdsonicNativeProtocol;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;

import java.util.ArrayList;
import java.util.List;

/**
 * Native protocol for computing logical AND of two values in boolean form.
 */
public class MdsonicOrBatchedProtocolMSS<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>> extends
        MdsonicNativeProtocol<List<DRes<SBool>>, PlainT, SecretP> {  // return ASS

  private DRes<List<DRes<SBool>>> bitsADef;
  private DRes<List<DRes<SBool>>> bitsBDef;
  private MdsonicASBoolBoolean<SecretP> innerProduct;
  private boolean crossOpen;
  private List<DRes<SBool>> products;

  public MdsonicOrBatchedProtocolMSS(DRes<List<DRes<SBool>>> bitsA,
                                     DRes<List<DRes<SBool>>> bitsB) {
    this.bitsADef = bitsA;
    this.bitsBDef = bitsB;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    List<DRes<SBool>> bitsA = bitsADef.out();
    List<DRes<SBool>> bitsB = bitsBDef.out();
    if (bitsA.size() != bitsB.size()) {
      throw new IllegalArgumentException("Lists must be same size");
    }
    SecretP macKeyShareBoolean = resourcePool.getDataSupplier().getSecretSharedKeyBoolean();
    MdsonicGFFactory<SecretP> factoryBoolean = resourcePool.getBooleanFactory();
    products = new ArrayList<>(bitsA.size());
    for (int i = 0; i < bitsA.size(); i++) {
      MdsonicMSBoolBoolean<SecretP> leftBit = (MdsonicMSBoolBoolean<SecretP>) bitsA.get(i).out();
      MdsonicMSBoolBoolean<SecretP> rightBit = (MdsonicMSBoolBoolean<SecretP>) bitsB.get(i).out();
      innerProduct = resourcePool.getDataSupplier().getNextBitTripleProductShare();
      crossOpen = (leftBit.getOpened() & rightBit.getOpened());
      MdsonicASBoolBoolean<SecretP> prod = innerProduct.xorOpen(crossOpen, macKeyShareBoolean, factoryBoolean.zero(), resourcePool.getMyId() == 1)
              .xor(leftBit.getMaskedSecret().and(rightBit.getOpened()))
              .xor(rightBit.getMaskedSecret().and(leftBit.getOpened()));
      MdsonicASBoolBoolean<SecretP> xored = leftBit.getMaskedSecret().xor(rightBit.getMaskedSecret())
              .xorOpen(leftBit.getOpened(), macKeyShareBoolean, factoryBoolean.zero(), resourcePool.getMyId() == 1)
              .xorOpen(rightBit.getOpened(), macKeyShareBoolean, factoryBoolean.zero(), resourcePool.getMyId() == 1);
      products.add(prod.xor(xored));
    }
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public List<DRes<SBool>> out() {
    return products;
  }

}
