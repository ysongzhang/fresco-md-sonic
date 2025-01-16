package dk.alexandra.fresco.suite.mdml.resource.storage;

import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric.TruncationPair;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric.MatrixTruncationPair;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.suite.mdml.datatypes.*;

/**
 * Interface for a supplier of pre-processing material. <p>Material includes random elements shares,
 * random bit shares, and multiplication triple shares.</p>
 */
public interface MdmlDataSupplier<T extends MdmlCompUInt<?, ?, T>> {

  /**
   * Supplies the next full multiplication triple.
   *
   * @return the next new triple
   */
  MdmlTriple<T, MdmlASIntArithmetic<T>> getNextTripleSharesFull();

  /**
   * Supplies the next boolean multiplication triple.
   *
   * @return the next new triple
   */
  MdmlTriple<T, MdmlSIntBoolean<T>> getNextBitTripleShares();

  /**
   * Supplies the next inputmask for a given input player.
   *
   * @param towardPlayerId the id of the input player
   * @return the appropriate input mask
   */
  MdmlInputMask<T> getNextInputMask(int towardPlayerId);

  /**
   * Supplies the next bit (SInt representing value in {0, 1}).
   */
  MdmlASIntArithmetic<T> getNextBitShare();

  /**
   * Returns the player's share of the mac key.
   */
  T getSecretSharedKey();

  /**
   * Supplies the next random field element.
   */
  MdmlASIntArithmetic<T> getNextRandomElementShare();

  /**
   * Supplies the next truncation pair (r^{prime}, r) where r = r^{prime} / 2^{d}.
   *
   * @param d number of shifts
   */
  TruncationPair getNextTruncationPair(int d);

  MatrixTruncationPair getNextMatrixTruncationPair(int d, int height, int width);

  MdmlMatrixTriple<T, MdmlASIntArithmetic<T>> getNextMatrixTripleShares(int n1, int n2, int n3);

  T getNextOpenedDelta();

  Matrix<T> getNextMatrixOpenedDelta(int n1, int n2);

}
