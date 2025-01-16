package dk.alexandra.fresco.demo.nn;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.lib.collections.Matrix;

public class FullyConnectedLayerParametersDRes<T> extends LayerParameters  {

  private DRes<Matrix<T>> weights;
  private DRes<Matrix<T>> bias;

  public FullyConnectedLayerParametersDRes(DRes<Matrix<T>> weights, DRes<Matrix<T>> bias) {
      super("FULL");

    this.weights = weights;
    this.bias = bias;
  }

  public DRes<Matrix<T>> getWeights() {
    return weights;
  }

  public DRes<Matrix<T>> getBias() {
    return bias;
  }
}
