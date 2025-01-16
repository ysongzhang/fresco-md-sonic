package dk.alexandra.fresco.framework.builder.numeric.mdsonic;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.SInt;

import java.util.List;

/**
 * Operators for converting between different representations of secret values (for instance between
 * an arithmetic representation and boolean representation). <p>NOTE: this is only experimental and
 * will change in the feature. Furthermore, this is currently only supported by MD-SONIC, not
 * Spdz and Spdz2k.</p>
 */
public interface ConversionMdsonic extends ComputationDirectory {

  /**
   * Convert from arithmetic representation to boolean representation.
   */
  DRes<SBool> toBoolean(DRes<SInt> arithmeticValue);

  /**
   * Convert from boolean representation to arithmetic representation.
   */
  DRes<SInt> toArithmetic(DRes<SBool> booleanValue);

  /**
   * Convert multiple values from arithmetic to boolean.
   */
  DRes<List<DRes<SBool>>> toBooleanBatch(DRes<List<DRes<SInt>>> arithmeticBatch);

  /**
   * Convert multiple values from boolean to arithmetic.
   */
  DRes<List<DRes<SInt>>> toArithmeticBatch(DRes<List<DRes<SBool>>> booleanBatch);

  /**
   * ***********************************************************************************************************************
   * The protocols for MD-SONIC. Change between MSS and ASS
   */
  DRes<SInt> toMSSArithmetic(DRes<SInt> assValue);

  DRes<SInt> toMSSArithmetic(DRes<SInt> assValue, DRes<SInt> maskedSecret);

  DRes<SBool> toMSSBoolean(DRes<SBool> assValue);

  DRes<SBool> toMSSBoolean(DRes<SBool> assValue, DRes<SBool> maskedSecret);

  DRes<SInt> toASSArithmetic(DRes<SInt> mssValue);

  DRes<SBool> toASSBoolean(DRes<SBool> mssValue);

  DRes<List<DRes<SInt>>> toMSSArithmeticBatch(DRes<List<DRes<SInt>>> assValueBatch);

  DRes<List<DRes<SInt>>> toMSSArithmeticBatch(DRes<List<DRes<SInt>>> assValueBatch, DRes<List<DRes<SInt>>> maskedSecretBatch);

  DRes<List<DRes<SBool>>> toMSSBooleanBatch(DRes<List<DRes<SBool>>> assValueBatch);

  DRes<List<DRes<SBool>>> toMSSBooleanBatch(DRes<List<DRes<SBool>>> assValueBatch, DRes<List<DRes<SBool>>> maskedSecretBatch);

  DRes<List<DRes<SInt>>> toASSArithmeticBatch(DRes<List<DRes<SInt>>> mssValueBatch);

  DRes<List<DRes<SBool>>> toASSBooleanBatch(DRes<List<DRes<SBool>>> mssValueBatch);

}
