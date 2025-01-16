package dk.alexandra.fresco.demo.nn;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.SReal;

public interface ActivationFunctions extends ComputationDirectory {

  public enum Type {
    RELU, SIGMOID, SOFTMAX, IDENTITY
  };

  /**
   * Apply the activation function of the given type to a vector.
   *
   * @param type
   * @param v
   * @return
   */
  DRes<Matrix<DRes<SReal>>> activation(Type type, Matrix<DRes<SReal>> v);

  DRes<Matrix<DRes<SReal>>> activation(Type type, DRes<Matrix<DRes<SReal>>> v);

  /**
   * Apply the ReLU (recitified linear unit) to the given vector, which simply applies max(0,
   * v<sub>i</sub>) to all entries.
   *
   * @param v
   * @return
   */
  DRes<Matrix<DRes<SReal>>> relu(Matrix<DRes<SReal>> v);

  DRes<Matrix<DRes<SReal>>> relu(DRes<Matrix<DRes<SReal>>> v);

  /**
   * Applies the softmax function which maps maps the i'th coordinate v <sub>i</sub> to
   * exp(v<sub>i</sub>) / (exp(v<sub>1</sub>) + ... + exp(v <sub>n</sub>)).
   *
   * @param v
   * @return
   */
  DRes<Matrix<DRes<SReal>>> softmax(Matrix<DRes<SReal>> v);

  /**
   * Apply the identity function to a vector.
   *
   * @param v
   * @return
   */
  DRes<Matrix<DRes<SReal>>> identity(Matrix<DRes<SReal>> v);

  /**
   * Apply the sigmoid function, <i>x -> 1 / (1 + e<sup>-x</sup>)</i>, to all entries.
   *
   * @param v
   * @return
   */
  DRes<Matrix<DRes<SReal>>> sigmoid(Matrix<DRes<SReal>> v);

  void updateBuilder(ProtocolBuilderNumeric builder);
}
