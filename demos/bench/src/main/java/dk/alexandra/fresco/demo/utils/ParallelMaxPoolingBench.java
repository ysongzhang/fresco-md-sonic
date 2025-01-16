package dk.alexandra.fresco.demo.utils;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.SReal;

import java.util.ArrayList;
import java.util.List;

/**
 * Only support to MaxPooling using 2^l poolSize. Don't support overlap pooling now.
 */
public class ParallelMaxPoolingBench implements Computation<List<DRes<Matrix<DRes<SReal>>>>, ProtocolBuilderNumeric> {
    private final List<DRes<Matrix<DRes<SReal>>>> inputs;
    private final boolean useMdsonic;
    private DRes<Matrix<DRes<SReal>>> subRes;
    private List<DRes<Matrix<DRes<SReal>>>> reluRes;
    private List<DRes<Matrix<DRes<SReal>>>> outputs;

    public ParallelMaxPoolingBench(List<DRes<Matrix<DRes<SReal>>>> inputs) {
        this.inputs = inputs;
        this.useMdsonic = false;
    }

    public ParallelMaxPoolingBench(List<DRes<Matrix<DRes<SReal>>>> inputs, boolean useMdsonic) {
        this.inputs = inputs;
        this.useMdsonic = useMdsonic;
    }

    @Override
    public DRes<List<DRes<Matrix<DRes<SReal>>>>> buildComputation(ProtocolBuilderNumeric builder) {
        outputs = new ArrayList<>(inputs.size() / 2);
        reluRes = new ArrayList<>(inputs.size() / 2);

        if (useMdsonic) {
            return builder.par(par -> {
                ActivationFunctionsBench activation = new MdsonicActivationFunctionsBench(par);
                for (int i = 0; i < inputs.size() / 2; i++) {
                    DRes<Matrix<DRes<SReal>>> left = inputs.get(2 * i);
                    DRes<Matrix<DRes<SReal>>> right = inputs.get(2 * i + 1);
                    subRes = par.realLinAlg().sub(left, right);
                    //relu
                    reluRes.add(activation.activation(ActivationFunctionsBench.Type.RELU, subRes));
                }
                return () -> reluRes;
            }).par((par, res) -> {
                for (int i = 0; i < inputs.size() / 2; i++) {
                    outputs.add(par.realLinAlg().add(res.get(i), inputs.get(2 * i + 1)));
                }
                if (inputs.size() % 2 != 0) {
                    outputs.add(inputs.get(inputs.size() - 1));
                }
                return () -> outputs;
            });
        } else {
            return builder.par(par -> {
                ActivationFunctionsBench activation = new DefaultActivationFunctionsBench(par);
                for (int i = 0; i < inputs.size() / 2; i++) {
                    DRes<Matrix<DRes<SReal>>> left = inputs.get(2 * i);
                    DRes<Matrix<DRes<SReal>>> right = inputs.get(2 * i + 1);
                    subRes = par.realLinAlg().sub(left, right);
                    //relu
                    reluRes.add(activation.activation(ActivationFunctionsBench.Type.RELU, subRes));
                }
                return () -> reluRes;
            }).par((par, res) -> {
                for (int i = 0; i < inputs.size() / 2; i++) {
                    outputs.add(par.realLinAlg().add(res.get(i), inputs.get(2 * i + 1)));
                }
                if (inputs.size() % 2 != 0) {
                    outputs.add(inputs.get(inputs.size() - 1));
                }
                return () -> outputs;
            });
        }
    }

}
