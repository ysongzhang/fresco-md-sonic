package dk.alexandra.fresco.suite.mdml.protocols.natives;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric.TruncationPair;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUInt;
import dk.alexandra.fresco.suite.mdml.resource.MdmlResourcePool;

public class MdmlTruncationPairProtocol<PlainT extends MdmlCompUInt<?, ?, PlainT>> extends
    MdmlNativeProtocol<TruncationPair, PlainT> {

  private TruncationPair pair;
  private final int d;

  public MdmlTruncationPairProtocol(int d) {
    this.d = d;
  }

  @Override
  public NativeProtocol.EvaluationStatus evaluate(int round, MdmlResourcePool<PlainT> resourcePool,
                                                  Network network) {
    pair = resourcePool.getDataSupplier().getNextTruncationPair(d);
    return NativeProtocol.EvaluationStatus.IS_DONE;
  }

  @Override
  public TruncationPair out() {
    return pair;
  }
}
