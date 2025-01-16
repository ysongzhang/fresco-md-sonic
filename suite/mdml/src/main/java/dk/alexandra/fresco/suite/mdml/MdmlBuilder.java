package dk.alexandra.fresco.suite.mdml;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.*;
import dk.alexandra.fresco.framework.value.*;
import dk.alexandra.fresco.lib.compare.MiscBigIntegerGenerators;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.lib.real.AdvancedRealNumeric;
import dk.alexandra.fresco.lib.real.RealLinearAlgebra;
import dk.alexandra.fresco.lib.real.RealNumeric;
import dk.alexandra.fresco.lib.real.RealNumericContext;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUIntArithmetic;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUIntFactory;
import dk.alexandra.fresco.suite.mdml.protocols.computations.InputComputationMdml;
import dk.alexandra.fresco.suite.mdml.protocols.natives.*;

import java.math.BigInteger;

/**
 * Basic native builder for the Mdml protocol suite.
 *
 * @param <PlainT> the type representing open values
 */
public class MdmlBuilder<PlainT extends MdmlCompUInt<?, ?, PlainT>> implements
    BuilderFactoryNumeric {

  private final MdmlCompUIntFactory<PlainT> factory;
  private final BasicNumericContext numericContext;
  private final RealNumericContext realNumericContext;
  private final MdmlCompUIntArithmetic<PlainT> uIntArithmetic;

  public MdmlBuilder(MdmlCompUIntFactory<PlainT> factory,
                     BasicNumericContext numericContext,
                     RealNumericContext realNumericContext) {
    this.factory = factory;
    this.uIntArithmetic = new MdmlCompUIntArithmetic<>(factory);
    this.numericContext = numericContext;
    this.realNumericContext = realNumericContext;
  }

  @Override
  public BasicNumericContext getBasicNumericContext() {
    return numericContext;
  }

  @Override
  public Comparison createComparison(ProtocolBuilderNumeric builder) {
      return new MdmlComparison<>(this, builder, factory);
  }

  @Override
  public Logical createLogical(ProtocolBuilderNumeric builder) {
      return new MdmlLogicalBoolean<>(builder, factory);
  }

  @Override
  public AdvancedNumeric createAdvancedNumeric(ProtocolBuilderNumeric builder) {
    return new MdmlAdvancedNumeric(builder);
  }

  @Override
  public RealNumeric createRealNumeric(ProtocolBuilderNumeric builder) {
    return new MdmlFixedNumeric(builder);
  }

  @Override
  public AdvancedRealNumeric createAdvancedRealNumeric(ProtocolBuilderNumeric builder) {
    return new MdmlAdvancedFixedNumeric(builder);
  }

  @Override
  public RealLinearAlgebra createRealLinearAlgebra(ProtocolBuilderNumeric builder) {
    return new MdmlFixedLinearAlgebra(builder);
  }

  @Override
  public Numeric createNumeric(ProtocolBuilderNumeric builder) {
    return new Numeric() {
      @Override
      public DRes<SInt> add(DRes<SInt> a, DRes<SInt> b) {
        return builder.append(new MdmlAddProtocol<>(a, b));
      }

      @Override
      public DRes<SInt> add(BigInteger a, DRes<SInt> b) {
        return builder.append(new MdmlAddKnownProtocol<>(factory.createFromBigInteger(a), b));
      }

      @Override
      public DRes<SInt> addOpen(DRes<OInt> a, DRes<SInt> b) {
        return builder.append(new MdmlAddKnownProtocol<>(factory.fromOInt(a), b));
      }

      @Override
      public DRes<SInt> sub(DRes<SInt> a, DRes<SInt> b) {
        return builder.append(new MdmlSubtractProtocol<>(a, b));
      }

      @Override
      public DRes<SInt> sub(BigInteger a, DRes<SInt> b) {
        return builder.append(
                new MdmlSubtractFromKnownProtocol<>(factory.createFromBigInteger(a), b));
      }

      @Override
      public DRes<SInt> subFromOpen(DRes<OInt> a, DRes<SInt> b) {
        return builder.append(new MdmlSubtractFromKnownProtocol<>(a, b));
      }

      @Override
      public DRes<SInt> subOpen(DRes<SInt> a, DRes<OInt> b) {
        return builder.append(
                new MdmlAddKnownProtocol<>(factory.fromOInt(b).negate(), a));
      }

      @Override
      public DRes<SInt> sub(DRes<SInt> a, BigInteger b) {
        return builder.append(
                new MdmlAddKnownProtocol<>(factory.createFromBigInteger(b).negate(), a));
      }

      @Override
      public DRes<SInt> mult(DRes<SInt> a, DRes<SInt> b) {
        return builder.append(new MdmlMultiplyProtocol<>(a, b));
      }

      @Override
      public DRes<SInt> mult(BigInteger a, DRes<SInt> b) {
        return multByOpen(factory.createFromBigInteger(a), b);
      }

      @Override
      public DRes<SInt> multByOpen(DRes<OInt> a, DRes<SInt> b) {
        return builder.append(new MdmlMultKnownProtocol<>(a, b));
      }

      @Override
      public DRes<SInt> randomBit() {
        return builder.append(new MdmlRandomBitProtocol<>());
      }

      @Override
      public DRes<SInt> randomElement() {
        return builder.append(new MdmlRandomElementProtocol<>());
      }

      @Override
      public DRes<SInt> known(BigInteger value) {
        return builder.append(new MdmlKnownSIntProtocol<>(
                (value != null) ? factory.fromLong(value.longValue()) : null));
      }

      @Override
      public DRes<SInt> input(BigInteger value, int inputParty) {
        PlainT val = (value != null) ? factory.fromLong(value.longValue()) : null;
        if (builder.getBasicNumericContext().getNoOfParties() <= 2) {
          return builder.append(new MdmlTwoPartyInputProtocol<>(val, inputParty));
        } else {
          return builder.seq(new InputComputationMdml<>(val, inputParty));
        }
      }

      @Override
      public DRes<OInt> openAsOInt(DRes<SInt> secretShare) {
        return builder.append(new MdmlOutputToAll<>(secretShare));
      }

      @Override
      public DRes<BigInteger> open(DRes<SInt> secretShare) {
        DRes<OInt> out = openAsOInt(secretShare);
        return () -> factory.fromOInt(out).toSignedBigInteger();
      }

      @Override
      public DRes<OInt> openAsOInt(DRes<SInt> secretShare, int outputParty) {
        return builder.append(new MdmlOutputSinglePartyProtocol<>(secretShare, outputParty));
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
    };
  }

  @Override
  public Conversion createConversion(ProtocolBuilderNumeric builder) {
    return new MdmlConversion(builder);
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
