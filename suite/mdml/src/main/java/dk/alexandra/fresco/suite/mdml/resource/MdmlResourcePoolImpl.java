package dk.alexandra.fresco.suite.mdml.resource;

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
import dk.alexandra.fresco.suite.mdml.MdmlBuilder;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUIntFactory;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlASIntArithmetic;
import dk.alexandra.fresco.suite.mdml.protocols.computations.CoinTossingComputation;
import dk.alexandra.fresco.suite.mdml.resource.storage.MdmlDataSupplier;

import java.io.Closeable;
import java.math.BigInteger;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Default implementation of {@link MdmlResourcePool}. <p>If a securely generated, joint random
 * seed is needed, {@link #initializeJointRandomness(Supplier, Function, int)} must be called before
 * using this class.</p>
 */
public class MdmlResourcePoolImpl<PlainT extends MdmlCompUInt<?, ?, PlainT>>
    extends ResourcePoolImpl
    implements MdmlResourcePool<PlainT> {

  private static final int DRBG_SEED_LENGTH = 256;  // 32 Byte
  private final int effectiveBitLength;
  private final BigInteger modulus;
  private final OpenedValueStore<MdmlASIntArithmetic<PlainT>, PlainT> storage;
  private final MdmlDataSupplier<PlainT> supplier;
  private final MdmlCompUIntFactory<PlainT> factory;
  private final ByteSerializer<PlainT> rawSerializer;
  private final Drbg localDrbg;
  private Drbg drbg;
  private final Function<byte[], Drbg> drbgSupplier;
  private final int drbgSeedBitLength;

  /**
   * Creates new {@link MdmlResourcePoolImpl}.
   */
  public MdmlResourcePoolImpl(int myId, int noOfPlayers, Drbg drbg,
                              OpenedValueStore<MdmlASIntArithmetic<PlainT>, PlainT> storage,
                              MdmlDataSupplier<PlainT> supplier, MdmlCompUIntFactory<PlainT> factory, Function<byte[], Drbg> drbgSupplier, int drbgSeedBitLength) {
    super(myId, noOfPlayers);
    Objects.requireNonNull(storage);
    Objects.requireNonNull(supplier);
    Objects.requireNonNull(factory);
    this.effectiveBitLength = factory.getLowBitLength();
    this.modulus = BigInteger.ONE.shiftLeft(effectiveBitLength);
    this.storage = storage;
    this.supplier = supplier;
    this.factory = factory;
    this.rawSerializer = factory.createSerializer();
    this.drbg = drbg;  // jointDrbg using coin tossing protocol
    this.localDrbg = new AesCtrDrbg();
    this.drbgSupplier = drbgSupplier;
    this.drbgSeedBitLength = drbgSeedBitLength;
  }

  public MdmlResourcePoolImpl(int myId, int noOfPlayers, Drbg drbg,
                              OpenedValueStore<MdmlASIntArithmetic<PlainT>, PlainT> storage, MdmlDataSupplier<PlainT> supplier, MdmlCompUIntFactory<PlainT> factory, Function<byte[], Drbg> drbgSupplier) {
    this(myId, noOfPlayers, drbg, storage, supplier, factory, drbgSupplier, DRBG_SEED_LENGTH);
  }

  @Override
  public int getMaxBitLength() {
    return effectiveBitLength;
  }

  @Override
  public OpenedValueStore<MdmlASIntArithmetic<PlainT>, PlainT> getOpenedValueStore() {
    return storage;
  }

  @Override
  public MdmlDataSupplier<PlainT> getDataSupplier() {
    return supplier;
  }

  @Override
  public MdmlCompUIntFactory<PlainT> getFactory() {
    return factory;
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
    BuilderFactoryNumeric builderFactory = new MdmlBuilder<>(factory,
        new BasicNumericContext(effectiveBitLength, modulus,
            getMyId(), getNoOfParties()), null);
    ProtocolBuilderNumeric root = builderFactory.createSequential();
    DRes<byte[]> jointSeed = coinTossing
        .buildComputation(root);
    ProtocolProducer coinTossingProducer = root.build();
    do {
      ProtocolCollectionList<MdmlResourcePool> protocolCollectionList =
          new ProtocolCollectionList<>(
              128); // batch size is irrelevant since this is a very light-weight protocol
      coinTossingProducer.getNextProtocols(protocolCollectionList);
      new BatchedStrategy<MdmlResourcePool>()
          .processBatch(protocolCollectionList, this, networkBatchDecorator);
    } while (coinTossingProducer.hasNextProtocols());
    return jointSeed.out();
  }

}
