package dk.alexandra.fresco.suite.mdsonic.datatypes;

import dk.alexandra.fresco.framework.util.ByteAndBitConverter;

import java.math.BigInteger;

/**
 * A wrapper for the int type adhering to the {@link MdsonicUInt} interface so that it can be used by
 * {@link MdsonicCompUInt} instances.
 */
public class MdsonicUInt32 implements MdsonicUInt<MdsonicUInt32> {

  private final int value;

  public MdsonicUInt32(int value) {
    this.value = value;
  }

  @Override
  public MdsonicUInt32 add(MdsonicUInt32 other) {
    return new MdsonicUInt32(value + other.value);
  }

  @Override
  public MdsonicUInt32 multiply(MdsonicUInt32 other) {
    return new MdsonicUInt32(value * other.value);
  }

  @Override
  public MdsonicUInt32 subtract(MdsonicUInt32 other) {
    return new MdsonicUInt32(value - other.value);
  }

  @Override
  public MdsonicUInt32 negate() {
    return new MdsonicUInt32(-value);
  }

  @Override
  public boolean isZero() {
    return value == 0;
  }

  @Override
  public boolean isOne() {
    return value == 1;
  }

  @Override
  public int getBitLength() {
    return 32;
  }

  @Override
  public byte[] toByteArray() {
    return ByteAndBitConverter.toByteArray(value);
  }

  @Override
  public BigInteger toBigInteger() {
    return new BigInteger(1, toByteArray());
  }

  @Override
  public long toLong() {
    return Integer.toUnsignedLong(value);
  }

  @Override
  public int toInt() {
    return value;
  }

  @Override
  public String toString() {
    return Integer.toUnsignedString(value);
  }

}
