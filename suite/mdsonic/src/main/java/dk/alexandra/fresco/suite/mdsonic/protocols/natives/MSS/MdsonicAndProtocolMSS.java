package dk.alexandra.fresco.suite.mdsonic.protocols.natives.MSS;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;
import dk.alexandra.fresco.suite.mdsonic.protocols.natives.MdsonicNativeProtocol;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;

/**
 * Native protocol for computing logical AND of two values in boolean form.
 */
public class MdsonicAndProtocolMSS<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>> extends
        MdsonicNativeProtocol<SBool, PlainT, SecretP> {  // return ASS

  private final DRes<SBool> left;
  private final DRes<SBool> right;
  private MdsonicASBoolBoolean<SecretP> innerProduct;
  private SBool product;

  /**
   * Creates new {@link MdsonicAndProtocolMSS}.
   *
   * @param left left factor
   * @param right right factor
   */
  public MdsonicAndProtocolMSS(DRes<SBool> left, DRes<SBool> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    SecretP macKeyShareBoolean = resourcePool.getDataSupplier().getSecretSharedKeyBoolean();
    MdsonicGFFactory<SecretP> factoryBoolean = resourcePool.getBooleanFactory();
    innerProduct = resourcePool.getDataSupplier().getNextBitTripleProductShare();
    MdsonicMSBoolBoolean<SecretP> leftBit = (MdsonicMSBoolBoolean<SecretP>) left.out();
    MdsonicMSBoolBoolean<SecretP> rightBit = (MdsonicMSBoolBoolean<SecretP>) right.out();
    boolean crossOpen = (leftBit.getOpened() & rightBit.getOpened());
    this.product = innerProduct.xorOpen(crossOpen, macKeyShareBoolean, factoryBoolean.zero(), resourcePool.getMyId() == 1)
            .xor(leftBit.getMaskedSecret().and(rightBit.getOpened()))
            .xor(rightBit.getMaskedSecret().and(leftBit.getOpened()));
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SBool out() {
    return product;
  }
}
