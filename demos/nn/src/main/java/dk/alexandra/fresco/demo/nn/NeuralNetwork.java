package dk.alexandra.fresco.demo.nn;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.SReal;

import java.math.BigDecimal;
import java.util.List;

public class NeuralNetwork implements Computation<Matrix<DRes<SReal>>, ProtocolBuilderNumeric> {

    private List<LayerParameters> layerParameters;
    private DRes<Matrix<DRes<SReal>>> input;
    private final boolean privacyNN;
    private final boolean useMdsonic;
    private final boolean useMatrix;
    private DRes<Matrix<DRes<SReal>>> x;

    public NeuralNetwork(List<LayerParameters> layers,
                         DRes<Matrix<DRes<SReal>>> input, boolean privacyNN, boolean useMdsonic, boolean useMatrix) {
        this.layerParameters = layers;
        this.input = input;
        this.privacyNN = privacyNN;
        this.useMdsonic = useMdsonic;
        this.useMatrix = useMatrix;
    }

    @Override
    public DRes<Matrix<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric builder) {
        if (privacyNN) {
            return builder.seq(seq -> {
                x = input;
                for (LayerParameters parameters : layerParameters) {
                    if (parameters.LayerType.equals("CNN"))
                    {
                        x = seq.seq(new PrivacyConvLayer((ConvLayerParametersDRes<DRes<SReal>>) parameters, x, useMdsonic, useMatrix));
                    }
                    else if (parameters.LayerType.equals("FULL"))
                    {
                        x = seq.seq(new PrivacyFullyConnectedLayer((FullyConnectedLayerParametersDRes<DRes<SReal>>) parameters, x, useMatrix));
                    }
                    else if (parameters.LayerType.equals("AvgPool"))
                    {
                        x = seq.seq(new AvgpoolingLayer((AvgpoolingLayerParameters) parameters, x));
                    }
                    else if(parameters.LayerType.equals("MaxPool")) {
                        x = seq.seq(new MaxpoolingLayer((MaxpoolingLayerParameters) parameters, x, useMdsonic));
                    } else if (parameters.LayerType.equals("ACT")) {
                        if (useMdsonic) {
                            ActivationFunctions activation = new MdsonicActivationFunctions(seq);
                            ActiveLayerParameters activeLayerParameters = (ActiveLayerParameters) parameters;
                            x = activation.activation(activeLayerParameters.getActivation(), x);
                        } else {
                            ActivationFunctions activation = new DefaultActivationFunctions(seq);
                            ActiveLayerParameters activeLayerParameters = (ActiveLayerParameters) parameters;
                            x = activation.activation(activeLayerParameters.getActivation(), x);
                        }

                    }
                }
                return x;
            });
        } else {
            return builder.seq(seq -> {
                x = input;
                for (LayerParameters parameters : layerParameters) {
                    if (parameters.LayerType.equals("CNN"))
                    {
                        x = seq.seq(new PublicConvLayer((ConvLayerParameters<BigDecimal>) parameters, x));
                    }
                    else if (parameters.LayerType.equals("FULL"))
                    {
                        x = seq.seq(new PublicFullyConnectedLayer((FullyConnectedLayerParameters<BigDecimal>) parameters, x));
                    }
                    else if (parameters.LayerType.equals("AvgPool"))
                    {
                        x = seq.seq(new AvgpoolingLayer((AvgpoolingLayerParameters) parameters, x));
                    }
                    else if(parameters.LayerType.equals("MaxPool")) {
                        x = seq.seq(new MaxpoolingLayer((MaxpoolingLayerParameters) parameters, x, useMdsonic));
                    } else if (parameters.LayerType.equals("ACT")) {
                        if (useMdsonic) {
                            ActivationFunctions activation = new MdsonicActivationFunctions(seq);
                            ActiveLayerParameters activeLayerParameters = (ActiveLayerParameters) parameters;
                            x = activation.activation(activeLayerParameters.getActivation(), x);
                        } else {
                            ActivationFunctions activation = new DefaultActivationFunctions(seq);
                            ActiveLayerParameters activeLayerParameters = (ActiveLayerParameters) parameters;
                            x = activation.activation(activeLayerParameters.getActivation(), x);
                        }
                    }
                }
                return x;
            });
        }
    }

}
