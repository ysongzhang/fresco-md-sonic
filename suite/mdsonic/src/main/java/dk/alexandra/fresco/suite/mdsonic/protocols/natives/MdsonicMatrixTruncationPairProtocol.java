package dk.alexandra.fresco.suite.mdsonic.protocols.natives;

import dk.alexandra.fresco.framework.builder.numeric.mdsonic.AdvancedNumericMdsonic.MatrixTruncationPair;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicCompUInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicGF;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;

public class MdsonicMatrixTruncationPairProtocol<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>> extends
        MdsonicNativeProtocol<MatrixTruncationPair, PlainT, SecretP> {

  private MatrixTruncationPair pair;
  private final int d;
  private final int width;
  private final int height;

  public MdsonicMatrixTruncationPairProtocol(int d, int height, int width) {
    this.d = d;
    this.height = height;
    this.width = width;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    pair = resourcePool.getDataSupplier().getNextMatrixTruncationPair(d, height, width);
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public MatrixTruncationPair out() {
    return pair;
  }
}
