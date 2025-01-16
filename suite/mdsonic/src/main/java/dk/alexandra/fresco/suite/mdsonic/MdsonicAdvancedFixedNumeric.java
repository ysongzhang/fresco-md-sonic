package dk.alexandra.fresco.suite.mdsonic;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.mdsonic.AdvancedNumericMdsonic.TruncationPair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.lib.real.mdsonic.AdvancedRealNumericMdsonic;
import dk.alexandra.fresco.lib.real.fixed.SFixed;
import dk.alexandra.fresco.suite.mdsonic.protocols.natives.MSS.MdsonicInnerProductProtocolMSS;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MdsonicAdvancedFixedNumeric implements AdvancedRealNumericMdsonic {

  public final ProtocolBuilderNumeric builder;
  private final int precision;
  private final boolean useMaskedEvaluation;

  public MdsonicAdvancedFixedNumeric(ProtocolBuilderNumeric builder, int precision) {
    this.builder = builder;
    this.precision = precision;
    this.useMaskedEvaluation = false;
  }

  public MdsonicAdvancedFixedNumeric(ProtocolBuilderNumeric builder, int precision, boolean useMaskedEvaluation) {
    this.builder = builder;
    this.precision = precision;
    this.useMaskedEvaluation = useMaskedEvaluation;
  }

  public MdsonicAdvancedFixedNumeric(ProtocolBuilderNumeric builder) {
    this(builder, builder.getRealNumericContext().getPrecision());
  }

  public MdsonicAdvancedFixedNumeric(ProtocolBuilderNumeric builder, boolean useMaskedEvaluation) {
    this(builder, builder.getRealNumericContext().getPrecision(), useMaskedEvaluation);
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
                  out.add(parallel.realNumericMdsonic().add(left, input1));
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
    if (useMaskedEvaluation) {
      if (a.size() != b.size()) {
        throw new IllegalArgumentException("Vectors must have same size");
      }
      // SReal to SInt
      List<DRes<SInt>> aInt = new ArrayList<>(a.size());
      List<DRes<SInt>> bInt = new ArrayList<>(a.size());
      DRes<TruncationPair> truncationPairDRes = builder.advancedNumericMdsonic().generateTruncationPair(precision);
      return builder.seq(seq -> {
        for (int i = 0; i < a.size(); i++) {
          aInt.add(((SFixed) a.get(i).out()).getSInt());
          bInt.add(((SFixed) b.get(i).out()).getSInt());
        }
        TruncationPair truncationPair = truncationPairDRes.out();
        DRes<SInt> unscaled = seq.append(new MdsonicInnerProductProtocolMSS<>(() -> aInt, () -> bInt, truncationPair.getRPrime()));
        DRes<SInt> truncated = seq.advancedNumericMdsonic().truncate(unscaled, precision, truncationPair.getR());
        return new SFixed(truncated, precision);
      });
    } else {
      return builder.par(par -> {
        if (a.size() != b.size()) {
          throw new IllegalArgumentException("Vectors must have same size");
        }

        List<DRes<SReal>> products = new ArrayList<>(a.size());
        for (int i = 0; i < a.size(); i++) {
          products.add(par.realNumericMdsonic().mult(a.get(i), b.get(i)));
        }

        return () -> products;
      }).seq((seq, list) -> {
        return seq.realAdvancedMdsonic().sum(list);
      });
    }
  }

  @Override
  public DRes<SReal> innerProductWithPublicPart(List<BigDecimal> a, List<DRes<SReal>> b) {
    if (useMaskedEvaluation) {
      throw new UnsupportedOperationException("Not supported to multiplication of List<BigDecimal> and List<SReal>");
    } else {
      return builder.par(r1 -> {
        if (a.size() != b.size()) {
          throw new IllegalArgumentException("Vectors must have same size");
        }

        List<DRes<SReal>> products = new ArrayList<>(a.size());
        for (int i = 0; i < a.size(); i++) {
          products.add(r1.realNumericMdsonic().mult(a.get(i), b.get(i)));
        }

        return () -> products;
      }).seq((seq, list) -> {
        return seq.realAdvancedMdsonic().sum(list);
      });
    }
  }
}
