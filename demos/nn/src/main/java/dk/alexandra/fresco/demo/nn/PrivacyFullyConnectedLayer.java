package dk.alexandra.fresco.demo.nn;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.SReal;

/**
 * This class represents a fully connected layer in a feed-forward neural network.
 */
public class PrivacyFullyConnectedLayer implements Layer {  // Output Column Vector

  private DRes<Matrix<DRes<SReal>>> v;  // Column Vector
  private FullyConnectedLayerParametersDRes<DRes<SReal>> parameters;
  final private boolean useMatrix;

  public PrivacyFullyConnectedLayer(FullyConnectedLayerParametersDRes<DRes<SReal>> parameters,
                                    DRes<Matrix<DRes<SReal>>> v, boolean useMatrix) {
    this.parameters = parameters;
    this.v = v;
    this.useMatrix = useMatrix;
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(r1 -> {
      return r1.realLinAlg().add(parameters.getBias(),
          r1.realLinAlg().mult(parameters.getWeights(), v, useMatrix));
    });
  }

}
