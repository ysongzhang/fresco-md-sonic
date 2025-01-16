package dk.alexandra.fresco.suite.mdsonic.resource.storage;

import dk.alexandra.fresco.framework.builder.numeric.mdsonic.AdvancedNumericMdsonic.MatrixTruncationPair;
import dk.alexandra.fresco.framework.builder.numeric.mdsonic.AdvancedNumericMdsonic.TruncationPair;
import dk.alexandra.fresco.framework.builder.numeric.mdsonic.DaBit;
import dk.alexandra.fresco.framework.builder.numeric.mdsonic.FMedaBit;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;

/**
 * Interface for a supplier of pre-processing material. <p>Material includes random elements shares,
 * random bit shares, and multiplication triple shares.</p>
 */
public interface MdsonicDataSupplier<T extends MdsonicCompUInt<?, ?, T>, L extends MdsonicGF<L>> {

  /**
   * Supplies the next full multiplication triple.
   *
   * @return the next new triple
   */
  MdsonicTriple<T, MdsonicASIntArithmetic<T>> getNextTripleSharesFull();

  MdsonicASIntArithmetic<T> getNextTripleProductShare();

  /**
   * Supplies the next boolean multiplication triple.
   *
   * @return the next new triple
   */
  MdsonicBitTriple<L, MdsonicASBoolBoolean<L>> getNextBitTripleShares();

  MdsonicWRBitTriple<L, MdsonicASBoolBoolean<L>> getNextWRBitTripleShares();

  MdsonicASBoolBoolean<L> getNextBitTripleProductShare();

  /**
   * Supplies the next inputmask for a given input player.
   *
   * @param towardPlayerId the id of the input player
   * @return the appropriate input mask
   */
  MdsonicInputMask<T> getNextInputMask(int towardPlayerId);

  /**
   * Supplies the next bit (SInt representing value in {0, 1}).
   */
  MdsonicASIntArithmetic<T> getNextBitShareAsArithmetic();

  /**
   * Supplies the next bit (SBool represent).
   */
  MdsonicASBoolBoolean<L> getNextBitShare();  // Bool represent

  /**
   * Returns the player's share of the mac key.
   */
  T getSecretSharedKey();

  L getSecretSharedKeyBoolean();

  /**
   * Supplies the next random field element.
   */
  MdsonicASIntArithmetic<T> getNextRandomElementShare();

  /**
   * Supplies the next truncation pair (r^{prime}, r) where r = r^{prime} / 2^{d}.
   * ASS
   * @param d number of shifts
   */
  TruncationPair getNextTruncationPair(int d);

  MatrixTruncationPair getNextMatrixTruncationPair(int d, int height, int width);

  DaBit getNextMaskedDaBit();  // bool: ASS + arithmetic: MSS

  DaBit getNextDaBit();

  FMedaBit getNextFMedaBit(int l, int d);  // l表示FMLength, d表示FMDegree

  FMedaBit getNextEdaBit();

  MdsonicASIntArithmetic<T> getNextInnerProductShare();

  Matrix<MdsonicASIntArithmetic<T>> getNextMatrixProductShare(int height, int width);

  MdsonicMatrixTriple<T, MdsonicASIntArithmetic<T>> getNextMatrixTripleShares(int n1, int n2, int n3);

}
