package dk.alexandra.fresco.suite.mdsonic.datatypes;

import dk.alexandra.fresco.framework.util.ByteAndBitConverter;

import java.math.BigInteger;

/**
 * A wrapper for the long type adhering to the {@link MdsonicUInt} interface so that it can be used by
 * {@link MdsonicCompUInt} instances.
 */
public class MdsonicUInt64 implements MdsonicUInt<MdsonicUInt64> {

  private final long value;

  public MdsonicUInt64(long value) {
    this.value = value;
  }

  @Override
  public MdsonicUInt64 add(MdsonicUInt64 other) {
    return new MdsonicUInt64(value + other.value);
  }

  @Override
  public MdsonicUInt64 multiply(MdsonicUInt64 other) {
    return new MdsonicUInt64(value * other.value);
  }

  @Override
  public MdsonicUInt64 subtract(MdsonicUInt64 other) {
    return new MdsonicUInt64(value - other.value);
  }

  @Override
  public MdsonicUInt64 negate() {
    return new MdsonicUInt64(-value);
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
    return 64;
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
    return value;
  }

  @Override
  public int toInt() {
    return (int) value;
  }

  @Override
  public String toString() {
    return Long.toUnsignedString(value);
  }
}
