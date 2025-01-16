package dk.alexandra.fresco.lib.real.fixed.mdsonic;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.mdsonic.AdvancedNumericMdsonic.TruncationPair;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.lib.real.mdsonic.RealNumericMdsonic;
import dk.alexandra.fresco.lib.real.fixed.SFixed;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * An implementation of the {@link RealNumericMdsonic} ComputationDirectory based on a fixed point
 * representation of real numbers.
 *
 * <p>The process FRESCO uses to handle decimals is as follows:<br>
 * 1. First, the decimal is imported into a {@link BigDecimal} variable.<br>
 * 2. Next, the decimal is converted into a {@link BigInteger} using the unscaled method in {@link MdsonicFixedNumeric}.
 * This operation involves shifting the binary representation of the original decimal to the left by precision bits
 * (equivalent to multiplying by 2^{precision}) and truncating the subsequent decimal places, thereby converting it into
 * an integer. Note that the resulting integer is a signed number with a sign bit.<br>
 * 3. The {@link BigInteger} is further parsed into a long or int type. At this stage, the signed number is stored in
 * long or int using two's complement representation.<br>
 * 4. During protocol execution, arithmetic operations such as addition, subtraction, multiplication, and division are
 * performed on the two's complement representation of long or int types, ultimately yielding the two's complement
 * result as output.<br>
 * 5. The final two's complement result is converted back to its original signed value by interpreting the MSB and
 * storing it in a {@link BigInteger} variable. This step is accomplished using the toSignedBigInteger method in the
 * UInt interface.<br>
 * 6. Finally, the integer is restored to a {@link BigDecimal} decimal using the scaled method in
 * {@link MdsonicFixedNumeric}, which reverses the process described in step 2.</p>
 */
public class MdsonicFixedNumeric implements RealNumericMdsonic {

  protected final ProtocolBuilderNumeric builder;
  // TODO these should be cached static fields
  private final int precision;
  private final BigInteger scalingFactor;
  private final BigDecimal scalingFactorDecimal;
  private final boolean useMaskedEvaluation;

  public MdsonicFixedNumeric(ProtocolBuilderNumeric builder, int precision) {
    this.builder = builder;
    this.precision = precision;
    this.scalingFactor = BigInteger.ONE.shiftLeft(precision);
    this.scalingFactorDecimal = new BigDecimal(scalingFactor);
    this.useMaskedEvaluation = false;
  }

  public MdsonicFixedNumeric(ProtocolBuilderNumeric builder, int precision, boolean useMaskedEvaluation) {
    this.builder = builder;
    this.precision = precision;
    this.scalingFactor = BigInteger.ONE.shiftLeft(precision);
    this.scalingFactorDecimal = new BigDecimal(scalingFactor);
    this.useMaskedEvaluation = useMaskedEvaluation;
  }

  public MdsonicFixedNumeric(ProtocolBuilderNumeric builder) {
    this(builder, builder.getRealNumericContext().getPrecision());
  }

  public MdsonicFixedNumeric(ProtocolBuilderNumeric builder, boolean useMaskedEvaluation) {
    this(builder, builder.getRealNumericContext().getPrecision(), useMaskedEvaluation);
  }



  private BigInteger unscaled(BigDecimal value) {
    return value.multiply(scalingFactorDecimal).setScale(0, RoundingMode.HALF_UP)
        .toBigIntegerExact();
  }

  private BigDecimal scaled(BigInteger value) {
    return new BigDecimal(value).setScale(precision).divide(scalingFactorDecimal,
        RoundingMode.HALF_UP);
  }

  @Override
  public DRes<SReal> add(DRes<SReal> a, DRes<SReal> b) {
    return builder.seq(seq -> {
      SFixed floatA = (SFixed) a.out();
      SFixed floatB = (SFixed) b.out();
      return new SFixed(seq.numericMdsonic().add(floatA.getSInt(), floatB.getSInt()), precision);
    });
  }

  @Override
  public DRes<SReal> add(BigDecimal a, DRes<SReal> b) {
    return builder.seq(seq -> {
      BigInteger unscaled = unscaled(a);
      SFixed floatB = (SFixed) b.out();
      return new SFixed(seq.numericMdsonic().add(unscaled, floatB.getSInt()), precision);
    });
  }

  @Override
  public DRes<SReal> sub(DRes<SReal> a, DRes<SReal> b) {
    return builder.seq(seq -> {
      SFixed floatA = (SFixed) a.out();
      SFixed floatB = (SFixed) b.out();
      return new SFixed(seq.numericMdsonic().sub(floatA.getSInt(), floatB.getSInt()), precision);
    });
  }

  @Override
  public DRes<SReal> sub(BigDecimal a, DRes<SReal> b) {
    return builder.seq(seq -> {
      BigInteger unscaled = unscaled(a);
      SFixed floatB = (SFixed) b.out();
      return new SFixed(seq.numericMdsonic().sub(unscaled, floatB.getSInt()), precision);
    });
  }

  @Override
  public DRes<SReal> sub(DRes<SReal> a, BigDecimal b) {
    return builder.seq(seq -> {
      BigInteger unscaled = unscaled(b);
      SFixed floatA = (SFixed) a.out();
      return new SFixed(seq.numericMdsonic().sub(floatA.getSInt(), unscaled), precision);
    });
  }

  @Override
  public DRes<SReal> mult(DRes<SReal> a, DRes<SReal> b) {
    if (useMaskedEvaluation) {
      DRes<TruncationPair> truncationPairDRes = builder.advancedNumericMdsonic().generateTruncationPair(precision);
      return builder.seq(seq -> {
        SFixed floatA = (SFixed) a.out();
        SFixed floatB = (SFixed) b.out();
        TruncationPair truncationPair = truncationPairDRes.out();
        DRes<SInt> unscaled = seq.numericMdsonic().mult(floatA.getSInt(), floatB.getSInt(), truncationPair.getRPrime());
        DRes<SInt> truncated = seq.advancedNumericMdsonic().truncate(unscaled, precision, truncationPair.getR());
        return new SFixed(truncated, precision);
      });
    } else {
      return builder.seq(seq -> {
        SFixed floatA = (SFixed) a.out();
        SFixed floatB = (SFixed) b.out();
        DRes<SInt> unscaled = seq.numericMdsonic().mult(floatA.getSInt(), floatB.getSInt());
        DRes<SInt> truncated = seq.advancedNumericMdsonic().truncate(unscaled, precision);
        return new SFixed(truncated, precision);
      });
    }
  }

  @Override
  public DRes<SReal> mult(BigDecimal a, DRes<SReal> b) {
    return builder.seq(seq -> {
      BigInteger unscaled = unscaled(a);
      SFixed floatB = (SFixed) b.out();
      DRes<SInt> overflowedProduct = seq.numericMdsonic().mult(unscaled, floatB.getSInt());
      DRes<SInt> truncated = seq.advancedNumericMdsonic().truncate(overflowedProduct, precision);
      return new SFixed(truncated, precision);
    });
  }

  @Override
  public DRes<SReal> div(DRes<SReal> a, DRes<SReal> b) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public DRes<SReal> div(DRes<SReal> a, BigDecimal b) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public DRes<SReal> known(BigDecimal value) {
    return builder.seq(seq -> {
      DRes<SInt> input = seq.numericMdsonic().known(unscaled(value));
      return new SFixed(input, precision);
    });
  }

  @Override
  public DRes<SReal> fromScaled(BigInteger value) {
    return builder.seq(seq -> {
      DRes<SInt> input = seq.numericMdsonic().known(value);
      return new SFixed(input, precision);
    });
  }

  @Override
  public DRes<SReal> fromSInt(DRes<SInt> value) {
    return builder.seq(seq -> {
      DRes<SInt> scaled = seq.numericMdsonic().mult(scalingFactor, value);
      return new SFixed(scaled, precision);
    });
  }

  @Override
  public DRes<SReal> input(BigDecimal value, int inputParty) {
    return builder.seq(seq -> {
      BigInteger unscaled = (value != null) ? unscaled(value) : null;
      DRes<SInt> input = seq.numericMdsonic().input(unscaled, inputParty);
      return new SFixed(input, precision);
    });
  }

  @Override
  public DRes<BigDecimal> open(DRes<SReal> secretShare) {
    return builder.seq(seq -> {
      SFixed floatX = (SFixed) secretShare.out();
      DRes<SInt> unscaled = floatX.getSInt();
      return seq.numericMdsonic().open(unscaled);
    }).seq((seq, unscaled) -> {
      // new scope to avoid scaling in lambda (since that would be recalculated every time value.out
      // is called)
      BigDecimal scaled = scaled(unscaled);
      return () -> scaled;
    });
  }

  @Override
  public DRes<BigInteger> openRaw(DRes<SReal> secretShare) {
    return builder.seq(seq -> {
      SFixed floatX = (SFixed) secretShare.out();
      DRes<SInt> unscaled = floatX.getSInt();
      return seq.numericMdsonic().open(unscaled);
    });
  }

  @Override
  public DRes<BigDecimal> open(DRes<SReal> secretShare, int outputParty) {
    return builder.seq(seq -> {
      SFixed floatX = (SFixed) secretShare.out();
      DRes<SInt> unscaled = floatX.getSInt();
      return seq.numericMdsonic().open(unscaled, outputParty);
    }).seq((seq, unscaled) -> {
      // new scope to avoid scaling in lambda (since that would be recalculated every time value.out
      // is called)
      if (unscaled != null) {
        BigDecimal scaled = scaled(unscaled);
        return () -> scaled;
      } else {
        return null;
      }
    });
  }

  @Override
  public DRes<SBool> leq(DRes<SReal> x, DRes<SReal> y) {
    return builder.seq(seq -> {
      SFixed floatX = (SFixed) x.out();
      SFixed floatY = (SFixed) y.out();
      return seq.comparisonMdsonic().compareLEQ(floatX.getSInt(), floatY.getSInt());
    });
  }

  public DRes<SBool> lt(DRes<SReal> x, DRes<SReal> y) {
    return builder.seq(seq -> {
      SFixed floatX = (SFixed) x.out();
      SFixed floatY = (SFixed) y.out();
      return seq.comparisonMdsonic().compareLT(floatX.getSInt(), floatY.getSInt());
    });
  }

  public DRes<SBool> equals(DRes<SReal> x, DRes<SReal> y) {
    return builder.seq(seq -> {
      SFixed floatX = (SFixed) x.out();
      SFixed floatY = (SFixed) y.out();
      return seq.comparisonMdsonic().equals(floatX.getSInt(), floatY.getSInt());
    });
  }

  public DRes<SBool> zero(DRes<SReal> x, int bitlength) {
    return builder.seq(seq -> {
      SFixed floatX = (SFixed) x.out();
      return seq.comparisonMdsonic().compareZero(floatX.getSInt(), bitlength);
    });
  }

  @Override
  public DRes<SReal> relu(DRes<SReal> input) {
    if (useMaskedEvaluation) {
      return builder.seq(seq -> {
        SFixed floatInput = (SFixed) input.out();
        DRes<SInt> res = seq.comparisonMdsonic().relu(floatInput.getSInt());
        return new SFixed(res, precision);
      });
    } else {
        return builder.seq(seq -> {
          DRes<SBool> compareBit = seq.realNumericMdsonic().leq(seq.realNumericMdsonic().known(BigDecimal.ZERO), input);
          DRes<SInt> compare = seq.conversionMdsonic().toArithmetic(compareBit);
          return seq.realNumericMdsonic().mult(input, seq.realNumericMdsonic().fromSInt(compare));
        });
    }
  }
}
