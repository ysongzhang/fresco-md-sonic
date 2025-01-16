package dk.alexandra.fresco.suite.mdsonic.synchronization;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedStrategy;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.suite.ProtocolSuite.RoundSynchronization;
import dk.alexandra.fresco.suite.mdsonic.MdsonicBuilder;
import dk.alexandra.fresco.suite.mdsonic.MdsonicProtocolSuite;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;
import dk.alexandra.fresco.suite.mdsonic.protocols.computations.MdsonicArithmeticMacCheckComputation;
import dk.alexandra.fresco.suite.mdsonic.protocols.computations.MdsonicBooleanMacCheckComputation;
import dk.alexandra.fresco.suite.mdsonic.protocols.computations.MdsonicMacCheckComputation;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;
import dk.alexandra.fresco.suite.mdsonic.protocols.natives.RequiresMacCheck;

import java.util.stream.StreamSupport;

/**
 * Round synchronization for Mdsonic. <p>Requires a mac check to be performed on an all opened
 * unauthenticated values whenever an output protocol is encountered in a batch.</p>
 */
public class MdsonicRoundSynchronization<
    HighT extends MdsonicUInt<HighT>,
    LowT extends MdsonicUInt<LowT>,
    PlainT extends MdsonicCompUInt<HighT, LowT, PlainT>, SecretP extends MdsonicGF<SecretP>>
    implements RoundSynchronization<MdsonicResourcePool<PlainT, SecretP>> {

  private static final int OPEN_VALUE_THRESHOLD = 1000000;
  private final int openValueThreshold;
  private final int batchSize;
  private boolean isCheckRequired;
  private final MdsonicProtocolSuite<HighT, LowT, PlainT, SecretP> protocolSuite;

  public MdsonicRoundSynchronization(MdsonicProtocolSuite<HighT, LowT, PlainT, SecretP> protocolSuite) {
    this(protocolSuite, OPEN_VALUE_THRESHOLD, 128);
  }

  public MdsonicRoundSynchronization(MdsonicProtocolSuite<HighT, LowT, PlainT, SecretP> protocolSuite,
                                     int openValueThreshold,
                                     int batchSize) {
    this.protocolSuite = protocolSuite;
    this.openValueThreshold = openValueThreshold;
    this.batchSize = batchSize;
    this.isCheckRequired = false;
  }

  private void doArithmeticMacCheck(MdsonicResourcePool<PlainT, SecretP> resourcePool, Network network) {
    MdsonicBuilder<PlainT> builder = new MdsonicBuilder<>(resourcePool.getFactory(),
        protocolSuite.createBasicNumericContext(resourcePool),
        protocolSuite.createRealNumericContext(),
        false);
    BatchEvaluationStrategy<MdsonicResourcePool<PlainT, SecretP>> batchStrategy = new BatchedStrategy<>();
    BatchedProtocolEvaluator<MdsonicResourcePool<PlainT, SecretP>> evaluator = new BatchedProtocolEvaluator<>(
        batchStrategy,
        protocolSuite,
        batchSize);
    OpenedValueStore<MdsonicASIntArithmetic<PlainT>, PlainT> store = resourcePool.getOpenedValueStore();
    MdsonicArithmeticMacCheckComputation<HighT, LowT, PlainT, SecretP> macCheck = new MdsonicArithmeticMacCheckComputation<>(
        store.popValues(),
        resourcePool, resourcePool::createRandomGenerator, resourcePool.getDrbgSeedBitLength());
    ProtocolBuilderNumeric sequential = builder.createSequential();
    macCheck.buildComputation(sequential);
    evaluator.eval(sequential.build(), resourcePool, network);
  }

  private void doBooleanMacCheck(MdsonicResourcePool<PlainT, SecretP> resourcePool, Network network) {
//    System.out.println("Do Boolean MAC Check");
    MdsonicBuilder<PlainT> builder = new MdsonicBuilder<>(resourcePool.getFactory(),
            protocolSuite.createBasicNumericContext(resourcePool),
            protocolSuite.createRealNumericContext(),
            false);
    BatchEvaluationStrategy<MdsonicResourcePool<PlainT, SecretP>> batchStrategy = new BatchedStrategy<>();
    BatchedProtocolEvaluator<MdsonicResourcePool<PlainT, SecretP>> evaluator = new BatchedProtocolEvaluator<>(
            batchStrategy,
            protocolSuite,
            batchSize);
    OpenedValueStore<MdsonicASBoolBoolean<SecretP>, Boolean> booleanStore = resourcePool.getOpenedBooleanValueStore();
    MdsonicBooleanMacCheckComputation<HighT, LowT, PlainT, SecretP> macCheck = new MdsonicBooleanMacCheckComputation<>(
            booleanStore.popValues(),
            resourcePool, resourcePool::createRandomGenerator, resourcePool.getDrbgSeedBitLength());
    ProtocolBuilderNumeric sequential = builder.createSequential();
    macCheck.buildComputation(sequential);
    evaluator.eval(sequential.build(), resourcePool, network);
  }

  private void doBothMacCheck(MdsonicResourcePool<PlainT, SecretP> resourcePool, Network network) {
//    System.out.println("Do Both MAC Check");
    MdsonicBuilder<PlainT> builder = new MdsonicBuilder<>(resourcePool.getFactory(),
            protocolSuite.createBasicNumericContext(resourcePool),
            protocolSuite.createRealNumericContext(),
            false);
    BatchEvaluationStrategy<MdsonicResourcePool<PlainT, SecretP>> batchStrategy = new BatchedStrategy<>();
    BatchedProtocolEvaluator<MdsonicResourcePool<PlainT, SecretP>> evaluator = new BatchedProtocolEvaluator<>(
            batchStrategy,
            protocolSuite,
            batchSize * 2);
    OpenedValueStore<MdsonicASIntArithmetic<PlainT>, PlainT> store = resourcePool.getOpenedValueStore();
    OpenedValueStore<MdsonicASBoolBoolean<SecretP>, Boolean> booleanStore = resourcePool.getOpenedBooleanValueStore();
    Application<Void, ProtocolBuilderNumeric> app =
            producer -> producer.par(par -> {
              par.seq(new MdsonicBooleanMacCheckComputation<>(
                      booleanStore.popValues(),
                      resourcePool, resourcePool::createRandomGenerator, resourcePool.getDrbgSeedBitLength()));
              par.seq(new MdsonicArithmeticMacCheckComputation<>(
                      store.popValues(),
                      resourcePool, resourcePool::createRandomGenerator, resourcePool.getDrbgSeedBitLength()));
              return null;
            });
    ProtocolBuilderNumeric sequential = builder.createSequential();
    app.buildComputation(sequential);
    evaluator.eval(sequential.build(), resourcePool, network);
  }

  private void doMacCheck(MdsonicResourcePool<PlainT, SecretP> resourcePool, Network network) {
    MdsonicBuilder<PlainT> builder = new MdsonicBuilder<>(resourcePool.getFactory(),
            protocolSuite.createBasicNumericContext(resourcePool),
            protocolSuite.createRealNumericContext(),
            false);
    BatchEvaluationStrategy<MdsonicResourcePool<PlainT, SecretP>> batchStrategy = new BatchedStrategy<>();
    BatchedProtocolEvaluator<MdsonicResourcePool<PlainT, SecretP>> evaluator = new BatchedProtocolEvaluator<>(
            batchStrategy,
            protocolSuite,
            batchSize * 2);
    OpenedValueStore<MdsonicASIntArithmetic<PlainT>, PlainT> store = resourcePool.getOpenedValueStore();
    OpenedValueStore<MdsonicASBoolBoolean<SecretP>, Boolean> booleanStore = resourcePool.getOpenedBooleanValueStore();
    MdsonicMacCheckComputation<HighT, LowT, PlainT, SecretP> macCheck;
    if (store.hasPendingValues() && booleanStore.hasPendingValues()) {
      macCheck = new MdsonicMacCheckComputation<>(
              store.popValues(),
              booleanStore.popValues(),
              resourcePool, resourcePool::createRandomGenerator, resourcePool.getDrbgSeedBitLength()
      );
    } else if (store.hasPendingValues()) {
      macCheck = new MdsonicMacCheckComputation<>(
              store.popValues(),
              null,
              resourcePool, resourcePool::createRandomGenerator, resourcePool.getDrbgSeedBitLength()
      );
    } else if (booleanStore.hasPendingValues()) {
      macCheck = new MdsonicMacCheckComputation<>(
              null,
              booleanStore.popValues(),
              resourcePool, resourcePool::createRandomGenerator, resourcePool.getDrbgSeedBitLength()
      );
    } else {
      return;
    }

    ProtocolBuilderNumeric sequential = builder.createSequential();
    macCheck.buildComputation(sequential);
    evaluator.eval(sequential.build(), resourcePool, network);
  }

  @Override
  public void finishedBatch(int gatesEvaluated, MdsonicResourcePool<PlainT, SecretP> resourcePool,
      Network network) {
    OpenedValueStore<MdsonicASIntArithmetic<PlainT>, PlainT> store = resourcePool.getOpenedValueStore();
    OpenedValueStore<MdsonicASBoolBoolean<SecretP>, Boolean> booleanStore = resourcePool.getOpenedBooleanValueStore();

    // old method
    if (isCheckRequired && store.hasPendingValues() && booleanStore.hasPendingValues()) {
      doBothMacCheck(resourcePool, network);
      isCheckRequired = false;
    } else if (isCheckRequired && store.hasPendingValues()) {
      doArithmeticMacCheck(resourcePool, network);
      isCheckRequired = false;
    } else if (isCheckRequired && booleanStore.hasPendingValues()) {
      doBooleanMacCheck(resourcePool, network);
      isCheckRequired = false;
    } else if (store.exceedsThreshold(openValueThreshold) && booleanStore.exceedsThreshold(openValueThreshold)) {
      doBothMacCheck(resourcePool, network);
      isCheckRequired = false;
    } else if (store.exceedsThreshold(openValueThreshold)) {
      doArithmeticMacCheck(resourcePool, network);
      isCheckRequired = false;
    } else if (booleanStore.exceedsThreshold(openValueThreshold)) {
      doBooleanMacCheck(resourcePool, network);
      isCheckRequired = false;
    }

//    // new method
//    if (isCheckRequired || store.exceedsThreshold(openValueThreshold) || booleanStore.exceedsThreshold(openValueThreshold)) {
//      doMacCheck(resourcePool, network);
//      isCheckRequired = false;
//    }

//    if (store.exceedsThreshold(openValueThreshold) && booleanStore.exceedsThreshold(openValueThreshold)) {
//      doBothMacCheck(resourcePool, network);
//    } else if (store.exceedsThreshold(openValueThreshold)) {
//      doArithmeticMacCheck(resourcePool, network);
//    } else if (booleanStore.exceedsThreshold(openValueThreshold)) {
//      doBooleanMacCheck(resourcePool, network);
//    }


  }

  @Override
  public void finishedEval(MdsonicResourcePool<PlainT, SecretP> resourcePool, Network network) {
    OpenedValueStore<MdsonicASIntArithmetic<PlainT>, PlainT> store = resourcePool.getOpenedValueStore();
    OpenedValueStore<MdsonicASBoolBoolean<SecretP>, Boolean> booleanStore = resourcePool.getOpenedBooleanValueStore();

    // old method
    if (store.hasPendingValues() && booleanStore.hasPendingValues()) {
      doBothMacCheck(resourcePool, network);
    } else if (store.hasPendingValues()) {
      doArithmeticMacCheck(resourcePool, network);
    } else if (booleanStore.hasPendingValues()) {
      doBooleanMacCheck(resourcePool, network);
    }

//    // new method
//    if (store.hasPendingValues() || booleanStore.hasPendingValues()) {
//      doMacCheck(resourcePool, network);
//    }
  }

  @Override
  public void beforeBatch(
      ProtocolCollection<MdsonicResourcePool<PlainT, SecretP>> nativeProtocols,
      MdsonicResourcePool<PlainT, SecretP> resourcePool, Network network) {
    this.isCheckRequired = StreamSupport.stream(nativeProtocols.spliterator(), false)
        .anyMatch(p -> p instanceof RequiresMacCheck);
//    System.out.println(outputFound + ";" + System.currentTimeMillis());
    OpenedValueStore<MdsonicASIntArithmetic<PlainT>, PlainT> store = resourcePool.getOpenedValueStore();
    if (store.hasPendingValues() && this.isCheckRequired) {
//      System.out.println("Because of output");
      doMacCheck(resourcePool, network);
    }
  }

}
