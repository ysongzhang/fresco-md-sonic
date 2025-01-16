package dk.alexandra.fresco.demo.utils;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.SReal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Average pooling.
 */
public class AveragePooling implements Computation<Matrix<DRes<SReal>>, ProtocolBuilderNumeric> {
    private final List<DRes<Matrix<DRes<SReal>>>> inputs;
    private final BigDecimal factor;

    public AveragePooling(List<DRes<Matrix<DRes<SReal>>>> inputs) {
        this.inputs = inputs;
        factor = new BigDecimal(1.0 / inputs.size());
    }

    @Override
    public DRes<Matrix<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric builder) {
        return builder.seq(seq -> () -> inputs)
                .whileLoop((inputs) -> inputs.size() > 1, (seq, inputs) -> seq.par(parallel -> {
                    List<DRes<Matrix<DRes<SReal>>>> out = new ArrayList<>();
                    DRes<Matrix<DRes<SReal>>> left = null;
                    for (DRes<Matrix<DRes<SReal>>> input1 : inputs) {
                        if (left == null) {
                            left = input1;
                        } else {
                            out.add(parallel.realLinAlg().add(left, input1));
                            left = null;
                        }
                    }
                    if (left != null) {
                        out.add(left);
                    }
                    return () -> out;
                })).seq((seq, out) -> out.get(0))
                .seq((seq, o) -> seq.realLinAlg().scale(this.factor, () -> o));
    }

}
