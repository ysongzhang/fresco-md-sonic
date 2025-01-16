package dk.alexandra.fresco.suite.mdml.resource.storage;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric.TruncationPair;
import dk.alexandra.fresco.framework.util.*;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.suite.mdml.datatypes.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Insecure implementation of {@link MdmlDataSupplier}. <p>This class deterministically generates
 * pre-processing material for each party and can therefore not be used in production.</p>
 */
public class MdmlDummyDataSupplier<
    PlainT extends MdmlCompUInt<?, ?, PlainT>> implements
    MdmlDataSupplier<PlainT> {

  private final int myId;
  private final ArithmeticDummyDataSupplier supplier;
  private final PlainT secretSharedKey;
  private final PlainT myKeyShare;
  private final MdmlCompUIntFactory<PlainT> factory;

  public MdmlDummyDataSupplier(int myId, int noOfParties, MdmlCompUIntFactory<PlainT> factory) {
    this.myId = myId;
    this.factory = factory;
    this.supplier = new ArithmeticDummyDataSupplier(
        myId,
        noOfParties,
        BigInteger.ONE.shiftLeft(factory.getCompositeBitLength()),
        BigInteger.ONE.shiftLeft(factory.getLowBitLength() - 1));
    final Pair<BigInteger, BigInteger> keyPair = supplier.getRandomMACKeyShare();
    this.secretSharedKey = factory.createFromBigInteger(keyPair.getFirst());
    this.myKeyShare = factory.createFromBigInteger(keyPair.getSecond());
  }

  @Override
  public MdmlTriple<PlainT, MdmlASIntArithmetic<PlainT>> getNextTripleSharesFull() {
    MultiplicationTripleShares rawTriple = supplier.getMultiplicationTripleShares();
    return new MdmlTriple<>(
        toMdmlSInt(rawTriple.getLeft()),
        toMdmlSInt(rawTriple.getRight()),
        toMdmlSInt(rawTriple.getProduct()));
  }

  @Override
  public MdmlTriple<PlainT, MdmlSIntBoolean<PlainT>> getNextBitTripleShares() {
    MultiplicationTripleShares rawTriple = supplier.getMultiplicationBitTripleShares();
    return new MdmlTriple<>(
        toMdmlSIntBool(rawTriple.getLeft()),
        toMdmlSIntBool(rawTriple.getRight()),
        toMdmlSIntBool(rawTriple.getProduct()));
  }

  @Override
  public MdmlInputMask<PlainT> getNextInputMask(int towardPlayerId) {
    Pair<BigInteger, BigInteger> raw = supplier.getRandomElementShare();
    if (myId == towardPlayerId) {
      return new MdmlInputMask<>(toMdmlSInt(raw),
          factory.createFromBigInteger(raw.getFirst()));
    } else {
      return new MdmlInputMask<>(toMdmlSInt(raw));
    }
  }

  @Override
  public MdmlASIntArithmetic<PlainT> getNextBitShare() {
    return toMdmlSInt(supplier.getRandomBitShare());
  }

  @Override
  public PlainT getSecretSharedKey() {
    return myKeyShare;
  }

  @Override
  public MdmlASIntArithmetic<PlainT> getNextRandomElementShare() {
    return toMdmlSInt(supplier.getRandomElementShare());
  }

  @Override
  public TruncationPair getNextTruncationPair(int d) {
    TruncationPairShares pair = supplier.getTruncationPairShares(d);
    return new TruncationPair(toMdmlSInt(pair.getRPrime()), toMdmlSInt(pair.getR()));
  }

  private Matrix<MdmlASIntArithmetic<PlainT>> getNextMatrixZeroShare(int height, int width) {
    return new Matrix<>(height, width, i -> {
      ArrayList<MdmlASIntArithmetic<PlainT>> row = new ArrayList<>(width);
      for (int j = 0; j < width; j++) {
        row.add(toMdmlSInt(supplier.getRandomElementShare()));
      }
      return row;
    });
  }

  @Override
  public MdmlMatrixTriple<PlainT, MdmlASIntArithmetic<PlainT>> getNextMatrixTripleShares(int n1, int n2, int n3) {
    Matrix<MdmlASIntArithmetic<PlainT>> left = getNextMatrixZeroShare(n1, n2);
    Matrix<MdmlASIntArithmetic<PlainT>> right = getNextMatrixZeroShare(n2, n3);
    Matrix<MdmlASIntArithmetic<PlainT>> product = getNextMatrixZeroShare(n1, n3);
    return new MdmlMatrixTriple<>(left, right, product);
  }

  private Matrix<DRes<SInt>> getNextMatrixZeroDResShare(int height, int width) {
    return new Matrix<>(height, width, i -> {
      ArrayList<DRes<SInt>> row = new ArrayList<>(width);
      for (int j = 0; j < width; j++) {
        row.add(toMdmlSInt(supplier.getRandomElementShare()));
      }
      return row;
    });
  }

  @Override
  public AdvancedNumeric.MatrixTruncationPair getNextMatrixTruncationPair(int d, int height, int width){
    Matrix<DRes<SInt>> rPrime = getNextMatrixZeroDResShare(height, width);
    Matrix<DRes<SInt>> r = getNextMatrixZeroDResShare(height, width);
    return new AdvancedNumeric.MatrixTruncationPair(rPrime, r);
  }

  @Override
  public PlainT getNextOpenedDelta(){
    return factory.zero();
  }

  @Override
  public Matrix<PlainT> getNextMatrixOpenedDelta(int n1, int n2){
    return new Matrix<>(n1, n2, i -> {
      ArrayList<PlainT> row = new ArrayList<>(n2);
      for (int j = 0; j < n2; j++) {
        row.add(factory.zero());
      }
      return row;
    });
  }

  private MdmlASIntArithmetic<PlainT> toMdmlSInt(Pair<BigInteger, BigInteger> raw) {
    PlainT openValue = factory.createFromBigInteger(raw.getFirst());
    PlainT share = factory.createFromBigInteger(raw.getSecond());
    PlainT macShare = share.multiply(secretSharedKey);
    return new MdmlASIntArithmetic<>(share, macShare);
  }

  private MdmlSIntBoolean<PlainT> toMdmlSIntBool(Pair<BigInteger, BigInteger> raw) {
    PlainT openValue = factory.createFromBigInteger(raw.getFirst()).toBitRep();
    PlainT share = factory.createFromBigInteger(raw.getSecond()).toBitRep();
    PlainT macShare = share.toArithmeticRep().multiply(secretSharedKey);
    return new MdmlSIntBoolean<>(share, macShare);
  }






  class ArithmeticDummyDataSupplier {

    private final int myId;
    private final int noOfParties;
    private final BigInteger modulus;
    private final BigInteger maxOpenValue;
    private final int modBitLength;
    private final Random random;
    private final SecretSharer<BigInteger> sharer;

    public ArithmeticDummyDataSupplier(int myId, int noOfParties, BigInteger modulus, BigInteger maxOpenValue) {
      this.myId = myId;
      this.noOfParties = noOfParties;
      this.modulus = modulus;
      this.maxOpenValue = maxOpenValue;
      this.modBitLength = modulus.bitLength();
      random = new Random(42);
      sharer = new DummyBigIntegerSharer(modulus, random);
    }

    public Pair<BigInteger, BigInteger> getRandomMACKeyShare() {
      BigInteger element = sampleRandomKey(maxOpenValue);
      return new Pair<>(element, sharer.share(element, noOfParties).get(myId - 1));
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



  }

}
