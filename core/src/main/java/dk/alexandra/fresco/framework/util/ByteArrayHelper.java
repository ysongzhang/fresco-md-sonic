package dk.alexandra.fresco.framework.util;

import cc.redberry.rings.poly.univar.UnivariatePolynomialZp64;

import java.util.List;
import java.util.stream.IntStream;

public class ByteArrayHelper {

  private ByteArrayHelper() {
    // Should not be instantiated
  }

  /**
   * Returns the bit at a given index, reading from left-to-right, from a byte array.
   *
   * @param input an array from which to retrieve a bit
   * @param index the index of the bit
   * @return Returns the bit at the given index, reading from left-to-right
   */
  public static boolean getBit(byte[] input, int index) {
    if (index < 0) {
      throw new IllegalAccessError("Bit index must not be negative.");
    }
    byte currentByte = (byte) (input[index / 8] >> (7 - (index % 8)));
    boolean choiceBit = false;
    if ((currentByte & 1) == 1) {
      choiceBit = true;
    }
    return choiceBit;
  }

  public static boolean[] getBits(byte[] input) {  // Keep the order
    boolean[] booleans = new boolean[input.length * 8];
    for (int i = 0; i < booleans.length; i++) {
      booleans[i] = getBit(input, i);
    }
    return booleans;
  }

  /**
   * Sets a bit at a given index to a given boolean value.
   *
   * @param input an array in which to set a bit
   * @param index index of the bit to set
   * @param choice value to set the given bit to
   */
  public static void setBit(byte[] input, int index, boolean choice) {
    if (index < 0) {
      throw new IllegalAccessError("Bit index must not be negative.");
    }
    if (choice) {
      // We read bits from left to right, hence the 7 - x.
      // Put a 1 in the correct position of a
      // zero-byte and OR it into the correct byte to ensure that the position
      // becomes 1 no matter whether it is currently set or not.
      input[index / 8] |= ((byte) 0x01) << (7 - (index % 8));
    } else {
      // Construct an all 1-byte, then construct a byte like above, where only
      // the correct position is set to 1. We XOR these bytes to get a byte
      // which is all 1's except in the correct position. We AND this into the
      // correct byte to ensure that only the correct positions gets set to 0.
      input[index / 8] &= 0xFF ^ ((byte) 0x01) << (7 - (index % 8));
    }
  }

  public static void setBits(byte[] input, boolean[] choices) {
    for (int i = 0; i < choices.length; i++) {
      setBit(input, i, choices[i]);
    }
  }

  /**
   * Computes the XOR of each element in a list of byte arrays. This is done in-place in "vector1".
   * If the lists are not of equal length or any of the byte arrays are not of equal size, then an
   * IllegalArgument exception is thrown
   *
   * @param vector1 First input list
   * @param vector2 Second input list
   */
  public static void xor(List<byte[]> vector1, List<byte[]> vector2) {
    if (vector1.size() != vector2.size()) {
      throw new IllegalArgumentException("The vectors are not of equal length");
    }
    for (int i = 0; i < vector1.size(); i++) {
      xor(vector1.get(i), vector2.get(i));
    }
  }

  /**
   * Computes the XOR of each element in a byte array. This is done in-place in "arr1". If the byte
   * arrays are not of equal size, then an IllegalArgument exception is thrown
   *
   * @param arr1 First byte array
   * @param arr2 Second byte array
   */
  public static void xor(byte[] arr1, byte[] arr2) {
    int bytesNeeded = arr1.length;
    if (bytesNeeded != arr2.length) {
      throw new IllegalArgumentException("The byte arrays are not of equal length");
    }
    for (int i = 0; i < bytesNeeded; i++) {
      // Compute the XOR (addition in GF2) of arr1 and arr2
      arr1[i] ^= arr2[i];
    }
  }

  /**
   * Shifts a byte array by a given number of positions.
   *
   * @param input the input array
   * @param output an array in which to store the output
   * @param positions number of positions to shift
   */
  public static void shiftArray(byte[] input, byte[] output, int positions) {
    for (int i = 0; i < input.length * 8; i++) {
      setBit(output, positions + i, getBit(input, i));
    }
  }

  public static String BooleanArrayToGFString(boolean[] input) { // input is big-endian
    StringBuilder polynomial = new StringBuilder();
    boolean isFirstTerm = true;

    for (int i = 0; i < input.length; i++) {
      if (input[i]) {
        if (!isFirstTerm) {
          polynomial.append("+");
        }
        if (i == input.length - 1) {
          polynomial.append("1");
        } else {
          polynomial.append("x^").append(input.length - i - 1);
        }
        isFirstTerm = false;
      }
    }

    if (isFirstTerm) { // 处理所有项都为0的情况
      polynomial.append("0");
    }

    return polynomial.toString();
  }

  public static boolean[] GFStringToBooleanArray(String input, int size) { // output is big-endian
    boolean[] booleans = new boolean[size];
    String[] terms = input.split("\\+");
    for (String term : terms) {
      int index = getIndex(term);
      if (index >= 0) {
        booleans[size - index - 1] = true;
      }
    }
    return booleans;
  }

  private static int getIndex(String term) {
    if (term.equals("1")) {
      return 0;
    } else if (term.equals("x")) {
      return 1;
    } else if (term.startsWith("x^")) {
      return Integer.parseInt(term.substring(2));
    }
    return -1;
  }

  public static UnivariatePolynomialZp64 littleEndianByteArrayToGf2x(byte[] value) {
    int lBitLength = value.length * Byte.SIZE;
    long[] coefficientLongArray = IntStream.range(0, lBitLength)
            // 字节数组为小端表示，转换为GF(2^l)多项式时，最高位是最低系数
            .mapToLong(degree -> getBit(value, degree) ? 1L : 0L)
            .toArray();
    return UnivariatePolynomialZp64.create(2L, coefficientLongArray);
  }

  public static UnivariatePolynomialZp64 bigEndianByteArrayToGf2x(byte[] value) {
    int lBitLength = value.length * Byte.SIZE;
    long[] coefficientLongArray = IntStream.range(0, lBitLength)
            // 字节数组为小端表示，转换为GF(2^l)多项式时，最高位是最低系数
            .mapToLong(degree -> getBit(value, lBitLength - 1 - degree) ? 1L : 0L)
            .toArray();
    return UnivariatePolynomialZp64.create(2L, coefficientLongArray);
  }

}
