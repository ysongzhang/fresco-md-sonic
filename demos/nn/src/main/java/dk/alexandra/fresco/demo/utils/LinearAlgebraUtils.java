package dk.alexandra.fresco.demo.utils;

import dk.alexandra.fresco.lib.collections.Matrix;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LinearAlgebraUtils {

  private double[] convertRow(ArrayList<BigDecimal> row) {
    int n = row.size();
    double[] out = new double[n];
    for (int j = 0; j < n; j++) {
      out[j] = row.get(j).doubleValue();
    }
    return out;
  }

  private double[][] convertRows(ArrayList<ArrayList<BigDecimal>> rows) {
    int m = rows.size();
    double[][] out = new double[m][];
    for (int i = 0; i < m; i++) {
      out[i] = convertRow(rows.get(i));
    }
    return out;
  }

  /**
   * Convert a {@link Matrix} to a {@link RealMatrix}.
   * 
   * @param a
   * @return
   */
  public RealMatrix convert(Matrix<BigDecimal> a) {
    return new Array2DRowRealMatrix(convertRows(a.getRows()));
  }

  /**
   * Create a n x 1-matrix from a list of length n with the same entries.
   * 
   * @param list
   * @return
   */
  public <T> Matrix<T> createColumnVector(List<T> list) {
    return new Matrix<>(list.size(), 1,
        i -> new ArrayList<>(Collections.singletonList(list.get(i))));

  }

}
