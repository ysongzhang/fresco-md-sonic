package dk.alexandra.fresco.demo.nn;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.SReal;

import java.util.List;

public class Resnet implements Computation<Matrix<DRes<SReal>>, ProtocolBuilderNumeric> {

    private List<LayerParameters> layerParameters;
    private DRes<Matrix<DRes<SReal>>> input;
    private final boolean useMdsonic;
    private final boolean useMatrix;

    public Resnet(List<LayerParameters> layers,
                  DRes<Matrix<DRes<SReal>>> input, boolean useMdsonic, boolean useMatrix) {
        this.layerParameters = layers;
        this.input = input;
        this.useMdsonic = useMdsonic;
        this.useMatrix = useMatrix;
    }

    @Override
    public DRes<Matrix<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric builder) {
        ActivationFunctions activation;
        if (useMdsonic) {
            activation = new MdsonicActivationFunctions(builder);
        } else {
            activation = new DefaultActivationFunctions(builder);
        }

        return builder.seq(new PrivacyConvLayer((ConvLayerParametersDRes<DRes<SReal>>) layerParameters.get(0), input, useMdsonic, useMatrix))
                .seq((seq, x) -> {
                    activation.updateBuilder(seq);
                    return activation.activation(ActivationFunctions.Type.RELU, x);
                })
                .seq((seq, x) -> {  // Stage 1: The first basic block
                    Matrix<DRes<SReal>> identity = x;
                    DRes<Matrix<DRes<SReal>>> out;
                    out = seq.seq(new PrivacyConvLayer((ConvLayerParametersDRes<DRes<SReal>>) layerParameters.get(1), () -> x, useMdsonic, useMatrix));
                    activation.updateBuilder(seq);
                    out = activation.activation(ActivationFunctions.Type.RELU, out);
                    out = seq.seq(new PrivacyConvLayer((ConvLayerParametersDRes<DRes<SReal>>) layerParameters.get(2), out, useMdsonic, useMatrix));
                    out = seq.realLinAlg().add(out, () -> identity);
                    out = activation.activation(ActivationFunctions.Type.RELU, out);
                    return out;
                })
                .seq((seq, x) -> {  // Stage 1: The second basic block
                    Matrix<DRes<SReal>> identity = x;
                    DRes<Matrix<DRes<SReal>>> out;
                    out = seq.seq(new PrivacyConvLayer((ConvLayerParametersDRes<DRes<SReal>>) layerParameters.get(3), () -> x, useMdsonic, useMatrix));
                    activation.updateBuilder(seq);
                    out = activation.activation(ActivationFunctions.Type.RELU, out);
                    out = seq.seq(new PrivacyConvLayer((ConvLayerParametersDRes<DRes<SReal>>) layerParameters.get(4), out, useMdsonic, useMatrix));
                    out = seq.realLinAlg().add(out, () -> identity);
                    out = activation.activation(ActivationFunctions.Type.RELU, out);
                    return out;
                })
                .pairInPar((seq, x) -> {  // Stage 2: The first basic block
                    DRes<Matrix<DRes<SReal>>> out;
                    out = seq.seq(new PrivacyConvLayer((ConvLayerParametersDRes<DRes<SReal>>) layerParameters.get(5), () -> x, useMdsonic, useMatrix));
                    activation.updateBuilder(seq);
                    out = activation.activation(ActivationFunctions.Type.RELU, out);
                    out = seq.seq(new PrivacyConvLayer((ConvLayerParametersDRes<DRes<SReal>>) layerParameters.get(6), out, useMdsonic, useMatrix));
                    return out;
                } , (seq, x) -> {
                    return seq.seq(new PrivacyConvLayer((ConvLayerParametersDRes<DRes<SReal>>) layerParameters.get(7), () -> x, useMdsonic, useMatrix));
                }).seq((seq, pairs) -> {
                    DRes<Matrix<DRes<SReal>>> out;
                    out = seq.realLinAlg().add(() -> pairs.getFirst(), () -> pairs.getSecond());
                    activation.updateBuilder(seq);
                    out = activation.activation(ActivationFunctions.Type.RELU, out);
                    return out;
                })
                .seq((seq, x) -> {  // Stage 2: The second basic block
                    Matrix<DRes<SReal>> identity = x;
                    DRes<Matrix<DRes<SReal>>> out;
                    out = seq.seq(new PrivacyConvLayer((ConvLayerParametersDRes<DRes<SReal>>) layerParameters.get(8), () -> x, useMdsonic, useMatrix));
                    activation.updateBuilder(seq);
                    out = activation.activation(ActivationFunctions.Type.RELU, out);
                    out = seq.seq(new PrivacyConvLayer((ConvLayerParametersDRes<DRes<SReal>>) layerParameters.get(9), out, useMdsonic, useMatrix));
                    out = seq.realLinAlg().add(out, () -> identity);
                    out = activation.activation(ActivationFunctions.Type.RELU, out);
                    return out;
                })
                .pairInPar((seq, x) -> {  // Stage 3: The first basic block
                    DRes<Matrix<DRes<SReal>>> out;
                    out = seq.seq(new PrivacyConvLayer((ConvLayerParametersDRes<DRes<SReal>>) layerParameters.get(10), () -> x, useMdsonic, useMatrix));
                    activation.updateBuilder(seq);
                    out = activation.activation(ActivationFunctions.Type.RELU, out);
                    out = seq.seq(new PrivacyConvLayer((ConvLayerParametersDRes<DRes<SReal>>) layerParameters.get(11), out, useMdsonic, useMatrix));
                    return out;
                } , (seq, x) -> {
                    return seq.seq(new PrivacyConvLayer((ConvLayerParametersDRes<DRes<SReal>>) layerParameters.get(12), () -> x, useMdsonic, useMatrix));
                }).seq((seq, pairs) -> {
                    DRes<Matrix<DRes<SReal>>> out;
                    out = seq.realLinAlg().add(() -> pairs.getFirst(), () -> pairs.getSecond());
                    activation.updateBuilder(seq);
                    out = activation.activation(ActivationFunctions.Type.RELU, out);
                    return out;
                })
                .seq((seq, x) -> {  // Stage 3: The second basic block
                    Matrix<DRes<SReal>> identity = x;
                    DRes<Matrix<DRes<SReal>>> out;
                    out = seq.seq(new PrivacyConvLayer((ConvLayerParametersDRes<DRes<SReal>>) layerParameters.get(13), () -> x, useMdsonic, useMatrix));
                    activation.updateBuilder(seq);
                    out = activation.activation(ActivationFunctions.Type.RELU, out);
                    out = seq.seq(new PrivacyConvLayer((ConvLayerParametersDRes<DRes<SReal>>) layerParameters.get(14), out, useMdsonic, useMatrix));
                    out = seq.realLinAlg().add(out, () -> identity);
                    out = activation.activation(ActivationFunctions.Type.RELU, out);
                    return out;
                })
                .pairInPar((seq, x) -> {  // Stage 4: The first basic block
                    DRes<Matrix<DRes<SReal>>> out;
                    out = seq.seq(new PrivacyConvLayer((ConvLayerParametersDRes<DRes<SReal>>) layerParameters.get(15), () -> x, useMdsonic, useMatrix));
                    activation.updateBuilder(seq);
                    out = activation.activation(ActivationFunctions.Type.RELU, out);
                    out = seq.seq(new PrivacyConvLayer((ConvLayerParametersDRes<DRes<SReal>>) layerParameters.get(16), out, useMdsonic, useMatrix));
                    return out;
                } , (seq, x) -> {
                    return seq.seq(new PrivacyConvLayer((ConvLayerParametersDRes<DRes<SReal>>) layerParameters.get(17), () -> x, useMdsonic, useMatrix));
                }).seq((seq, pairs) -> {
                    DRes<Matrix<DRes<SReal>>> out;
                    out = seq.realLinAlg().add(() -> pairs.getFirst(), () -> pairs.getSecond());
                    activation.updateBuilder(seq);
                    out = activation.activation(ActivationFunctions.Type.RELU, out);
                    return out;
                })
                .seq((seq, x) -> {  // Stage 4: The second basic block
                    Matrix<DRes<SReal>> identity = x;
                    DRes<Matrix<DRes<SReal>>> out;
                    out = seq.seq(new PrivacyConvLayer((ConvLayerParametersDRes<DRes<SReal>>) layerParameters.get(18), () -> x, useMdsonic, useMatrix));
                    activation.updateBuilder(seq);
                    out = activation.activation(ActivationFunctions.Type.RELU, out);
                    out = seq.seq(new PrivacyConvLayer((ConvLayerParametersDRes<DRes<SReal>>) layerParameters.get(19), out, useMdsonic, useMatrix));
                    out = seq.realLinAlg().add(out, () -> identity);
                    out = activation.activation(ActivationFunctions.Type.RELU, out);
                    return out;
                })
                .seq((seq, x) -> {  // Average pooling
                    return seq.seq(new AvgpoolingLayer((AvgpoolingLayerParameters) layerParameters.get(20), () -> x));
                })
                .seq((seq, x) -> {  // Output
                    return seq.seq(new PrivacyFullyConnectedLayer((FullyConnectedLayerParametersDRes<DRes<SReal>>) layerParameters.get(21), () -> x, useMatrix));
                });
    }

}
