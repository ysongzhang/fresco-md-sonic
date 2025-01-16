package dk.alexandra.fresco.suite.mdsonic.protocols.natives;

import dk.alexandra.fresco.framework.builder.numeric.mdsonic.AdvancedNumericMdsonic.TruncationPair;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicCompUInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicGF;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;

public class MdsonicTruncationPairProtocol<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>> extends
        MdsonicNativeProtocol<TruncationPair, PlainT, SecretP> {

  private TruncationPair pair;
  private final int d;

  public MdsonicTruncationPairProtocol(int d) {
    this.d = d;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    pair = resourcePool.getDataSupplier().getNextTruncationPair(d);
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public TruncationPair out() {
    return pair;
  }
}
