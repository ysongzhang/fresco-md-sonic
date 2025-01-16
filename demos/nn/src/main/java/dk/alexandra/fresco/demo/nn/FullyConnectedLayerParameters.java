package dk.alexandra.fresco.demo.nn;

import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.demo.nn.ActivationFunctions.Type;

public class FullyConnectedLayerParameters<T> extends LayerParameters  {

  private Matrix<T> weights;
  private Matrix<T> bias;

  public FullyConnectedLayerParameters(Matrix<T> weights, Matrix<T> bias) {
      super("FULL");

      if (bias.getWidth() != 1) {
      throw new IllegalArgumentException(
          "Bias must be a column vector. Has width " + bias.getWidth() + " != 1.");
    }

    if (weights.getHeight() != bias.getHeight()) {
      throw new IllegalArgumentException("Height of weight matrix (" + weights.getHeight()
          + ") must be equal to height of bias vector (" + bias.getHeight() + ")");
    }

    this.weights = weights;
    this.bias = bias;
  }

  public Matrix<T> getWeights() {
    return weights;
  }

  public Matrix<T> getBias() {
    return bias;
  }

  public int getInputs() {
    return weights.getWidth();
  }

  public int getOutputs() {
    return bias.getHeight();
  }
}
