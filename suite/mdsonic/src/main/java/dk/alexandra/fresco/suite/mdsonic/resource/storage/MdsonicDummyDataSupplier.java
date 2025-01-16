package dk.alexandra.fresco.suite.mdsonic.resource.storage;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.mdsonic.AdvancedNumericMdsonic.MatrixTruncationPair;
import dk.alexandra.fresco.framework.builder.numeric.mdsonic.AdvancedNumericMdsonic.TruncationPair;
import dk.alexandra.fresco.framework.builder.numeric.mdsonic.DaBit;
import dk.alexandra.fresco.framework.builder.numeric.mdsonic.FMedaBit;
import dk.alexandra.fresco.framework.util.*;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Insecure implementation of {@link MdsonicDataSupplier}. <p>This class deterministically generates
 * pre-processing material for each party and can therefore not be used in production.</p>
 */
public class MdsonicDummyDataSupplier<
    PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>> implements
        MdsonicDataSupplier<PlainT, SecretP> {

  private final int myId;
  private final ArithmeticDummyDataSupplier supplier;
  private final PlainT secretSharedKey;
  private final PlainT myKeyShare;
  private final MdsonicCompUIntFactory<PlainT> factory;
  private final SecretP secretSharedKeyBoolean;
  private final SecretP myKeyShareBoolean;
  private final MdsonicGFFactory<SecretP> factoryBoolean;

  public MdsonicDummyDataSupplier(int myId, int noOfParties, PlainT secretSharedKey,
                                  MdsonicCompUIntFactory<PlainT> factory, MdsonicGFFactory<SecretP> factoryBoolean) {
    this.myId = myId;
    this.factory = factory;
    this.factoryBoolean = factoryBoolean;

    byte[] seed = new byte[32];
    seed[0] = 42;
    BytePrg bytePrg = new BytePrg(seed);

    this.supplier = new ArithmeticDummyDataSupplier(
        myId,
        noOfParties,
        BigInteger.ONE.shiftLeft(factory.getCompositeBitLength()),
            BigInteger.ONE.shiftLeft(factory.getLowBitLength() - 1),
            factoryBoolean.getBitLength(), bytePrg);
    final Pair<BigInteger, BigInteger> keyPair = supplier.getRandomMACKeyShare();
    this.secretSharedKey = factory.createFromBigInteger(keyPair.getFirst());
    this.myKeyShare = factory.createFromBigInteger(keyPair.getSecond());

    // tinyOT
    final Pair<StrictBitVector, StrictBitVector> keyPairBoolean = supplier.getRandomMACKeyShareBoolean();
    this.secretSharedKeyBoolean = factoryBoolean.createFromStrictVector(keyPairBoolean.getFirst());
    this.myKeyShareBoolean = factoryBoolean.createFromStrictVector(keyPairBoolean.getSecond());
  }

  @Override
  public MdsonicTriple<PlainT, MdsonicASIntArithmetic<PlainT>> getNextTripleSharesFull() {
    MultiplicationTripleShares rawTriple = supplier.getMultiplicationTripleShares();
    return new MdsonicTriple<>(
        toMdsonicASInt(rawTriple.getLeft()),
        toMdsonicASInt(rawTriple.getRight()),
        toMdsonicASInt(rawTriple.getProduct()));
  }

  public MdsonicASIntArithmetic<PlainT> getNextTripleProductShare() {
    Pair<BigInteger, BigInteger> product = supplier.getNextTripleProductShare();
    return toMdsonicASInt(product);
  }

  @Override
  public MdsonicBitTriple<SecretP, MdsonicASBoolBoolean<SecretP>> getNextBitTripleShares() {
    MultiplicationTripleShares rawTriple = supplier.getMultiplicationBitTripleShares();
    return new MdsonicBitTriple<>(
        toMdsonicASBool(rawTriple.getLeft()),
        toMdsonicASBool(rawTriple.getRight()),
        toMdsonicASBool(rawTriple.getProduct()));
  }

  @Override
  public MdsonicWRBitTriple<SecretP, MdsonicASBoolBoolean<SecretP>> getNextWRBitTripleShares() {
    Pair<MultiplicationTripleShares, MultiplicationTripleShares> rawTriples = supplier.getMultiplicationWRBitTripleShares();
    return new MdsonicWRBitTriple<>(
            toMdsonicASBool(rawTriples.getFirst().getLeft()),
            toMdsonicASBool(rawTriples.getFirst().getRight()),
            toMdsonicASBool(rawTriples.getSecond().getRight()),
            toMdsonicASBool(rawTriples.getFirst().getProduct()),
            toMdsonicASBool(rawTriples.getSecond().getProduct()));
  }

  public MdsonicASBoolBoolean<SecretP> getNextBitTripleProductShare() {
    Pair<BigInteger, BigInteger> product = supplier.getNextBitTripleProductShare();
    return toMdsonicASBool(product);
  }

  @Override
  public MdsonicInputMask<PlainT> getNextInputMask(int towardPlayerId) {
    Pair<BigInteger, BigInteger> raw = supplier.getRandomElementShare();
    if (myId == towardPlayerId) {
      return new MdsonicInputMask<>(toMdsonicASInt(raw),
          factory.zero());
    } else {
      return new MdsonicInputMask<>(toMdsonicASInt(raw));
    }
  }

  public MdsonicASIntArithmetic<PlainT> getNextBitShareAsArithmetic() {
    return toMdsonicASInt(supplier.getRandomBitShare());
  }

  @Override
  public MdsonicASBoolBoolean<SecretP> getNextBitShare() {
    return toMdsonicASBool(supplier.getRandomBitShare());
  }

  @Override
  public PlainT getSecretSharedKey() {
    return myKeyShare;
  }

  @Override
  public SecretP getSecretSharedKeyBoolean() {
    return myKeyShareBoolean;
  }

  @Override
  public MdsonicASIntArithmetic<PlainT> getNextRandomElementShare() {
    return toMdsonicASInt(supplier.getRandomElementShare());
  }

  @Override
  public TruncationPair getNextTruncationPair(int d) {
    TruncationPairShares pair = supplier.getTruncationPairShares(d);
    return new TruncationPair(toMdsonicASInt(pair.getRPrime()), toMdsonicASInt(pair.getR()));
  }

  @Override
  public MatrixTruncationPair getNextMatrixTruncationPair(int d, int height, int width) {
    Matrix<TruncationPair> truncationPairMatrix = new Matrix<>(height, width, i -> {
      ArrayList<TruncationPair> row = new ArrayList<>(width);
      for (int j = 0; j < width; j++) {
        TruncationPairShares pair = supplier.getTruncationPairShares(d);
        row.add(new TruncationPair(toMdsonicASInt(pair.getRPrime()), toMdsonicASInt(pair.getR())));
      }
      return row;
    });
    Matrix<DRes<SInt>> rPrimeMatrix = new Matrix<>(height, width, i -> {
      ArrayList<DRes<SInt>> row = new ArrayList<>(width);
      for (int j = 0; j < width; j++) {
        row.add(truncationPairMatrix.getRow(i).get(j).getRPrime());
      }
      return row;
    });
    Matrix<DRes<SInt>> rMatrix = new Matrix<>(height, width, i -> {
      ArrayList<DRes<SInt>> row = new ArrayList<>(width);
      for (int j = 0; j < width; j++) {
        row.add(truncationPairMatrix.getRow(i).get(j).getR());
      }
      return row;
    });
    return new MatrixTruncationPair(rPrimeMatrix, rMatrix);
  }

  public DaBit getNextMaskedDaBit() {
    MdsonicASBoolBoolean<SecretP> bitA = toMdsonicASBool(supplier.getRandomBitShare());
    MdsonicMSIntArithmetic<PlainT> bitB = toMdsonicMSInt(supplier.getRandomBitShare());
    return new DaBit(bitA, bitB);
  }

  public DaBit getNextDaBit() {
    MdsonicASBoolBoolean<SecretP> bitA = toMdsonicASBool(supplier.getRandomBitShare());
    MdsonicASIntArithmetic<PlainT> bitB = toMdsonicASInt(supplier.getRandomBitShare());
    return new DaBit(bitA, bitB);
  }

  public FMedaBit getNextFMedaBit(int l, int d) {
    if (l > factory.getLowBitLength()) {
      throw new IllegalArgumentException(
              "Flexible masked length " + l + " must be less than " + (factory.getLowBitLength()+1));
    }
    if (d != 2 && d != 4) {
      throw new IllegalArgumentException(
              "Flexible masked degree " + d + " must be 2 or 4.");
    }
    MdsonicASIntArithmetic<PlainT> value = toMdsonicASInt(supplier.getRandomElementShare());
    List<DRes<SBool>> valueBooleanList = new ArrayList<>(factory.getLowBitLength());
    for (int i = 0; i < factory.getLowBitLength(); i++) {
      valueBooleanList.add(toMdsonicASBool(supplier.getRandomBitShare()));
    }
    List<DRes<SBool>> valueProductList;

    if (d == 2) {
      valueProductList = new ArrayList<>(l / 2);
      for (int i = 0; i < l / 2; i++) {
        valueProductList.add(toMdsonicASBool(supplier.getRandomBitShare()));
      }
    } else {
      valueProductList = new ArrayList<>(11 * (l / 4) + 1 * 4);
      for (int i = 0; i < 11 * (l / 4) + 1 * 4; i++) {
        valueProductList.add(toMdsonicASBool(supplier.getRandomBitShare()));
      }
    }

    PlainT openedValue = factory.zero();
    return new FMedaBit(value, factory.getLowBitLength(), d, l, valueBooleanList, valueProductList, openedValue);
  }

  public FMedaBit getNextEdaBit() {
    MdsonicASIntArithmetic<PlainT> value = toMdsonicASInt(supplier.getRandomElementShare());
    List<DRes<SBool>> valueASSBooleanList = new ArrayList<>(factory.getLowBitLength());
    for (int i = 0; i < factory.getLowBitLength(); i++) {
      valueASSBooleanList.add(toMdsonicASBool(supplier.getRandomBitShare()));
    }
    return new FMedaBit(value, factory.getLowBitLength(), valueASSBooleanList);
  }

  public MdsonicASIntArithmetic<PlainT> getNextInnerProductShare() {
    Pair<BigInteger, BigInteger> product = supplier.getNextInnerProductShare();
    return toMdsonicASInt(product);
  }

  public Matrix<MdsonicASIntArithmetic<PlainT>> getNextMatrixProductShare(int height, int width) {
    return new Matrix<>(height, width, i -> {
      ArrayList<MdsonicASIntArithmetic<PlainT>> row = new ArrayList<>(width);
      for (int j = 0; j < width; j++) {
        row.add(toMdsonicASInt(supplier.getNextMatrixProductShare()));
      }
      return row;
    });
  }

  @Override
  public MdsonicMatrixTriple<PlainT, MdsonicASIntArithmetic<PlainT>> getNextMatrixTripleShares(int n1, int n2, int n3) {
    Matrix<MdsonicASIntArithmetic<PlainT>> left = getNextMatrixProductShare(n1, n2);
    Matrix<MdsonicASIntArithmetic<PlainT>> right = getNextMatrixProductShare(n2, n3);
    Matrix<MdsonicASIntArithmetic<PlainT>> product = getNextMatrixProductShare(n1, n3);
    return new MdsonicMatrixTriple<>(left, right, product);
  }


  private MdsonicASIntArithmetic<PlainT> toMdsonicASInt(Pair<BigInteger, BigInteger> raw) {
    PlainT share = factory.createFromBigInteger(raw.getSecond());
    PlainT macShare = share.multiply(secretSharedKey);
    return new MdsonicASIntArithmetic<>(share, macShare);
  }

  private MdsonicMSIntArithmetic<PlainT> toMdsonicMSInt(Pair<BigInteger, BigInteger> raw) {
    PlainT share = factory.createFromBigInteger(raw.getSecond());
    PlainT macShare = share.multiply(secretSharedKey);
    MdsonicASIntArithmetic<PlainT> maskedSecret = new MdsonicASIntArithmetic<>(share, macShare);
    PlainT opened = factory.zero();
    return new MdsonicMSIntArithmetic<>(maskedSecret, opened);
  }

  private MdsonicASBoolBoolean<SecretP> toMdsonicASBool(Pair<BigInteger, BigInteger> raw) {
    boolean share = (raw.getSecond().mod(BigInteger.valueOf(2)) == BigInteger.ONE);
    SecretP macShare;
    if (share) {
      macShare = secretSharedKeyBoolean.getClone();
    } else {
      macShare = factoryBoolean.zero();
    }
    return new MdsonicASBoolBoolean<>(share, macShare);
  }

  private MdsonicMSBoolBoolean<SecretP> toMdsonicMSBool(Pair<BigInteger, BigInteger> raw) {
    boolean share = (raw.getSecond().mod(BigInteger.valueOf(2)) == BigInteger.ONE);
    SecretP macShare;
    if (share) {
      macShare = secretSharedKeyBoolean.getClone();
    } else {
      macShare = factoryBoolean.zero();
    }
    MdsonicASBoolBoolean<SecretP> maskedSecret = new MdsonicASBoolBoolean<>(share, macShare);
    return new MdsonicMSBoolBoolean<>(maskedSecret, false);
  }

  class ArithmeticDummyDataSupplier {

    private final int myId;
    private final int noOfParties;
    private final BigInteger modulus;
    private final BigInteger maxOpenValue;
    private final int modBitLength;
    private final Random random;
    private final SecretSharer<BigInteger> sharer;

    // tinyOT
    private final int bitLength;
    private BytePrg bytePrg;
    private final SecretSharer<StrictBitVector> byteSharer;

    public ArithmeticDummyDataSupplier(int myId, int noOfParties, BigInteger modulus, BigInteger maxOpenValue, int bitLength, BytePrg bytePrg) {
      this.myId = myId;
      this.noOfParties = noOfParties;
      this.modulus = modulus;
      this.maxOpenValue = maxOpenValue;
      this.modBitLength = modulus.bitLength();
      this.bitLength = bitLength;
      this.bytePrg = bytePrg;
      random = new Random(42);
      sharer = new DummyBigIntegerSharer(modulus, random);
      byteSharer = new DummyByteArraySharer(bitLength, bytePrg);
    }

    public Pair<BigInteger, BigInteger> getRandomMACKeyShare() {
      BigInteger element = sampleRandomKey(maxOpenValue);
      return new Pair<>(element, sharer.share(element, noOfParties).get(myId - 1));
    }

    public Pair<StrictBitVector, StrictBitVector> getRandomMACKeyShareBoolean() {
      StrictBitVector element = bytePrg.getNext(bitLength);
      return new Pair<>(element, byteSharer.share(element, noOfParties).get(myId - 1));
    }

    /**
     * Computes the next random element and this party's share.
     */
    public Pair<BigInteger, BigInteger> getRandomElementShare() {
      BigInteger element = sampleRandomBigInteger();
      return new Pair<>(element, sharer.share(element, noOfParties).get(myId - 1));
    }

    /**
     * Computes the next random bit (expressed as {@link BigInteger}) and this party's share.
     */
    public Pair<BigInteger, BigInteger> getRandomBitShare() {
      BigInteger bit = getNextBit();
      return new Pair<>(bit, sharer.share(bit, noOfParties).get(myId - 1));
    }

    public Pair<BigInteger, BigInteger> getRandomBitShareR() {
      BigInteger bit = getNextBitR();
      return new Pair<>(bit, sharer.share(bit, noOfParties).get(myId - 1));
    }

    /**
     * Computes the next random multiplication triple and this party's shares.
     */
    public MultiplicationTripleShares getMultiplicationTripleShares() {
      BigInteger left = sampleRandomBigInteger();
      BigInteger right = sampleRandomBigInteger();
      BigInteger product = left.multiply(right);
      return new MultiplicationTripleShares(
              new Pair<>(left, sharer.share(left, noOfParties).get(myId - 1)),
              new Pair<>(right, sharer.share(right, noOfParties).get(myId - 1)),
              new Pair<>(product, sharer.share(product, noOfParties).get(myId - 1))
      );
    }

    public Pair<BigInteger, BigInteger> getNextTripleProductShare() {
      BigInteger product = sampleRandomBigInteger();
      return new Pair<>(product, sharer.share(product, noOfParties).get(myId - 1));
    }

    public MultiplicationTripleShares getMultiplicationBitTripleShares() {
      BigInteger left = getNextBit();
      BigInteger right = getNextBit();
      BigInteger product = left.multiply(right).mod(BigInteger.valueOf(2));
      return new MultiplicationTripleShares(
              new Pair<>(left, sharer.share(left, noOfParties).get(myId - 1)),
              new Pair<>(right, sharer.share(right, noOfParties).get(myId - 1)),
              new Pair<>(product, sharer.share(product, noOfParties).get(myId - 1))
      );
    }

    public Pair<MultiplicationTripleShares, MultiplicationTripleShares> getMultiplicationWRBitTripleShares() {
      BigInteger left = getNextBit();
      BigInteger right1 = getNextBit();
      BigInteger right2 = getNextBit();
      BigInteger product1 = left.multiply(right1).mod(BigInteger.valueOf(2));
      BigInteger product2 = left.multiply(right2).mod(BigInteger.valueOf(2));
      MultiplicationTripleShares triple1 = new MultiplicationTripleShares(
              new Pair<>(left, sharer.share(left, noOfParties).get(myId - 1)),
              new Pair<>(right1, sharer.share(right1, noOfParties).get(myId - 1)),
              new Pair<>(product1, sharer.share(product1, noOfParties).get(myId - 1)));
      MultiplicationTripleShares triple2 = new MultiplicationTripleShares(
              new Pair<>(left, sharer.share(left, noOfParties).get(myId - 1)),
              new Pair<>(right2, sharer.share(right2, noOfParties).get(myId - 1)),
              new Pair<>(product2, sharer.share(product2, noOfParties).get(myId - 1)));
      return new Pair<>(triple1, triple2);
    }

    public Pair<BigInteger, BigInteger> getNextBitTripleProductShare() {
      BigInteger product = getNextBit();
      return new Pair<>(product, sharer.share(product, noOfParties).get(myId - 1));
    }

    /**
     * Computes pair of values r and r^{prime} such that r^{prime} is a random element and r =
     * r^{prime} / 2^{d}, i.e., r right-shifted by d.
     */
    public TruncationPairShares getTruncationPairShares(int d) {
      BigInteger rPrime = sampleRandomBigInteger();
      BigInteger r = rPrime.shiftRight(d);
      return new TruncationPairShares(
              new Pair<>(rPrime, sharer.share(rPrime, noOfParties).get(myId - 1)),
              new Pair<>(r, sharer.share(r, noOfParties).get(myId - 1))
      );
    }

    public Pair<BigInteger, BigInteger> getNextInnerProductShare() {
      BigInteger product = sampleRandomBigInteger();
      return new Pair<>(product, sharer.share(product, noOfParties).get(myId - 1));
    }

    public Pair<BigInteger, BigInteger> getNextMatrixProductShare() {
      BigInteger product = sampleRandomBigInteger();
      return new Pair<>(product, sharer.share(product, noOfParties).get(myId - 1));
    }

    /**
     * Returns this party's share of given secret.
     *
     * @param value secret to share
     * @return this party's secret share
     */
    public BigInteger secretShare(BigInteger value) {
      return sharer.share(value, noOfParties).get(myId - 1);
    }

    private BigInteger sampleRandomBigInteger() {  // always equal to zero
      return BigInteger.ZERO;
    }

    private BigInteger sampleRandomKey(BigInteger limit) {
      BigInteger randomElement = new BigInteger(modBitLength, random);
      // this is a biased distribution but we're not in secure-land anyways
      return randomElement.mod(limit);
    }

    private BigInteger getNextBit() {
      return BigInteger.ZERO;
    }

    private BigInteger getNextBitR() {
      return random.nextBoolean() ? BigInteger.ONE : BigInteger.ZERO;
    }

    class DummyBigIntegerSharer implements SecretSharer<BigInteger> {

      private final BigInteger modulus;
      private final int modBitLength;
      private final Random random;

      DummyBigIntegerSharer(BigInteger modulus, Random random) {
        this.modulus = modulus;
        this.modBitLength = modulus.bitLength();
        this.random = random;
      }

      /**
       * Computes an additive secret-sharing of the input element.
       */
      @Override
      public List<BigInteger> share(BigInteger input, int numShares) {
        List<BigInteger> shares = getNextRandomElements(numShares - 1);
        BigInteger sumShares = MathUtils.sum(shares, modulus);
        BigInteger diff = input.subtract(sumShares).mod(modulus);
        shares.add(diff);
        return shares;
      }

      /**
       * Recombines additive secret-shares into secret.
       */
      @Override
      public BigInteger recombine(List<BigInteger> shares) {
        return MathUtils.sum(shares, modulus);
      }

      private List<BigInteger> getNextRandomElements(int numElements) {
        return IntStream.range(0, numElements)
                .mapToObj(i -> new BigInteger(modBitLength, random).mod(modulus))
                .collect(Collectors.toList());
      }
    }

    class DummyByteArraySharer implements SecretSharer<StrictBitVector> {

      private final int bitLength;
      private final BytePrg bytePrg;

      DummyByteArraySharer(int bitLength, BytePrg bytePrg) {
        this.bitLength = bitLength;
        this.bytePrg = bytePrg;
      }

      /**
       * Computes an additive secret-sharing of the input element.
       */
      @Override
      public List<StrictBitVector> share(StrictBitVector input, int numShares) {
        List<StrictBitVector> shares = getNextRandomElements(numShares - 1);
        StrictBitVector sumShares = VectorOperations.sum(shares);
        StrictBitVector diff = VectorOperations.xor(input, sumShares);
        shares.add(diff);
        return shares;
      }

      /**
       * Recombines additive secret-shares into secret.
       */
      @Override
      public StrictBitVector recombine(List<StrictBitVector> shares) {
        return VectorOperations.sum(shares);
      }

      private List<StrictBitVector> getNextRandomElements(int numElements) {
        return IntStream.range(0, numElements)
                .mapToObj(i -> bytePrg.getNext(bitLength))
                .collect(Collectors.toList());
      }
    }

  }

}


