package dk.alexandra.fresco.suite.mdsonic;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.*;
import dk.alexandra.fresco.framework.builder.numeric.mdsonic.*;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.*;
import dk.alexandra.fresco.lib.compare.MiscBigIntegerGenerators;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.lib.real.RealLinearAlgebra;
import dk.alexandra.fresco.lib.real.RealNumericContext;
import dk.alexandra.fresco.lib.real.mdsonic.AdvancedRealNumericMdsonic;
import dk.alexandra.fresco.lib.real.mdsonic.RealNumericMdsonic;
import dk.alexandra.fresco.lib.real.fixed.mdsonic.MdsonicFixedNumeric;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicCompUInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicCompUIntArithmetic;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicCompUIntFactory;
import dk.alexandra.fresco.suite.mdsonic.protocols.computations.InputComputationMdsonic;
import dk.alexandra.fresco.suite.mdsonic.protocols.computations.MdsonicInputMacCheckComputation;
import dk.alexandra.fresco.suite.mdsonic.protocols.natives.*;
import dk.alexandra.fresco.suite.mdsonic.protocols.natives.MSS.MdsonicGetOpenedProtocolMSS;
import dk.alexandra.fresco.suite.mdsonic.protocols.natives.MSS.MdsonicMultiplyProtocolMSS;

import java.math.BigInteger;
import java.util.List;

/**
 * Basic native builder for the Mdsonic protocol suite.
 *
 * @param <PlainT> the type representing open values
 */
public class MdsonicBuilder<PlainT extends MdsonicCompUInt<?, ?, PlainT>> implements
    BuilderFactoryNumeric {

  private final MdsonicCompUIntFactory<PlainT> factory;
  private final BasicNumericContext numericContext;
  private final RealNumericContext realNumericContext;
  private final boolean useMaskedEvaluation;
  private final MdsonicCompUIntArithmetic<PlainT> uIntArithmetic;

  public MdsonicBuilder(MdsonicCompUIntFactory<PlainT> factory,
                       BasicNumericContext numericContext,
                       RealNumericContext realNumericContext,
                       boolean useMaskedEvaluation) {
    this.factory = factory;
    this.uIntArithmetic = new MdsonicCompUIntArithmetic<>(factory);
    this.numericContext = numericContext;
    this.realNumericContext = realNumericContext;
    this.useMaskedEvaluation = useMaskedEvaluation;
  }

  @Override
  public BasicNumericContext getBasicNumericContext() {
    return numericContext;
  }

  @Override
  public ComparisonMdsonic createComparisonMdsonic(ProtocolBuilderNumeric builder) {
      return new MdsonicComparison<>(this, builder, factory, useMaskedEvaluation);
  }

  @Override
  public LogicalBoolean createLogicalBoolean(ProtocolBuilderNumeric builder) {
      return new MdsonicLogicalBoolean<>(builder, factory, useMaskedEvaluation);
  }

  @Override
  public LogicalBoolean createLogicalBoolean(ProtocolBuilderNumeric builder, boolean useMaskedEvaluation) {
    return new MdsonicLogicalBoolean<>(builder, factory, useMaskedEvaluation);
  }

  @Override
  public AdvancedNumericMdsonic createAdvancedNumericMdsonic(ProtocolBuilderNumeric builder) {
    return new MdsonicAdvancedNumeric(this, builder, useMaskedEvaluation);
  }

  @Override
  public RealNumericMdsonic createRealNumericMdsonic(ProtocolBuilderNumeric builder) {
    return new MdsonicFixedNumeric(builder, useMaskedEvaluation);
  }

  @Override
  public AdvancedRealNumericMdsonic createAdvancedRealNumericMdsonic(ProtocolBuilderNumeric builder) {
    return new MdsonicAdvancedFixedNumeric(builder, useMaskedEvaluation);
  }

  @Override
  public RealLinearAlgebra createRealLinearAlgebra(ProtocolBuilderNumeric builder) {
    return new MdsonicFixedLinearAlgebra(builder, useMaskedEvaluation);
  }

  @Override
  public NumericMdsonic createNumericMdsonic(ProtocolBuilderNumeric builder) {
    return createNumericMdsonic(builder, useMaskedEvaluation);
  }

  @Override
  public NumericMdsonic createNumericMdsonic(ProtocolBuilderNumeric builder, boolean useMaskedEvaluation) {
    return new NumericMdsonic() {
      @Override
      public DRes<SInt> add(DRes<SInt> a, DRes<SInt> b) {
        return builder.append(new MdsonicAddProtocol<>(a, b, useMaskedEvaluation));
      }

      @Override
      public DRes<SInt> add(BigInteger a, DRes<SInt> b) {
        return builder.append(new MdsonicAddKnownProtocol<>(factory.createFromBigInteger(a), b, useMaskedEvaluation));
      }

      @Override
      public DRes<SInt> addOpen(DRes<OInt> a, DRes<SInt> b) {
        return builder.append(new MdsonicAddKnownProtocol<>(factory.fromOInt(a), b, useMaskedEvaluation));
      }

      @Override
      public DRes<SInt> sub(DRes<SInt> a, DRes<SInt> b) {
        return builder.append(new MdsonicSubtractProtocol<>(a, b, useMaskedEvaluation));
      }

      @Override
      public DRes<SInt> sub(BigInteger a, DRes<SInt> b) {
        return builder.append(
            new MdsonicSubtractFromKnownProtocol<>(factory.createFromBigInteger(a), b, useMaskedEvaluation));
      }

      @Override
      public DRes<SInt> subFromOpen(DRes<OInt> a, DRes<SInt> b) {
        return builder.append(new MdsonicSubtractFromKnownProtocol<>(a, b, useMaskedEvaluation));
      }

      @Override
      public DRes<SInt> subOpen(DRes<SInt> a, DRes<OInt> b) {
        return builder.append(
            new MdsonicAddKnownProtocol<>(factory.fromOInt(b).negate(), a, useMaskedEvaluation));
      }

      @Override
      public DRes<SInt> sub(DRes<SInt> a, BigInteger b) {
        return builder.append(
            new MdsonicAddKnownProtocol<>(factory.createFromBigInteger(b).negate(), a, useMaskedEvaluation));
      }

      @Override
      public DRes<SInt> mult(DRes<SInt> a, DRes<SInt> b) {
        return builder.append(new MdsonicMultiplyProtocol<>(a, b, useMaskedEvaluation));
      }

      @Override
      public DRes<SInt> mult(DRes<SInt> a, DRes<SInt> b, DRes<SInt> maskedSecret) {
        return builder.append(new MdsonicMultiplyProtocol<>(a, b, useMaskedEvaluation, maskedSecret));
      }

      @Override
      public DRes<SInt> mult(BigInteger a, DRes<SInt> b) {
        return multByOpen(factory.createFromBigInteger(a), b);
      }

      @Override
      public DRes<SInt> multByOpen(DRes<OInt> a, DRes<SInt> b) {
        return builder.append(new MdsonicMultKnownProtocol<>(a, b, useMaskedEvaluation));
      }

      @Override
      public DRes<SInt> randomBitAsArithmetic() {
        return builder.append(new MdsonicRandomBitAsArithmeticProtocol<>());
      }

      @Override
      public DRes<SBool> randomBitAsBoolean() {
        return builder.append(new MdsonicRandomBitAsBooleanProtocol<>());
      }

      @Override
      public DRes<SInt> randomElement() {
        return builder.append(new MdsonicRandomElementProtocol<>());
      }

      @Override
      public DRes<SInt> known(BigInteger value) {
        return builder.append(new MdsonicKnownSIntProtocol<>(
            (value != null) ? factory.fromLong(value.longValue()) : null, useMaskedEvaluation));
      }

      @Override
      public DRes<SInt> secret(byte[] opened, DRes<SInt> random) {
        return builder.append(new MdsonicSecretProtocol<>(
                factory.createFromBytes(opened), random, useMaskedEvaluation));
      }

      @Override
      public DRes<SInt> input(BigInteger value, int inputParty) {
        // long类型表示长整数，既有正数也有负数，是符号数，在计算机内部按照补码存储
        PlainT val = (value != null) ? factory.fromLong(value.longValue()) : null;
        if (builder.getBasicNumericContext().getNoOfParties() <= 2) {
          return builder.append(new MdsonicTwoPartyInputProtocol<>(val, inputParty, useMaskedEvaluation));
        } else {
          return builder.seq(new InputComputationMdsonic<>(val, inputParty, useMaskedEvaluation));
        }
      }

      @Override
      public DRes<OInt> openAsOInt(DRes<SInt> secretShare) {
        return builder.append(new MdsonicOutputToAll<>(secretShare, useMaskedEvaluation));
      }

      @Override
      public DRes<BigInteger> open(DRes<SInt> secretShare) {
        DRes<OInt> out = openAsOInt(secretShare);
        return () -> factory.fromOInt(out).toSignedBigInteger();  // return Signed value
      }

      @Override
      public DRes<OInt> openAsOInt(DRes<SInt> secretShare, int outputParty) {
        return builder.append(new MdsonicOutputSinglePartyProtocol<>(secretShare, outputParty, useMaskedEvaluation));
      }

      @Override
      public DRes<BigInteger> open(DRes<SInt> secretShare, int outputParty) {
        DRes<OInt> out = openAsOInt(secretShare, outputParty);
        return () -> {
          OInt res = out.out();
          if (res == null) {
            return null;
          } else {
            return factory.fromOInt(out).toSignedBigInteger();
          }
        };
      }

      @Override
      public DRes<Void> macCheck(List<DRes<SInt>> randomList, byte[] opened, byte[] seed) {
        Pair<List<DRes<SInt>>, PlainT> toCheck = new Pair<>(randomList, factory.createFromBytes(opened));
        return builder.seq(new MdsonicInputMacCheckComputation<>(toCheck, seed,
                getBasicNumericContext().getNoOfParties(), factory));
      }


      // MSS专有计算
      @Override
      public DRes<SInt> multMSS(DRes<SInt> a, DRes<SInt> b) {  // return ASS
        if (useMaskedEvaluation) {
          return builder.append(new MdsonicMultiplyProtocolMSS<>(a, b));
        } else {
          throw new UnsupportedOperationException("ASS: Don't support mul MSS");
        }
      }

      @Override
      public DRes<OInt> getOpened(DRes<SInt> secretShare) {
        if (useMaskedEvaluation) {
          return builder.append(new MdsonicGetOpenedProtocolMSS<>(secretShare));
        } else {
          throw new UnsupportedOperationException("ASS: Don't support obtain Opened value of an MSS secret");
        }
      }

      @Override
      public DRes<SInt> getProduct() {
        return builder.append(new MdsonicGetProductProtocol<>());
      }
    };
  }

  @Override
  public ConversionMdsonic createConversionMdsonic(ProtocolBuilderNumeric builder) {
    return new MdsonicConversion(builder, useMaskedEvaluation);
  }

  @Override
  public MiscBigIntegerGenerators getBigIntegerHelper() {
    throw new UnsupportedOperationException();
  }

  @Override
  public OIntFactory getOIntFactory() {
    return factory;
  }

  @Override
  public OIntArithmetic getOIntArithmetic() {
    return uIntArithmetic;
  }

  @Override
  public RealNumericContext getRealNumericContext() {
    return realNumericContext;
  }

}
