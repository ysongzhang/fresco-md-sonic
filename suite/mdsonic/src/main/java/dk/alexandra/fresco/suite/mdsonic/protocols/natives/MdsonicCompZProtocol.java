package dk.alexandra.fresco.suite.mdsonic.protocols.natives;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;

/**
 * Native protocol for computing Zj = mj - \alpha * y in MAC check.
 */
public class MdsonicCompZProtocol<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>>
    extends MdsonicNativeProtocol<OInt, PlainT, SecretP> {

  private final PlainT y;
  private final PlainT mj;
  private OInt zj;

  /**
   * Creates new {@link MdsonicCompZProtocol}.
   */
  public MdsonicCompZProtocol(PlainT y, PlainT mj) {
    this.mj = mj;
    this.y = y;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    PlainT macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
    this.zj = mj.subtract(macKeyShare.multiply(y));
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public OInt out() {
    return zj;
  }

}
