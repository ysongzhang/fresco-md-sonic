package dk.alexandra.fresco.suite.mdml;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.Conversion;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdml.protocols.natives.MdmlArithmeticToBooleanProtocol;
import dk.alexandra.fresco.suite.mdml.protocols.natives.MdmlBooleanToArithmeticProtocol;

import java.util.ArrayList;
import java.util.List;

/**
 * Mdml optimized protocols for converting between arithmetic and boolean representations.
 */
public class MdmlConversion implements Conversion {

  private final ProtocolBuilderNumeric builder;

  public MdmlConversion(ProtocolBuilderNumeric builder) {
    this.builder = builder;
  }

  @Override
  public DRes<SInt> toBoolean(DRes<SInt> arithmeticValue) {
    return builder.append(new MdmlArithmeticToBooleanProtocol<>(arithmeticValue));
  }

  @Override
  public DRes<SInt> toArithmetic(DRes<SInt> booleanValue) {
    return builder.append(new MdmlBooleanToArithmeticProtocol<>(booleanValue));
  }

  @Override
  public DRes<List<DRes<SInt>>> toBooleanBatch(DRes<List<DRes<SInt>>> arithmeticBatch) {
    return builder.par(par -> {
      List<DRes<SInt>> inner = arithmeticBatch.out();
      List<DRes<SInt>> converted = new ArrayList<>(inner.size());
      for (DRes<SInt> anInner : inner) {
        converted.add(par.conversion().toBoolean(anInner));
      }
      return () -> converted;
    });
  }

  @Override
  public DRes<List<DRes<SInt>>> toArithmeticBatch(DRes<List<DRes<SInt>>> booleanBatch) {
    return builder.par(par -> {
      List<DRes<SInt>> inner = booleanBatch.out();
      List<DRes<SInt>> converted = new ArrayList<>(inner.size());
      for (DRes<SInt> anInner : inner) {
        converted.add(par.conversion().toArithmetic(anInner));
      }
      return () -> converted;
    });
  }

}
