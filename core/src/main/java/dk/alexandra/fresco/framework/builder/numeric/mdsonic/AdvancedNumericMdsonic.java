package dk.alexandra.fresco.framework.builder.numeric.mdsonic;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;

/**
 * Interface for advanced functionality applicable to numeric type applications.
 */
public interface AdvancedNumericMdsonic extends ComputationDirectory {


  /**
   * Creates a random daBit.
   *
   * @return A container holding the bit string once evaluated.
   */
  DRes<DaBit> daBit();

  /**
   * Creates a random edaBit without FM.
   *
   * @return A container holding the bit mask
   */
  DRes<FMedaBit> edaBit();

  DRes<FMedaBit> edaBit(int FMLength, int FMDegree);

  /**
   * Right-shifts input by {@code shifts}. <p>Note that this is a probabilistic method which may
   * produce an error in the least-significant bit.</p>
   *
   * @param input secret value to right shift
   * @param shifts number of shifts
   * @return shifted result
   */
  DRes<SInt> truncate(DRes<SInt> input, int shifts);

  DRes<SInt> truncate(DRes<SInt> input, int shifts, DRes<SInt> truncationR);  // input:MSS, truncationR:ASS

  DRes<SInt> mux(DRes<SInt> input, DRes<SBool> bit);

  /**
   * Creates truncation pair ({@link TruncationPair}). <p>This method may rely on pre-processed
   * material in which case it should be overridden by backend suits.</p>
   *
   * @param d number of shifts in truncation pair
   */
  DRes<TruncationPair> generateTruncationPair(int d);

  DRes<MatrixTruncationPair> generateMatrixTruncationPair(int d, int height, int width);

  /**
   * Generic representation of a truncation pair. <p> A truncation pair is pre-processing material
   * used for probabilistic truncation. A truncation pair consists of a value r and r^{prime} such
   * that r^{prime} is a random element and r = r^{prime} / 2^{d}, i.e., r right-shifted by d.</p>
   */
  class TruncationPair {

    private final DRes<SInt> rPrime;
    private final DRes<SInt> r;

    public TruncationPair(
            DRes<SInt> rPrime,
            DRes<SInt> r) {
      this.rPrime = rPrime;
      this.r = r;
    }

    public DRes<SInt> getRPrime() {
      return rPrime;
    }

    public DRes<SInt> getR() {
      return r;
    }

  }

  class MatrixTruncationPair {

    private final Matrix<DRes<SInt>> rPrime;
    private final Matrix<DRes<SInt>> r;

    public MatrixTruncationPair(
            Matrix<DRes<SInt>> rPrime,
            Matrix<DRes<SInt>> r) {
      this.rPrime = rPrime;
      this.r = r;
    }

    public Matrix<DRes<SInt>> getRPrime() {
      return rPrime;
    }

    public Matrix<DRes<SInt>> getR() {
      return r;
    }

  }

}
