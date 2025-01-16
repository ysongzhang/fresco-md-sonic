package dk.alexandra.fresco.suite.mdsonic.datatypes;

import dk.alexandra.fresco.framework.util.ByteAndBitConverter;

import java.math.BigInteger;

/**
 * In CompuInt64, the real information bit is only stored in the last bit of the value, and the 32-bit security bits are adjacent to the real information bit.</p>
 */
public class MdsonicCompUInt64Bit extends MdsonicCompUInt64 {

  public MdsonicCompUInt64Bit(int high, int bit) {
    super((MdsonicUInt.toUnLong(high) << 1) | MdsonicUInt.toUnLong(bit));
  }  // Boolean represents using the last 33 bits to store real information

  public MdsonicCompUInt64Bit(long value) {
    super(value);
  }

  @Override
  public MdsonicCompUInt64 toBitRep() {
    throw new IllegalStateException("Already in bit form.");
  }

  @Override
  public MdsonicCompUInt64 toArithmeticRep() {
    return new MdsonicCompUInt64(value << 31);
  }  // Note that after converting to arithmetic representation, the first 33 bits are used to store the true information

  @Override
  public BigInteger toBigInteger() {
    return toArithmeticRep().toBigInteger();
  }

  @Override
  public MdsonicCompUInt64 multiply(MdsonicCompUInt64 other) {
    return new MdsonicCompUInt64Bit(value * other.value);
  }

  @Override
  public MdsonicCompUInt64 add(MdsonicCompUInt64 other) {
    return new MdsonicCompUInt64Bit(value + other.value);
  }

  @Override
  public String toString() {
    return toBigInteger().toString() + "B";
  }

  @Override
  public MdsonicCompUInt64 subtract(MdsonicCompUInt64 other) {
    throw new UnsupportedOperationException("Subtraction not supported by bit representation");
  }

  @Override
  public MdsonicCompUInt64 negate() {
    throw new UnsupportedOperationException("Negation not supported by bit representation");
  }

  @Override
  public int bitValue() {
    return (int) (value & 1L);
  }

  @Override
  public byte[] serializeLeastSignificant() {
    return new byte[]{(byte) bitValue()};
  }

  @Override
  public byte[] serializeAll() {  // Real information bit occupies a single byte
    byte bit = (byte) bitValue();  // 1 bit, Use one byte storage, where all the first bits are 0
    byte[] high = ByteAndBitConverter.toByteArray((int) (value >>> 1)); // 4 bytes
    byte[] result = new byte[high.length + 1];  // 5 bytes
    System.arraycopy(high, 0, result, 0, high.length);
    result[high.length] = bit;
    return result;  // 33bits
  }

  @Override
  public MdsonicCompUInt64 multiplyByBit(int bitValue) {
    return new MdsonicCompUInt64Bit(value * MdsonicUInt.toUnLong(bitValue));
  }

}
