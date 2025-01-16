package dk.alexandra.fresco.lib.real.mdsonic;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.lib.real.SReal;

import java.math.BigDecimal;
import java.util.List;

public interface AdvancedRealNumericMdsonic extends ComputationDirectory {

  /**
   * Calculate the sum of all terms in a list.
   *
   * @param terms List of secret values
   * @return A deferred result computing the sum of the terms
   */
  DRes<SReal> sum(List<DRes<SReal>> terms);

  /**
   * Calculate the inner product of two secret vectors.
   *
   * @param a List of secret values
   * @param b List of secret values
   * @return A deferred result computing computing the inner product of the two lists
   */
  DRes<SReal> innerProduct(List<DRes<SReal>> a, List<DRes<SReal>> b);

  /**
   * Calculate the inner product of a public and a secret vector.
   *
   * @param a List of public values
   * @param b List of secret values
   * @return A deferred result computing computing the inner product of the two lists
   */
  DRes<SReal> innerProductWithPublicPart(List<BigDecimal> a, List<DRes<SReal>> b);

}
