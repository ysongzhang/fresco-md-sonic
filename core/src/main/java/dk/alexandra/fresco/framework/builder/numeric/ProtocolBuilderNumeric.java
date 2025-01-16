package dk.alexandra.fresco.framework.builder.numeric;

import dk.alexandra.fresco.framework.builder.ProtocolBuilderImpl;
import dk.alexandra.fresco.framework.builder.numeric.mdsonic.*;
import dk.alexandra.fresco.framework.value.OIntArithmetic;
import dk.alexandra.fresco.framework.value.OIntFactory;
import dk.alexandra.fresco.lib.compare.MiscBigIntegerGenerators;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.lib.real.AdvancedRealNumeric;
import dk.alexandra.fresco.lib.real.RealLinearAlgebra;
import dk.alexandra.fresco.lib.real.RealNumeric;
import dk.alexandra.fresco.lib.real.RealNumericContext;
import dk.alexandra.fresco.lib.real.mdsonic.AdvancedRealNumericMdsonic;
import dk.alexandra.fresco.lib.real.mdsonic.RealNumericMdsonic;

/**
 * Central class for building protocols that are based on numeric protocol suites. This class
 * contains all computation directories within FRESCO which numeric applications can use to build
 * applications.
 */
public class ProtocolBuilderNumeric extends ProtocolBuilderImpl<ProtocolBuilderNumeric> {

  private final BuilderFactoryNumeric factory;
  private BasicNumericContext basicNumericContext;
  private Numeric numeric;
  private Comparison comparison;
  private Collections collections;
  private AdvancedNumeric advancedNumeric;
  private PreprocessedValues preprocessedValues;
  private Debug debug;
  private RealNumeric realNumeric;
  private AdvancedRealNumeric advancedRealNumeric;
  private RealLinearAlgebra realLinearAlgebra;
  private Logical logical;
  private Logical logicalArithmetic;
  private Conversion conversion;
  private RealNumericContext realNumericContext;

  // For Mdsonic: MD-SONIC can only use the following Computation Dictionary
  private ComparisonMdsonic comparisonMdsonic;
  private LogicalBoolean logicalBoolean;
  private LogicalBoolean logicalBooleanASS;
  private ConversionMdsonic conversionMdsonic;
  private NumericMdsonic numericMdsonic;
  private NumericMdsonic numericMdsonicASS;
  private AdvancedNumericMdsonic advancedNumericMdsonic;
  private RealNumericMdsonic realNumericMdsonic;
  private AdvancedRealNumericMdsonic advancedRealNumericMdsonic;

  ProtocolBuilderNumeric(BuilderFactoryNumeric factory, boolean parallel) {
    super(factory, parallel);
    this.factory = factory;
    this.basicNumericContext = factory.getBasicNumericContext();
    this.realNumericContext = factory.getRealNumericContext();
  }

  /**
   * Returns the container for information about the field of operation.
   *
   * @return The {@link BasicNumericContext} used within this protocol builder.
   */
  public BasicNumericContext getBasicNumericContext() {
    return basicNumericContext;
  }

  public RealNumericContext getRealNumericContext() {
    return realNumericContext;
  }

  /**
   * Creates a {@link Numeric} computation directory for this instance - i.e. this intended
   * producer. Contains only protocol suite native basic operations such as Addition and
   * multiplication.
   *
   * @return The {@link Numeric} computation directory.
   */
  public Numeric numeric() {
    if (numeric == null) {
      numeric = factory.createNumeric(this);
    }
    return numeric;
  }

  public NumericMdsonic numericMdsonic() {
    if (numericMdsonic == null) {
      numericMdsonic = factory.createNumericMdsonic(this);
    }
    return numericMdsonic;
  }

  public NumericMdsonic numericMdsonic(boolean useMaskedEvaluation) {
    if (numericMdsonicASS == null) {
      numericMdsonicASS = factory.createNumericMdsonic(this, useMaskedEvaluation);
    }
    return numericMdsonicASS;
  }

  /**
   * Creates a {@link Comparison} computation directory for this instance - i.e. this intended
   * producer. Contains protocols on comparing numbers and computing the sign.
   *
   * @return The comparison computation directory.
   */
  public Comparison comparison() {
    if (comparison == null) {
      comparison = factory.createComparison(this);
    }
    return comparison;
  }

  public ComparisonMdsonic comparisonMdsonic() {
    if (comparisonMdsonic == null) {
      comparisonMdsonic = factory.createComparisonMdsonic(this);
    }
    return comparisonMdsonic;
  }

  /**
   * Creates an {@link AdvancedNumeric} computation directory for this instance - i.e. this intended
   * producer. Contains a lot of useful protocols such as sum, division, bit-shifting.
   *
   * @return The advanced numeric computation directory.
   */
  public AdvancedNumeric advancedNumeric() {
    if (advancedNumeric == null) {
      advancedNumeric = factory.createAdvancedNumeric(this);
    }
    return advancedNumeric;
  }

  public AdvancedNumericMdsonic advancedNumericMdsonic() {
    if (advancedNumericMdsonic == null) {
      advancedNumericMdsonic = factory.createAdvancedNumericMdsonic(this);
    }
    return advancedNumericMdsonic;
  }

  /**
   * Creates a {@link Collections} computation directory for this instance - i.e. this intended
   * producer. Contains operations on collections.
   *
   * @return The collections computation directory.
   */
  public Collections collections() {
    if (collections == null) {
      collections = factory.createCollections(this);
    }
    return collections;
  }

  /**
   * Creates a {@link Logical} computation directory for this instance - i.e. this intended
   * producer. Contains logical operations on arithmetic value.
   */
  public Logical logical() {
    if (logical == null) {
      logical = factory.createLogical(this);
    }
    return logical;
  }

  // we need a way of gracefully handling protocol suites that support both arithmetic and boolean
  // operations
  public Logical logicalArithmetic() {
    if (logicalArithmetic == null) {
      logicalArithmetic = factory.createLogicalArithmetic(this);
    }
    return logicalArithmetic;
  }

  public LogicalBoolean logicalBoolean() {
    if (logicalBoolean == null) {
      logicalBoolean = factory.createLogicalBoolean(this);
    }
    return logicalBoolean;
  }

  public LogicalBoolean logicalBoolean(boolean useMaskedEvaluation) {  // only for false
    if (logicalBooleanASS == null) {
      logicalBooleanASS = factory.createLogicalBoolean(this, useMaskedEvaluation);
    }
    return logicalBooleanASS;
  }

  /**
   * Creates a {@link Conversion} computation directory for this instance - i.e. this intended
   * producer. Contains operations for converting between arithmetic and boolean representations of
   * secret values.
   */
  public Conversion conversion() {
    if (conversion == null) {
      conversion = factory.createConversion(this);
    }
    return conversion;
  }

  public ConversionMdsonic conversionMdsonic() {
    if (conversionMdsonic == null) {
      conversionMdsonic = factory.createConversionMdsonic(this);
    }
    return conversionMdsonic;
  }

  /**
   * Creates a {@link PreprocessedValues} computation directory for this instance - i.e. this
   * intended producer. Contains elements which, if created prior to this evaluation, would save
   * computation and network. Preprocessed values does not depend on the input of the function to
   * evaluate.
   *
   * @return The preprocessed values computation directory.
   */
  public PreprocessedValues preprocessedValues() {
    if (preprocessedValues == null) {
      preprocessedValues = factory.createPreprocessedValues(this);
    }
    return preprocessedValues;
  }

  /**
   * Creates a {@link Debug} computation directory for this instance - i.e. this intended producer.
   * Contains debugging protocols for use during application development. <b>WARNING: Do not use in
   * production code as most methods within this builder reveals values to all parties.</b>
   *
   * @return The debug computation directory.
   */
  public Debug debug() {
    if (debug == null) {
      debug = factory.createDebug(this);
    }
    return debug;
  }

  /**
   * Mostly for use within internal FRESCO protocols. Contains methods helpful for working with the
   * BigInteger class.
   *
   * @return The {@link MiscBigIntegerGenerators} used within the builder.
   */
  public MiscBigIntegerGenerators getBigIntegerHelper() {
    return factory.getBigIntegerHelper();
  }

  /**
   * Returns the backend-specific implementation of {@link OIntFactory}, for converting between
   * backend-suite representations of open values and native data types.
   */
  public OIntFactory getOIntFactory() {
    return factory.getOIntFactory();
  }

  public OIntArithmetic getOIntArithmetic() {
    return factory.getOIntArithmetic();
  }

  /**
   * Creates a {@link RealNumeric} computation directory for this instance - i.e. this intended
   * producer. Contains basic arithmetic operations on fixed point numbers.
   *
   * @return The real numeric computation directory.
   */
  public RealNumeric realNumeric() {
    if (realNumeric == null) {
      realNumeric = factory.createRealNumeric(this);
    }
    return realNumeric;
  }

  public RealNumericMdsonic realNumericMdsonic() {
    if (realNumericMdsonic == null) {
      realNumericMdsonic = factory.createRealNumericMdsonic(this);
    }
    return realNumericMdsonic;
  }

  /**
   * Creates an {@link AdvancedRealNumeric} computation directory for this instance - i.e. this
   * intended producer. Contains advanced mathematical functions for fixed point numbers.
   *
   * @return The advanced real numeric computation directory.
   */
  public AdvancedRealNumeric realAdvanced() {
    if (advancedRealNumeric == null) {
      advancedRealNumeric = factory.createAdvancedRealNumeric(this);
    }
    return advancedRealNumeric;
  }

  public AdvancedRealNumericMdsonic realAdvancedMdsonic() {
    if (advancedRealNumericMdsonic == null) {
      advancedRealNumericMdsonic = factory.createAdvancedRealNumericMdsonic(this);
    }
    return advancedRealNumericMdsonic;
  }

  /**
   * Creates a {@link RealLinearAlgebra} computation directory for this instance - i.e. this
   * intended producer. Contains linear algebra functions over fixed point numbers.
   *
   * @return The advanced real numeric computation directory.
   */
  public RealLinearAlgebra realLinAlg() {
    if (realLinearAlgebra == null) {
      realLinearAlgebra = factory.createRealLinearAlgebra(this);
    }
    return realLinearAlgebra;
  }

}
