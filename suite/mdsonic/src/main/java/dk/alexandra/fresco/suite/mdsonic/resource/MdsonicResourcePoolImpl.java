package dk.alexandra.fresco.suite.mdsonic.resource;

import dk.alexandra.fresco.commitment.HashBasedCommitmentSerializer;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.NetworkBatchDecorator;
import dk.alexandra.fresco.framework.sce.evaluator.ProtocolCollectionList;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.suite.mdsonic.MdsonicBuilder;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;
import dk.alexandra.fresco.suite.mdsonic.protocols.computations.CoinTossingComputation;
import dk.alexandra.fresco.suite.mdsonic.resource.storage.MdsonicDataSupplier;

import java.io.Closeable;
import java.math.BigInteger;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Default implementation of {@link MdsonicResourcePool}. <p>If a securely generated, joint random
 * seed is needed, {@link #initializeJointRandomness(Supplier, Function, int)} must be called before
 * using this class.</p>
 */
public class MdsonicResourcePoolImpl<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>>
    extends ResourcePoolImpl
    implements MdsonicResourcePool<PlainT, SecretP> {

  private static final int DRBG_SEED_LENGTH = 256;  // 32 Byte
  private final int effectiveBitLength;
  private final BigInteger modulus;
  private final OpenedValueStore<MdsonicASIntArithmetic<PlainT>, PlainT> storage;
  private final OpenedValueStore<MdsonicASBoolBoolean<SecretP>, Boolean> storageBoolean;
  private final MdsonicDataSupplier<PlainT, SecretP> supplier;
  private final MdsonicCompUIntFactory<PlainT> factory;
  private final MdsonicGFFactory<SecretP> booleanFactory;
  private final ByteSerializer<PlainT> rawSerializer;
  private final Drbg localDrbg;
  private Drbg drbg;
  private final Function<byte[], Drbg> drbgSupplier;
  private final int drbgSeedBitLength;

  /**
   * Creates new {@link MdsonicResourcePoolImpl}.
   */
  public MdsonicResourcePoolImpl(int myId, int noOfPlayers, Drbg drbg,
                                 OpenedValueStore<MdsonicASIntArithmetic<PlainT>, PlainT> storage,
                                 OpenedValueStore<MdsonicASBoolBoolean<SecretP>, Boolean> storageBoolean,
                                 MdsonicDataSupplier<PlainT, SecretP> supplier,
                                 MdsonicCompUIntFactory<PlainT> factory, MdsonicGFFactory<SecretP> booleanFactory,
                                 Function<byte[], Drbg> drbgSupplier, int drbgSeedBitLength) {
    super(myId, noOfPlayers);
    Objects.requireNonNull(storage);
    Objects.requireNonNull(supplier);
    Objects.requireNonNull(factory);
    this.effectiveBitLength = factory.getLowBitLength();
    this.modulus = BigInteger.ONE.shiftLeft(effectiveBitLength);
    this.storage = storage;
    this.storageBoolean = storageBoolean;
    this.supplier = supplier;
    this.factory = factory;
    this.booleanFactory = booleanFactory;
    this.rawSerializer = factory.createSerializer();
    this.drbg = drbg;  // jointDrbg using coin tossing protocol
    this.localDrbg = new AesCtrDrbg();
    this.drbgSupplier = drbgSupplier;
    this.drbgSeedBitLength = drbgSeedBitLength;
  }

  public MdsonicResourcePoolImpl(int myId, int noOfPlayers, Drbg drbg,
                                 OpenedValueStore<MdsonicASIntArithmetic<PlainT>, PlainT> storage,
                                 OpenedValueStore<MdsonicASBoolBoolean<SecretP>, Boolean> storageBoolean,
                                 MdsonicDataSupplier<PlainT, SecretP> supplier,
                                 MdsonicCompUIntFactory<PlainT> factory,
                                 MdsonicGFFactory<SecretP> booleanFactory,
                                 Function<byte[], Drbg> drbgSupplier) {
    this(myId, noOfPlayers, drbg, storage, storageBoolean, supplier, factory, booleanFactory, drbgSupplier, DRBG_SEED_LENGTH);
  }

  @Override
  public int getMaxBitLength() {
    return effectiveBitLength;
  }

  @Override
  public OpenedValueStore<MdsonicASIntArithmetic<PlainT>, PlainT> getOpenedValueStore() {
    return storage;
  }

  @Override
  public OpenedValueStore<MdsonicASBoolBoolean<SecretP>, Boolean> getOpenedBooleanValueStore() {
    return storageBoolean;
  }

  @Override
  public MdsonicDataSupplier<PlainT, SecretP> getDataSupplier() {
    return supplier;
  }

  @Override
  public MdsonicCompUIntFactory<PlainT> getFactory() {
    return factory;
  }

  @Override
  public MdsonicGFFactory<SecretP> getBooleanFactory() {
    return booleanFactory;
  }

  @Override
  public ByteSerializer<PlainT> getPlainSerializer() {
    return rawSerializer;
  }

  @Override
  public void initializeJointRandomness(Supplier<Network> networkSupplier,
      Function<byte[], Drbg> drbgGenerator, int seedLength) {
    Network network = networkSupplier.get();
    Computation<byte[], ProtocolBuilderNumeric> coinTossing =
        new CoinTossingComputation(seedLength, new HashBasedCommitmentSerializer(),
            getNoOfParties(), getLocalRandomGenerator());
    byte[] jointSeed = runCoinTossing(coinTossing, network);
    drbg = drbgGenerator.apply(jointSeed);
    ExceptionConverter.safe(() -> {
      ((Closeable) network).close();
      return null;
    }, "Failed to close network");
  }

  @Override
  public BigInteger getModulus() {
    return modulus;
  }

  @Override
  public ByteSerializer<BigInteger> getSerializer() {
    throw new UnsupportedOperationException("This suite does not support serializing big integers");
  }

  @Override
  public Drbg getRandomGenerator() {
    if (drbg == null) {
      throw new IllegalStateException("Joint drbg must be initialized before use");
    }
    return drbg;
  }

  @Override
  public Drbg getLocalRandomGenerator() {
    return localDrbg;
  }

  public int getDrbgSeedBitLength() {
    return drbgSeedBitLength;
  }

  public Drbg createRandomGenerator(byte[] seed) {
    return drbgSupplier.apply(seed);
  }

  /**
   * Evaluates, on the fly, a coin-tossing computation to get joint seed.
   */
  private byte[] runCoinTossing(Computation<byte[], ProtocolBuilderNumeric> coinTossing,
      Network network) {
    NetworkBatchDecorator networkBatchDecorator =
        new NetworkBatchDecorator(
            this.getNoOfParties(),
            network);
    BuilderFactoryNumeric builderFactory = new MdsonicBuilder<>(factory,
        new BasicNumericContext(effectiveBitLength, modulus,
            getMyId(), getNoOfParties()), null,false);
    ProtocolBuilderNumeric root = builderFactory.createSequential();
    DRes<byte[]> jointSeed = coinTossing
        .buildComputation(root);
    ProtocolProducer coinTossingProducer = root.build();
    do {
      ProtocolCollectionList<MdsonicResourcePool> protocolCollectionList =
          new ProtocolCollectionList<>(
              128); // batch size is irrelevant since this is a very light-weight protocol
      coinTossingProducer.getNextProtocols(protocolCollectionList);
      new BatchedStrategy<MdsonicResourcePool>()
          .processBatch(protocolCollectionList, this, networkBatchDecorator);
    } while (coinTossingProducer.hasNextProtocols());
    return jointSeed.out();
  }

}
