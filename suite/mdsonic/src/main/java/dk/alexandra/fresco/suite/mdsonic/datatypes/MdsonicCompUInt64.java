package dk.alexandra.fresco.suite.mdsonic.datatypes;

import dk.alexandra.fresco.framework.util.ByteAndBitConverter;
import dk.alexandra.fresco.framework.value.OInt;

import java.math.BigInteger;

public class MdsonicCompUInt64 implements MdsonicCompUInt<MdsonicUInt32, MdsonicUInt32, MdsonicCompUInt64> {

  private static final MdsonicCompUInt64 ZERO = new MdsonicCompUInt64(0);
  private static final MdsonicCompUInt64 ONE = new MdsonicCompUInt64(1);

  protected final long value;

  public MdsonicCompUInt64(byte[] bytes) {
    this(bytes, false);
  }

  public MdsonicCompUInt64(byte[] bytes, boolean requiresPadding) {
    byte[] padded = requiresPadding ? MdsonicCompUInt.pad(bytes, 64) : bytes;
    if (padded.length == 4) {
      // we are instantiating from the least significant bits only
      this.value = MdsonicUInt.toUnLong(ByteAndBitConverter.toInt(padded, 0));
    } else {
      this.value = ByteAndBitConverter.toLong(padded, 0);
    }
  }

  public MdsonicCompUInt64(long value) {
    this.value = value;
  }

  public MdsonicCompUInt64(BigInteger value) {
    this.value = value.longValue();
  }

  @Override
  public MdsonicUInt32 getMostSignificant() {
    return new MdsonicUInt32((int) (value >>> 32));
  }

  @Override
  public MdsonicUInt32 getLeastSignificant() {
    return new MdsonicUInt32((int) (value & 0xfffffffffL));
  } // the (int) forced type conversion, the excess part will be directly truncated

  @Override
  public MdsonicUInt32 getLeastSignificantAsHigh() {
    return new MdsonicUInt32((int) (value & 0xfffffffffL));
  }

  @Override
  public MdsonicCompUInt64 shiftLeftSmall(int n) {
    return new MdsonicCompUInt64(value << n);
  }

  @Override
  public MdsonicCompUInt64 shiftRightSmall(int n) {
    return new MdsonicCompUInt64(value >>> n);
  }

  @Override
  public MdsonicCompUInt64 shiftRightLowOnly(int n) {
    if (n >= Integer.SIZE) {
      throw new IllegalArgumentException(
              "Shift step must be less than " + Integer.SIZE + " but was " + n);
    }
    if (n <= 0) {
      return this;
    }
    int low = toInt();
    return new MdsonicCompUInt64((((value >>> 32) << 32) | MdsonicUInt.toUnLong(low >>> n)));
  }

  public MdsonicCompUInt64 shiftRightLowOnlySigned(int n) {
    if (n >= Integer.SIZE) {
      throw new IllegalArgumentException(
              "Shift step must be less than " + Integer.SIZE + " but was " + n);
    }
    if (n <= 0) {
      return this;
    }
    int low = toInt();
    return new MdsonicCompUInt64((((value >>> 32) << 32) | MdsonicUInt.toUnLong(low >> n)));
  }

  @Override
  public MdsonicCompUInt64 shiftLowIntoHigh() {
    return new MdsonicCompUInt64(value << 32);
  }

  @Override
  public MdsonicCompUInt64 clearAboveBitAt(int bitPos) {
    long mask = ~(0x8000000000000000L >> (63 - bitPos));
    return new MdsonicCompUInt64(value & mask);
  }

  @Override
  public MdsonicCompUInt64 clearHighBits() {
    return new MdsonicCompUInt64((value & 0xffffffffL));
  }

  @Override
  public MdsonicCompUInt64 toBitRep() {
    return new MdsonicCompUInt64Bit(value);
  }

  @Override
  public MdsonicCompUInt64 toArithmeticRep() {
    throw new IllegalStateException("Already arithmetic");
  }

  @Override
  public MdsonicCompUInt64 multiplyByBit(int bit) {
    return new MdsonicCompUInt64(this.value * bit);
  }

  @Override
  public boolean testBit(int bitPos) {
    return (((1L << bitPos) & value) >>> bitPos) == 1;
  }

  @Override
  public MdsonicCompUInt64 testBitAsUInt(int bitPos) {
    return testBit(bitPos) ? ONE : ZERO;
  }

  @Override
  public int getLowBitLength() {
    return 32;
  }

  @Override
  public int getHighBitLength() {
    return 32;
  }

  @Override
  public byte[] serializeLeastSignificant() {
    return ByteAndBitConverter.toByteArray((int) (value));
  }

  @Override
  public byte[] serializeAll() {
    return ByteAndBitConverter.toByteArray(value);
  }

  @Override
  public int bitValue() {
    return (int) value & 1; // lowest bit
  }

  @Override
  public OInt out() {
    return this;
  }

  @Override
  public MdsonicCompUInt64 add(MdsonicCompUInt64 other) {
    return new MdsonicCompUInt64(value + other.value);
  }

  @Override
  public MdsonicCompUInt64 multiply(MdsonicCompUInt64 other) {
    return new MdsonicCompUInt64(value * other.value);
  }

  @Override
  public MdsonicCompUInt64 subtract(MdsonicCompUInt64 other) {
    return new MdsonicCompUInt64(value - other.value);
  }

  @Override
  public MdsonicCompUInt64 negate() {
    return new MdsonicCompUInt64(-value);
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
  public byte[] toByteArray() {
    return ByteAndBitConverter.toByteArray(value);
  }

  @Override
  public BigInteger toBigInteger() {
    return new BigInteger(1, toByteArray());
  }

  public BigInteger toSignedBigInteger() { // Only used in the output phase
    BigInteger uIntInfo = new BigInteger(1, ByteAndBitConverter.toByteArray(toInt()));
    // Whether the MSB == 1
    if (uIntInfo.compareTo(BigInteger.ONE.shiftLeft(31)) > 0) { // negative number
      return uIntInfo.subtract(BigInteger.ONE.shiftLeft(32));
    }
    return uIntInfo;
  }

  @Override
  public long toLong() {
    return value;
  }

  @Override
  public int toInt() {
    return (int) (value & 0xffffffffL);
  }

  @Override
  public String toString() {
    return toBigInteger().toString();
  }

}
