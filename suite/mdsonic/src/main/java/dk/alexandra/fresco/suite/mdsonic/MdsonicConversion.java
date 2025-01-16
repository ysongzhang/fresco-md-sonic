package dk.alexandra.fresco.suite.mdsonic;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.mdsonic.ConversionMdsonic;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdsonic.protocols.natives.*;
import dk.alexandra.fresco.suite.mdsonic.protocols.natives.ASS.MdsonicArithmeticToBooleanProtocolASS;
import dk.alexandra.fresco.suite.mdsonic.protocols.natives.ASS.MdsonicBooleanToArithmeticProtocolASS;

import java.util.ArrayList;
import java.util.List;

/**
 * MD-SONIC optimized protocols for converting between arithmetic and boolean representations.
 */
public class MdsonicConversion implements ConversionMdsonic {

  private final ProtocolBuilderNumeric builder;

  private final boolean useMaskedEvaluation;

  public MdsonicConversion(ProtocolBuilderNumeric builder, boolean useMaskedEvaluation) {
    this.builder = builder;
    this.useMaskedEvaluation = useMaskedEvaluation;
  }

  @Override
  public DRes<SBool> toBoolean(DRes<SInt> arithmeticValue) {
    if (useMaskedEvaluation) {
      throw new UnsupportedOperationException("Masked: don't support to boolean");
    } else {
      return builder.append(new MdsonicArithmeticToBooleanProtocolASS<>(arithmeticValue));
    }
  }

  @Override
  public DRes<SInt> toArithmetic(DRes<SBool> booleanValue) {
    if (useMaskedEvaluation) {
      throw new UnsupportedOperationException("Masked: don't support to arithmetic");
    } else {
      return builder.append(new MdsonicBooleanToArithmeticProtocolASS<>(booleanValue));
    }
  }

  @Override
  public DRes<List<DRes<SBool>>> toBooleanBatch(DRes<List<DRes<SInt>>> arithmeticBatch) {
    if (useMaskedEvaluation) {
      throw new UnsupportedOperationException("Masked: don't support to boolean batched");
    }
    return builder.par(par -> {
      List<DRes<SInt>> inner = arithmeticBatch.out();
      List<DRes<SBool>> converted = new ArrayList<>(inner.size());
      for (DRes<SInt> anInner : inner) {
        converted.add(par.conversionMdsonic().toBoolean(anInner));
      }
      return () -> converted;
    });
  }

  @Override
  public DRes<List<DRes<SInt>>> toArithmeticBatch(DRes<List<DRes<SBool>>> booleanBatch) {
    if (useMaskedEvaluation) {
      throw new UnsupportedOperationException("Masked: don't support to arithmetic batched");
    }
    return builder.par(par -> {
      List<DRes<SBool>> inner = booleanBatch.out();
      List<DRes<SInt>> converted = new ArrayList<>(inner.size());
      for (DRes<SBool> anInner : inner) {
        converted.add(par.conversionMdsonic().toArithmetic(anInner));
      }
      return () -> converted;
    });
  }

  @Override
  public DRes<SInt> toMSSArithmetic(DRes<SInt> assValue) {
    return builder.append(new MdsonicASSToMSSArithmeticProtocol<>(assValue));
  }

  @Override
  public DRes<SInt> toMSSArithmetic(DRes<SInt> assValue, DRes<SInt> maskedSecret) {
    return builder.append(new MdsonicASSToMSSArithmeticProtocol<>(assValue, maskedSecret));
  }

  @Override
  public DRes<SBool> toMSSBoolean(DRes<SBool> assValue) {
    return builder.append(new MdsonicASSToMSSBooleanProtocol<>(assValue));
  }

  @Override
  public DRes<SBool> toMSSBoolean(DRes<SBool> assValue, DRes<SBool> maskedSecret) {
    return builder.append(new MdsonicASSToMSSBooleanProtocol<>(assValue, maskedSecret));
  }

  @Override
  public DRes<SInt> toASSArithmetic(DRes<SInt> mssValue) {
    return builder.append(new MdsonicMSSToASSArithmeticProtocol<>(mssValue));
  }

  @Override
  public DRes<SBool> toASSBoolean(DRes<SBool> mssValue) {
    return builder.append(new MdsonicMSSToASSBooleanProtocol<>(mssValue));
  }

  @Override
  public DRes<List<DRes<SInt>>> toMSSArithmeticBatch(DRes<List<DRes<SInt>>> assValueBatch) {
    return builder.par(par -> {
      List<DRes<SInt>> inner = assValueBatch.out();
      List<DRes<SInt>> converted = new ArrayList<>(inner.size());
      for (DRes<SInt> anInner : inner) {
        converted.add(par.conversionMdsonic().toMSSArithmetic(anInner));
      }
      return () -> converted;
    });
  }

  @Override
  public DRes<List<DRes<SInt>>> toMSSArithmeticBatch(DRes<List<DRes<SInt>>> assValueBatch, DRes<List<DRes<SInt>>> maskedSecretBatch) {
    return builder.par(par -> {
      List<DRes<SInt>> inner = assValueBatch.out();
      List<DRes<SInt>> masked = maskedSecretBatch.out();
      List<DRes<SInt>> converted = new ArrayList<>(inner.size());
      int i = 0;
      for (DRes<SInt> anInner : inner) {
        converted.add(par.conversionMdsonic().toMSSArithmetic(anInner, masked.get(i++)));
      }
      return () -> converted;
    });
  }

  @Override
  public DRes<List<DRes<SBool>>> toMSSBooleanBatch(DRes<List<DRes<SBool>>> assValueBatch) {
    return builder.append(new MdsonicASSToMSSBooleanBatchedProtocol<>(assValueBatch));
  }

  @Override
  public DRes<List<DRes<SBool>>> toMSSBooleanBatch(DRes<List<DRes<SBool>>> assValueBatch, DRes<List<DRes<SBool>>> maskedSecretBatch) {
    return builder.append(new MdsonicASSToMSSBooleanBatchedProtocol<>(assValueBatch, maskedSecretBatch));
  }

  @Override
  public DRes<List<DRes<SInt>>> toASSArithmeticBatch(DRes<List<DRes<SInt>>> mssValueBatch) {
    return builder.par(par -> {
      List<DRes<SInt>> inner = mssValueBatch.out();
      List<DRes<SInt>> converted = new ArrayList<>(inner.size());
      for (DRes<SInt> anInner : inner) {
        converted.add(par.conversionMdsonic().toASSArithmetic(anInner));
      }
      return () -> converted;
    });
  }

  @Override
  public DRes<List<DRes<SBool>>> toASSBooleanBatch(DRes<List<DRes<SBool>>> mssValueBatch) {
    return builder.par(par -> {
      List<DRes<SBool>> inner = mssValueBatch.out();
      List<DRes<SBool>> converted = new ArrayList<>(inner.size());
      for (DRes<SBool> anInner : inner) {
        converted.add(par.conversionMdsonic().toASSBoolean(anInner));
      }
      return () -> converted;
    });
  }

}
