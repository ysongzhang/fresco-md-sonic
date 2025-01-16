package dk.alexandra.fresco.suite.mdsonic.protocols.natives.MSS;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;
import dk.alexandra.fresco.suite.mdsonic.protocols.natives.MdsonicNativeProtocol;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;

/**
 * Native protocol for computing product of two secret numbers.
 */
public class MdsonicMultiplyProtocolMSS<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>> extends
        MdsonicNativeProtocol<SInt, PlainT, SecretP> {  // return ASS

  private final DRes<SInt> left;
  private final DRes<SInt> right;
  private MdsonicASIntArithmetic<PlainT> innerProduct;
  private SInt product;

  /**
   * Creates new {@link MdsonicMultiplyProtocolMSS}.
   *
   * @param left left factor
   * @param right right factor
   */
  public MdsonicMultiplyProtocolMSS(DRes<SInt> left, DRes<SInt> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    final PlainT macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
    MdsonicCompUIntFactory<PlainT> factory = resourcePool.getFactory();
    innerProduct = resourcePool.getDataSupplier().getNextTripleProductShare();
    MdsonicMSIntArithmetic<PlainT> outLeft = factory.toMdsonicMSIntArithmetic(left);
    MdsonicMSIntArithmetic<PlainT> outRight = factory.toMdsonicMSIntArithmetic(right);
    PlainT crossOpen = outLeft.getOpened().multiply(outRight.getOpened());
    this.product = innerProduct.addConstant(crossOpen, macKeyShare, factory.zero(), resourcePool.getMyId() == 1)
            .add(outLeft.getMaskedSecret().multiply(outRight.getOpened()))
            .add(outRight.getMaskedSecret().multiply(outLeft.getOpened()));
    return EvaluationStatus.IS_DONE;
  }


  @Override
  public SInt out() {
    return product;
  }

}
