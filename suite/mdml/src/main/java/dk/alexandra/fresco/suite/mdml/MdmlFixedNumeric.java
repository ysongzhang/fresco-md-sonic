package dk.alexandra.fresco.suite.mdml;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.RealNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.lib.real.fixed.DefaultFixedNumeric;
import dk.alexandra.fresco.suite.mdml.protocols.natives.MdmlFixedMultiplyProtocol;


/**
 * Mdml optimized protocols for real numeric.
 */
public class MdmlFixedNumeric extends DefaultFixedNumeric implements RealNumeric {

  private final int precision;

  public MdmlFixedNumeric(ProtocolBuilderNumeric builder) {
    super(builder);
    this.precision = builder.getRealNumericContext().getPrecision();
  }

  public MdmlFixedNumeric(ProtocolBuilderNumeric builder, int precision) {
    super(builder, precision);
    this.precision = precision;
  }

  @Override
  public DRes<SReal> mult(DRes<SReal> a, DRes<SReal> b) {
    return builder.append(new MdmlFixedMultiplyProtocol<>(a, b, precision));
  }

}
