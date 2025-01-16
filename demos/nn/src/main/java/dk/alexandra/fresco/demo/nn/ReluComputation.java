package dk.alexandra.fresco.demo.nn;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.SReal;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ReluComputation implements Computation<Matrix<DRes<SReal>>, ProtocolBuilderNumeric> {

  private DRes<Matrix<DRes<SReal>>> v;  // Column Vector

  public ReluComputation(DRes<Matrix<DRes<SReal>>> v) {
    this.v = v;
  }


  @Override
  public DRes<Matrix<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric builder) {
    int height = v.out().getHeight();
    int width = v.out().getWidth();
    Matrix<DRes<SReal>> input = v.out();
    return builder.par(par -> {
      Matrix<DRes<SReal>> matrix = new Matrix<>(height, width, i -> {
        return new ArrayList<>(
                input.getRow(i).stream().map(x -> par.realNumericMdsonic().relu(x)).collect(Collectors.toCollection(ArrayList::new)));
      });
      return () -> matrix;
    });
  }

}

