package dk.alexandra.fresco.suite.mdml;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.AdvancedRealNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.suite.mdml.protocols.natives.MdmlFixedInnerMultiplyProtocol;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * MDML-specific advanced fixed numeric functionality.
 */
public class MdmlAdvancedFixedNumeric implements AdvancedRealNumeric {

  public final ProtocolBuilderNumeric builder;
  private final int precision;

  MdmlAdvancedFixedNumeric(ProtocolBuilderNumeric builder) {
    this.builder = builder;
    this.precision = builder.getRealNumericContext().getPrecision();
  }

  @Override
  public DRes<SReal> sum(List<DRes<SReal>> input) {
    return builder.seq(seq -> () -> input)
            .whileLoop((inputs) -> inputs.size() > 1, (seq, inputs) -> seq.par(parallel -> {
              List<DRes<SReal>> out = new ArrayList<>();
              DRes<SReal> left = null;
              for (DRes<SReal> input1 : inputs) {
                if (left == null) {
                  left = input1;
                } else {
                  out.add(parallel.realNumeric().add(left, input1));
                  left = null;
                }
              }
              if (left != null) {
                out.add(left);
              }
              return () -> out;
            })).seq((r3, currentInput) -> {
              return currentInput.get(0);
            });
  }

  @Override
  public DRes<SReal> innerProduct(List<DRes<SReal>> a, List<DRes<SReal>> b) {
    if (a.size() != b.size()) {
      throw new IllegalArgumentException("Vectors must have same size");
    }

    return builder.append(new MdmlFixedInnerMultiplyProtocol<>(() -> a, () -> b, precision));
  }

  @Override
  public DRes<SReal> innerProductWithPublicPart(List<BigDecimal> a, List<DRes<SReal>> b){
    throw new UnsupportedOperationException("Not supported to innerProductWithPublicPart in AdvancedRealNumeric");
  }

  @Override
  public DRes<SReal> exp(DRes<SReal> x){
    throw new UnsupportedOperationException("Not supported to exp in AdvancedRealNumeric");
  }

  @Override
  public DRes<SReal> random(int bits){
    throw new UnsupportedOperationException("Not supported to random in AdvancedRealNumeric");
  }

  @Override
  public DRes<SReal> log(DRes<SReal> x){
    throw new UnsupportedOperationException("Not supported to log in AdvancedRealNumeric");
  }

  @Override
  public DRes<SReal> sqrt(DRes<SReal> x){
    throw new UnsupportedOperationException("Not supported to sqrt in AdvancedRealNumeric");
  }

}
