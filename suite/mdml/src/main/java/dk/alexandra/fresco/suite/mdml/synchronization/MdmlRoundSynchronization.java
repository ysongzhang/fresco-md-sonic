package dk.alexandra.fresco.suite.mdml.synchronization;

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedStrategy;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.suite.ProtocolSuite.RoundSynchronization;
import dk.alexandra.fresco.suite.mdml.MdmlBuilder;
import dk.alexandra.fresco.suite.mdml.MdmlProtocolSuite;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlASIntArithmetic;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlUInt;
import dk.alexandra.fresco.suite.mdml.protocols.computations.MdmlMacCheckComputation;
import dk.alexandra.fresco.suite.mdml.protocols.natives.RequiresMacCheck;
import dk.alexandra.fresco.suite.mdml.resource.MdmlResourcePool;

import java.util.stream.StreamSupport;

/**
 * Round synchronization for MD-ML. <p>Requires a mac check to be performed on an all opened
 * unauthenticated values whenever an output protocol is encountered in a batch.</p>
 */
public class MdmlRoundSynchronization<
        HighT extends MdmlUInt<HighT>,
        LowT extends MdmlUInt<LowT>,
        PlainT extends MdmlCompUInt<HighT, LowT, PlainT>>
        implements RoundSynchronization<MdmlResourcePool<PlainT>> {

  private static final int OPEN_VALUE_THRESHOLD = 1000000;
  private final int openValueThreshold;
  private final int batchSize;
  private boolean isCheckRequired;
  private final MdmlProtocolSuite<HighT, LowT, PlainT> protocolSuite;

  public MdmlRoundSynchronization(MdmlProtocolSuite<HighT, LowT, PlainT> protocolSuite) {
    this(protocolSuite, OPEN_VALUE_THRESHOLD, 128);
  }

  public MdmlRoundSynchronization(MdmlProtocolSuite<HighT, LowT, PlainT> protocolSuite,
                                  int openValueThreshold,
                                  int batchSize) {
    this.protocolSuite = protocolSuite;
    this.openValueThreshold = openValueThreshold;
    this.batchSize = batchSize;
    this.isCheckRequired = false;
  }

  private void doMacCheck(MdmlResourcePool<PlainT> resourcePool, Network network) {
    MdmlBuilder<PlainT> builder = new MdmlBuilder<>(resourcePool.getFactory(),
            protocolSuite.createBasicNumericContext(resourcePool),
            protocolSuite.createRealNumericContext());
    BatchEvaluationStrategy<MdmlResourcePool<PlainT>> batchStrategy = new BatchedStrategy<>();
    BatchedProtocolEvaluator<MdmlResourcePool<PlainT>> evaluator = new BatchedProtocolEvaluator<>(
            batchStrategy,
            protocolSuite,
            batchSize);
    OpenedValueStore<MdmlASIntArithmetic<PlainT>, PlainT> store = resourcePool.getOpenedValueStore();
    MdmlMacCheckComputation<HighT, LowT, PlainT> macCheck = new MdmlMacCheckComputation<>(
            store.popValues(),
            resourcePool, resourcePool::createRandomGenerator, resourcePool.getDrbgSeedBitLength());
    ProtocolBuilderNumeric sequential = builder.createSequential();
    macCheck.buildComputation(sequential);
    // Ensure that the MAC check is executed correctly, as we previously overlooked the invocation of the eval function.
    evaluator.eval(sequential.build(), resourcePool, network);
  }

  @Override
  public void finishedBatch(int gatesEvaluated, MdmlResourcePool<PlainT> resourcePool,
                            Network network) {
    OpenedValueStore<MdmlASIntArithmetic<PlainT>, PlainT> store = resourcePool.getOpenedValueStore();
    if (isCheckRequired || store.exceedsThreshold(openValueThreshold)) {
      doMacCheck(resourcePool, network);
      isCheckRequired = false;
    }
  }

  @Override
  public void finishedEval(MdmlResourcePool<PlainT> resourcePool, Network network) {
    OpenedValueStore<MdmlASIntArithmetic<PlainT>, PlainT> store = resourcePool.getOpenedValueStore();
    if (store.hasPendingValues()) {
      doMacCheck(resourcePool, network);
    }
  }

  @Override
  public void beforeBatch(
          ProtocolCollection<MdmlResourcePool<PlainT>> nativeProtocols,
          MdmlResourcePool<PlainT> resourcePool, Network network) {
    this.isCheckRequired = StreamSupport.stream(nativeProtocols.spliterator(), false)
            .anyMatch(p -> p instanceof RequiresMacCheck);
    OpenedValueStore<MdmlASIntArithmetic<PlainT>, PlainT> store = resourcePool.getOpenedValueStore();
    if (store.hasPendingValues() && this.isCheckRequired) {
      doMacCheck(resourcePool, network);
    }
  }

}
