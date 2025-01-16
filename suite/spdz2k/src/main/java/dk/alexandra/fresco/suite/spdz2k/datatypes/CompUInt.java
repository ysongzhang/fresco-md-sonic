package dk.alexandra.fresco.suite.spdz2k.datatypes;

import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.OInt;


/**
 * An unsigned integer conceptually composed of two other unsigned integers. <p>Composite integers
 * have a bit length of t = s + k, where the s most significant bits can be viewed as a s-bit
 * integer of type HighT and the k least significant bits as a k-bit integer of type LowT. The
 * underlying representation is big endian.</p>
 */
public interface CompUInt<
    HighT extends UInt<HighT>,
    LowT extends UInt<LowT>,
    CompT extends UInt<CompT>> extends UInt<CompT>, OInt {

  /**
   * Get the s most significant bits as an unsigned integer of type {@link HighT}.
   */
  HighT getMostSignificant();

  /**
   * Get the k least significant bits as an unsigned integer of type {@link LowT}.
   */
  LowT getLeastSignificant();

  /**
   * Get the s least significant bits as an unsigned integer of type {@link HighT}.
   */
  HighT getLeastSignificantAsHigh();

  /**
   * Left shift by n bits where n < k. <p>Result of a shift by n >= k is undefined. A shift for n <=
   * 0 returns this, unchanged.</p>
   */
  CompT shiftLeftSmall(int n);

  /**
   * Unsigned right shift by n bits where n < k. <p>Result of a shift by n >= k is undefined. A
   * shift for n <= 0 returns this, unchanged.</p>
   */
  CompT shiftRightSmall(int n);

  /**
   * Unsigned right shift by n bits where n < k in the least significant bits only.
   */
  CompT shiftRightLowOnly(int n);

  /**
   * 用于有符号数的右移，算术右移
   */
  CompT shiftRightLowOnlySigned(int n);

  /**
   * Left-shift the k least significant bits by k.
   */
  CompT shiftLowIntoHigh();

  /**
   * Clears all bits above bitPos, i.e., (k + s) - bitPos most significant bits. <p>Analogous to
   * computing this mod 2^{bitPos}.</p>
   */
  CompT clearAboveBitAt(int bitPos);

  /**
   * Sets s most significant bits to 0.
   */
  CompT clearHighBits();

  /**
   * Converts this to bit representation.
   */
  CompT toBitRep();

  /**
   * Converts this to arithmetic representation.
   */
  CompT toArithmeticRep();

  /**
   * Computes product of this and value.
   */
  CompT multiplyByBit(int value);

  /**
   * Returns result of {@link #testBit(int)} as UInt.
   */
  CompT testBitAsUInt(int bit);

  boolean testBit(int bit);

  /**
   * Returns bit value of this as integer.
   */
  default int bitValue() {
    throw new IllegalStateException("Only supported by bit representations");
  }

  /**
   * Get length of least significant bit segment, i.e., k.
   */
  int getLowBitLength();

  /**
   * Get length of most significant bit segment, i.e., s.
   */
  int getHighBitLength();

  /**
   * Returns total bit length, i.e., k + s.
   */
  default int getCompositeBitLength() {
    return getHighBitLength() + getLowBitLength();
  }

  @Override
  default int getBitLength() {
    return getCompositeBitLength();
  }

  /**
   * 返回真实的信息位，用在18版本的公开阶段.
   */
  default byte[] serializeLeastSignificant() {
    return getLeastSignificant().toByteArray();
  }

  /**
   * 返回全部的k + s个比特，用在22版本的公开阶段.
   */
  default byte[] serializeAll() {
    byte[] highByte = getMostSignificant().toByteArray();
    byte[] lowByte = getLeastSignificant().toByteArray();
    byte[] result = new byte[highByte.length + lowByte.length];  // length = getCompositeBitLength() / Byte.SIZE
    System.arraycopy(highByte, 0, result, 0, highByte.length);
    System.arraycopy(lowByte, 0, result, highByte.length, lowByte.length);
    return result;
  }

  /**
   * Util for padding a byte array up to the required bit length. <p>Since we are working with
   * unsigned ints, the first byte of the passed in array is discarded if it's a zero byte.</p>
   */
  static byte[] pad(byte[] bytes, int requiredBitLength) {
    byte[] padded = new byte[requiredBitLength / Byte.SIZE];
    // potentially drop byte containing sign bit
    boolean dropSignBitByte = (bytes[0] == 0x00);
    int bytesLen = dropSignBitByte ? bytes.length - 1 : bytes.length;
    if (bytesLen > padded.length) {
      throw new IllegalArgumentException("Exceeds capacity");
    }
    int srcPos = dropSignBitByte ? 1 : 0;
    System.arraycopy(bytes, srcPos, padded, padded.length - bytesLen, bytesLen);
    return padded;
  }

}
