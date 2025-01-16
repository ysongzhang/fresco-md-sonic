package dk.alexandra.fresco.demo.nn;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.SReal;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class MdsonicActivationFunctions implements ActivationFunctions {

  private ProtocolBuilderNumeric builder;

  public MdsonicActivationFunctions(ProtocolBuilderNumeric builder) {
    this.builder = builder;
  }

  @Override
  public void updateBuilder(ProtocolBuilderNumeric builder) {
    this.builder = builder;
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> activation(Type type, Matrix<DRes<SReal>> v) {
    switch (type) {
      case RELU:
        return relu(v);
      case IDENTITY:
        return identity(v);
      default:
        throw new IllegalArgumentException("Unsupported activation function type, " + type);
    }
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> activation(Type type, DRes<Matrix<DRes<SReal>>> v) {
    switch (type) {
      case RELU:
        return relu(v);
      default:
        throw new IllegalArgumentException("Unsupported activation function type, " + type);
    }
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> relu(Matrix<DRes<SReal>> v) {
    return builder.par(par -> {
      Matrix<DRes<SReal>> matrix = new Matrix<>(v.getHeight(), v.getWidth(), i -> {
        return new ArrayList<>(
                v.getRow(i).stream().map(x -> par.realNumericMdsonic().relu(x)).collect(Collectors.toCollection(ArrayList::new)));
      });
      return () -> matrix;
    });
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> relu(DRes<Matrix<DRes<SReal>>> v) {
    return builder.par(par -> {
      return par.seq(new ReluComputation(v));
    });
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> softmax(Matrix<DRes<SReal>> v) {
    throw new UnsupportedOperationException("Mdsonic: don't support to softmax now.");
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> sigmoid(Matrix<DRes<SReal>> v) {
    throw new UnsupportedOperationException("Mdsonic: don't support to sigmoid now.");
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> identity(Matrix<DRes<SReal>> v) {
    return builder.seq(seq -> () -> v);
  }
}

