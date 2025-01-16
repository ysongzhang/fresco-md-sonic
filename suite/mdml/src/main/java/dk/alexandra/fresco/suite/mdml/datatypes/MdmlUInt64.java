package dk.alexandra.fresco.suite.mdml.datatypes;

import dk.alexandra.fresco.framework.util.ByteAndBitConverter;

import java.math.BigInteger;

/**
 * A wrapper for the long type adhering to the {@link MdmlUInt} interface so that it can be used by
 * {@link MdmlCompUInt} instances.
 */
public class MdmlUInt64 implements MdmlUInt<MdmlUInt64> {

  private final long value;

  public MdmlUInt64(long value) {
    this.value = value;
  }

  @Override
  public MdmlUInt64 add(MdmlUInt64 other) {
    return new MdmlUInt64(value + other.value);
  }

  @Override
  public MdmlUInt64 multiply(MdmlUInt64 other) {
    return new MdmlUInt64(value * other.value);
  }

  @Override
  public MdmlUInt64 subtract(MdmlUInt64 other) {
    return new MdmlUInt64(value - other.value);
  }

  @Override
  public MdmlUInt64 negate() {
    return new MdmlUInt64(-value);
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
