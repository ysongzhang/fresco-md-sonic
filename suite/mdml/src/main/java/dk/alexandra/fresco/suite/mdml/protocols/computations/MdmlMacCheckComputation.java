package dk.alexandra.fresco.suite.mdml.protocols.computations;

import dk.alexandra.fresco.commitment.HashBasedCommitment;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUIntFactory;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlASIntArithmetic;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlUInt;
import dk.alexandra.fresco.suite.mdml.resource.MdmlResourcePool;
import dk.alexandra.fresco.suite.mdml.resource.storage.MdmlDataSupplier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Computation for performing batched mac-check on all currently opened, unchecked values.
 */
public class MdmlMacCheckComputation<
    HighT extends MdmlUInt<HighT>,
    LowT extends MdmlUInt<LowT>,
    PlainT extends MdmlCompUInt<HighT, LowT, PlainT>>
    implements Computation<Void, ProtocolBuilderNumeric> {

  private static int COUNT = 0;

  private final ByteSerializer<PlainT> serializer;
  private final MdmlDataSupplier<PlainT> supplier;
  private final MdmlCompUIntFactory<PlainT> factory;
  private final List<MdmlASIntArithmetic<PlainT>> authenticatedElements;
  private final List<PlainT> openValues;
  private ByteSerializer<HashBasedCommitment> commitmentSerializer;
  private final int noOfParties;
  private final Drbg localDrbg;

  private final Function<byte[], Drbg> jointDrbgSupplier;

  private final int drbgByteLength;

  /**
   * Creates new {@link MdmlMacCheckComputation}.
   *
   * @param toCheck authenticated elements and open values that must be checked
   * @param resourcePool resources for running Mdml
   */
  public MdmlMacCheckComputation(Pair<List<MdmlASIntArithmetic<PlainT>>, List<PlainT>> toCheck,
                                 MdmlResourcePool<PlainT> resourcePool,
                                 final Function<byte[], Drbg> jointDrbgSupplier, final int drbgSeedBitLength) {
    this.authenticatedElements = toCheck.getFirst();
    this.openValues = toCheck.getSecond();
    this.serializer = resourcePool.getPlainSerializer();
    this.supplier = resourcePool.getDataSupplier();
    this.factory = resourcePool.getFactory();
    this.commitmentSerializer = resourcePool.getCommitmentSerializer();
    this.noOfParties = resourcePool.getNoOfParties();
    this.localDrbg = resourcePool.getLocalRandomGenerator();
    this.jointDrbgSupplier = jointDrbgSupplier;
    this.drbgByteLength = drbgSeedBitLength / 8;
  }

  @Override
  public DRes<Void> buildComputation(ProtocolBuilderNumeric builder) {
    PlainT macKeyShare = supplier.getSecretSharedKey();
//    System.out.println("Mdml Mac Check " + COUNT++ + " "+ openValues.size());
    return builder
            .seq(new CoinTossingComputation(drbgByteLength, commitmentSerializer, noOfParties, localDrbg))
            .seq((seq, seed) -> {
              Drbg jointDrbg = jointDrbgSupplier.apply(seed);
              List<PlainT> randomCoefficients = sampleCoefficients(jointDrbg, factory, openValues.size());
              PlainT y = MdmlUInt.innerProduct(openValues, randomCoefficients);
              List<PlainT> macShares = authenticatedElements.stream()
                      .map(MdmlASIntArithmetic::getMacShare)
                      .collect(Collectors.toList());
              PlainT mj = MdmlUInt.innerProduct(macShares, randomCoefficients);
              PlainT zj = mj.subtract(macKeyShare.multiply(y));

              // Commit to z and open it afterwards
              return seq.seq(new CommitmentComputationMdml(commitmentSerializer, serializer.serialize(zj),
                      noOfParties, localDrbg));
            }).seq((seq, commitZjs) -> {
              List<PlainT> elements = serializer.deserializeList(commitZjs);
              PlainT sum = MdmlUInt.sum(elements);
              if (!sum.isZero()) {
                throw new MaliciousException("Mac check failed");
              }
              authenticatedElements.clear();
              openValues.clear();
              return null;
            });
  }

  /**
   * Samples random coefficients for mac-check using joint source of randomness.
   * Fixed bug: Invoke coin tossing protocol to get random seed real time.
   */
  private List<PlainT> sampleCoefficients(Drbg drbg, MdmlCompUIntFactory<PlainT> factory,
      int numCoefficients) {

    List<PlainT> randomCoefficients = new ArrayList<>(numCoefficients);
    for (int i = 0; i < numCoefficients; i++) {
      byte[] bytes = new byte[factory.getHighBitLength() / Byte.SIZE];
      drbg.nextBytes(bytes);
      randomCoefficients.add(factory.createFromBytes(bytes));
    }
    return randomCoefficients;
  }
}
