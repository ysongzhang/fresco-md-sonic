package dk.alexandra.fresco.suite.spdz2k.synchronization;

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedStrategy;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.suite.ProtocolSuite.RoundSynchronization;
import dk.alexandra.fresco.suite.spdz2k.Spdz2kBuilder;
import dk.alexandra.fresco.suite.spdz2k.Spdz2kProtocolSuite;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntConverter;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSIntArithmetic;
import dk.alexandra.fresco.suite.spdz2k.datatypes.UInt;
import dk.alexandra.fresco.suite.spdz2k.protocols.computations.Spdz2kMacCheckComputation;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.RequiresMacCheck;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * Round synchronization for SPDZ2k. <p>Requires a mac check to be performed on an all opened
 * unauthenticated values whenever an output protocol is encountered in a batch.</p>
 */
public class Spdz2kRoundSynchronization<
    HighT extends UInt<HighT>,
    LowT extends UInt<LowT>,
    PlainT extends CompUInt<HighT, LowT, PlainT>>
    implements RoundSynchronization<Spdz2kResourcePool<PlainT>> {

  private static final int OPEN_VALUE_THRESHOLD = 1000000;
  private final int openValueThreshold;
  private final int batchSize;
  private boolean isCheckRequired;
  private final Spdz2kProtocolSuite<HighT, LowT, PlainT> protocolSuite;

  public Spdz2kRoundSynchronization(Spdz2kProtocolSuite<HighT, LowT, PlainT> protocolSuite) {
    this(protocolSuite, OPEN_VALUE_THRESHOLD, 128);
  }

  public Spdz2kRoundSynchronization(Spdz2kProtocolSuite<HighT, LowT, PlainT> protocolSuite,
      int openValueThreshold,
      int batchSize) {
    this.protocolSuite = protocolSuite;
    this.openValueThreshold = openValueThreshold;
    this.batchSize = batchSize;
    this.isCheckRequired = false;
  }

  private void doMacCheck(Spdz2kResourcePool<PlainT> resourcePool, Network network) {
//    Pair<List<Spdz2kSIntArithmetic<PlainT>>, List<PlainT>> bar = resourcePool.getOpenedValueStore().popValues();
//    bar.getFirst().clear();
//    bar.getSecond().clear();
    Spdz2kBuilder<PlainT> builder = new Spdz2kBuilder<>(resourcePool.getFactory(),
        protocolSuite.createBasicNumericContext(resourcePool),
        protocolSuite.createRealNumericContext(),
        false);
    BatchEvaluationStrategy<Spdz2kResourcePool<PlainT>> batchStrategy = new BatchedStrategy<>();
    BatchedProtocolEvaluator<Spdz2kResourcePool<PlainT>> evaluator = new BatchedProtocolEvaluator<>(
        batchStrategy,
        protocolSuite,
        batchSize);
    OpenedValueStore<Spdz2kSIntArithmetic<PlainT>, PlainT> store = resourcePool.getOpenedValueStore();
    Spdz2kMacCheckComputation<HighT, LowT, PlainT> macCheck = new Spdz2kMacCheckComputation<>(
        store.popValues(),
        resourcePool, resourcePool::createRandomGenerator, resourcePool.getDrbgSeedBitLength());
    ProtocolBuilderNumeric sequential = builder.createSequential();
    macCheck.buildComputation(sequential);
//    long then = System.currentTimeMillis();
//    System.out.println("Time1-MACCheck: " + then + " ms.");
    ProtocolEvaluator.EvaluationStatistics eval = evaluator.eval(sequential.build(), resourcePool, network);

//    System.out.println("Evaluator done."
//            + " Evaluated a total of " + eval.getNativeProtocols()
//            + " native protocols in " + eval.getBatches() + " batches.");
//    long now = System.currentTimeMillis();
//    long timeSpent = now - then;
//    System.out.println("Duration: " + timeSpent + "ms.");
  }

  @Override
  public void finishedBatch(int gatesEvaluated, Spdz2kResourcePool<PlainT> resourcePool,
      Network network) {
    OpenedValueStore<Spdz2kSIntArithmetic<PlainT>, PlainT> store = resourcePool.getOpenedValueStore();
    if (isCheckRequired || store.exceedsThreshold(openValueThreshold)) {
      doMacCheck(resourcePool, network);
      isCheckRequired = false;
    }
  }

  @Override
  public void finishedEval(Spdz2kResourcePool<PlainT> resourcePool, Network network) {
    OpenedValueStore<Spdz2kSIntArithmetic<PlainT>, PlainT> store = resourcePool.getOpenedValueStore();
    if (store.hasPendingValues()) {
//      System.out.println("Because eval finished");
      doMacCheck(resourcePool, network);
    }
  }

  @Override
  public void beforeBatch(
      ProtocolCollection<Spdz2kResourcePool<PlainT>> nativeProtocols,
      Spdz2kResourcePool<PlainT> resourcePool, Network network) {
    this.isCheckRequired = StreamSupport.stream(nativeProtocols.spliterator(), false)
        .anyMatch(p -> p instanceof RequiresMacCheck);
//    System.out.println(outputFound);
    OpenedValueStore<Spdz2kSIntArithmetic<PlainT>, PlainT> store = resourcePool.getOpenedValueStore();
    if (store.hasPendingValues() && this.isCheckRequired) {
//      System.out.println("Because of output");
      doMacCheck(resourcePool, network);
    }
  }

}
