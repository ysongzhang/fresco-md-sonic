package dk.alexandra.fresco.demo.utils;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.SReal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class DefaultActivationFunctionsBench implements ActivationFunctionsBench {

  private ProtocolBuilderNumeric builder;

  public DefaultActivationFunctionsBench(ProtocolBuilderNumeric builder) {
    this.builder = builder;
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> activation(Type type, Matrix<DRes<SReal>> v) {
    switch (type) {
      case RELU:
        return relu(v);
      case SIGMOID:
        return sigmoid(v);
      case IDENTITY:
        return identity(v);
      case SOFTMAX:
        return softmax(v);
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

  private BiFunction<ProtocolBuilderNumeric, DRes<SReal>, DRes<SReal>> ebeRelu() {
    return (builder, x) -> {
      return builder.seq(r1 -> {
        DRes<SInt> compare = r1.realNumeric().leq(r1.realNumeric().known(BigDecimal.ZERO), x);
        return r1.realNumeric().mult(x, r1.realNumeric().fromSInt(compare));
      });
    };
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> relu(Matrix<DRes<SReal>> v) {
    return ebe(v, ebeRelu());
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> relu(DRes<Matrix<DRes<SReal>>> v) {
    return ebeRes(v, ebeRelu());
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> softmax(Matrix<DRes<SReal>> v) {
    throw new UnsupportedOperationException("Don't support to softmax now.");
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> sigmoid(Matrix<DRes<SReal>> v) {
    return ebe(v, ebeSigmoid());
  }

  @Override
  public DRes<Matrix<DRes<SReal>>> identity(Matrix<DRes<SReal>> v) {
    return builder.seq(seq -> () -> v);
  }

  private BiFunction<ProtocolBuilderNumeric, DRes<SReal>, DRes<SReal>> ebeSigmoid() {
    return (builder, x) -> {
      return builder.seq(r1 -> {
        DRes<SReal> exp = r1.realAdvanced().exp(x);
        return r1.realNumeric().div(exp, r1.realNumeric().add(BigDecimal.ONE, exp));
      });
    };
  }

  /**
   * Apply the given map to all elements in the given matrix.
   *
   * @param m
   * @param map
   * @return
   */
  private DRes<Matrix<DRes<SReal>>> ebe(Matrix<DRes<SReal>> m,
                                        BiFunction<ProtocolBuilderNumeric, DRes<SReal>, DRes<SReal>> map) {
    return builder.par(par -> {
      Matrix<DRes<SReal>> matrix = new Matrix<>(m.getHeight(), m.getWidth(), i -> {
        return new ArrayList<>(
                m.getRow(i).stream().map(x -> map.apply(par, x)).collect(Collectors.toCollection(ArrayList::new)));
      });
      return () -> matrix;
    });
  }
  private DRes<Matrix<DRes<SReal>>> ebeRes(DRes<Matrix<DRes<SReal>>> m,
                                           BiFunction<ProtocolBuilderNumeric, DRes<SReal>, DRes<SReal>> map) {
    return builder.par(par -> {
      Matrix<DRes<SReal>> mOut = m.out(); // Fix the lagging bug!!!
      Matrix<DRes<SReal>> matrix = new Matrix<>(mOut.getHeight(), mOut.getWidth(), i -> {
        return new ArrayList<>(
                mOut.getRow(i).stream().map(x -> map.apply(par, x)).collect(Collectors.toCollection(ArrayList::new)));
      });
      return () -> matrix;
    });
  }
}

