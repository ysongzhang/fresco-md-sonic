package dk.alexandra.fresco.framework.builder.numeric.mdsonic;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.framework.util.SBoolPair;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.SInt;

import java.util.List;

/**
 * Interface for comparing numeric values in MD-SONIC.
 */
public interface ComparisonMdsonic extends ComputationDirectory {

  /**
   * Computes if x < y in MD-SONIC.
   *
   * @param x the first input
   * @param y the second input
   * @return A deferred result computing x <= y. Result will be either [1] (true) or [0] (false).
   */
  DRes<SBool> compareLT(DRes<SInt> x, DRes<SInt> y);

  DRes<SInt> relu(DRes<SInt> input);

  /**
   * Computes if x <= y.
   *
   * @param x the first input
   * @param y the second input
   * @return A deferred result computing x <= y. Result will be either [1] (true) or [0] (false).
   */
  DRes<SBool> compareLEQ(DRes<SInt> x, DRes<SInt> y);

  /**
   * Computes if the bit decomposition of an open value is less than the bit decomposition of a
   * secret value.
   *
   * @param openValue open value which will be decomposed into bits and compared to secretBits
   * @param secretBits secret value decomposed into bits
   */
  default DRes<SBool> compareLTBits(DRes<OInt> openValue, DRes<List<DRes<SBool>>> secretBits) {
    throw new UnsupportedOperationException();
  }

  /**
   * Method used internally in less-than protocol. <p>This is only here since we need a way to plug
   * in a backend specific native protocol for it.</p>
   */
  default DRes<List<SBoolPair>> carry(List<SBoolPair> bitPairs) {
    throw new UnsupportedOperationException();
  }

  /**
   * Computes x == y.
   *
   * @param x the first input
   * @param y the second input
   * @param bitlength the amount of bits to do the equality test on. Must be less than or equal to
   * the max bitlength allowed
   * @return A deferred result computing x' == y'. Where x' and y' represent the {@code bitlength}
   * least significant bits of x, respectively y. Result will be either [1] (true) or [0] (false).
   */
  DRes<SBool> equals(DRes<SInt> x, DRes<SInt> y, int bitlength);

  DRes<SBool> equals(DRes<SInt> x, DRes<SInt> y);

  /**
   * Test for equality with zero for a bitLength-bit number (positive or negative)
   *
   * @param x the value to test against zero
   * @param bitlength the amount of bits to do the zero-test on. Must be less than or equal to the
   * modulus bitlength
   * @return A deferred result computing x' == 0 where x' is the {@code bitlength} least significant
   * bits of x. Result will be either [1] (true) or [0] (false)
   */
  DRes<SBool> compareZero(DRes<SInt> x, int bitlength);

}
