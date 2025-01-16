package dk.alexandra.fresco.suite.mdml.datatypes;

import dk.alexandra.fresco.framework.util.ByteAndBitConverter;
import dk.alexandra.fresco.framework.value.OInt;

import java.math.BigInteger;

/**
 * Unsigned 128-bit integer with support for in-place operations. <p>Loosely follows this article
 * https://locklessinc.com/articles/256bit_arithmetic/. Note that this class is NOT SAFE to
 * instantiate with negative values.</p>
 */
public class MdmlCompUInt128 implements MdmlCompUInt<MdmlUInt64, MdmlUInt64, MdmlCompUInt128> {

  private static final MdmlCompUInt128 ONE = new MdmlCompUInt128(1);
  private static final MdmlCompUInt128 ZERO = new MdmlCompUInt128(0);
  protected final long high;
  protected final int mid;
  final int low;

  /**
   * Creates new {@link MdmlCompUInt128}. <p>Do <b>not</b> pad bytes by default.</p>
   *
   * @param bytes bytes interpreted in big-endian order.
   */
  public MdmlCompUInt128(byte[] bytes) {
    this(bytes, false);
  }

  /**
   * Creates new {@link MdmlCompUInt128}.
   *
   * @param bytes bytes interpreted in big-endian order.
   * @param requiresPadding indicates if the bytes need to be padded up to 16 bytes.
   */
  public MdmlCompUInt128(byte[] bytes, boolean requiresPadding) {
    byte[] padded = requiresPadding ? MdmlCompUInt.pad(bytes, 128) : bytes;
    if (padded.length == 8) {
      // we are instantiating from the least significant bits only
      this.high = 0L;
      this.mid = ByteAndBitConverter.toInt(padded, 4);
      this.low = ByteAndBitConverter.toInt(padded, 0);
    } else {
      this.high = ByteAndBitConverter.toLong(padded, 8);
      this.mid = ByteAndBitConverter.toInt(padded, 4);
      this.low = ByteAndBitConverter.toInt(padded, 0);
    }
  }

  /**
   * Creates new {@link MdmlCompUInt128} from {@link BigInteger}.
   */
  public MdmlCompUInt128(BigInteger value) {
    this(value.shiftRight(64).longValue(), value.shiftRight(32).intValue(), value.intValue());
  }

  public MdmlCompUInt128(BigInteger value, boolean lowOnly) {
    this(value.longValue());
  }

  MdmlCompUInt128(long high, int mid, int low) {
    this.high = high;
    this.mid = mid;
    this.low = low;
  }

  MdmlCompUInt128(MdmlUInt64 value) {
    this(value.toLong());
  }

  MdmlCompUInt128(long value) {
    this.high = 0;
    this.mid = (int) (value >>> 32);
    this.low = (int) value;
  }

  MdmlCompUInt128(MdmlCompUInt128 other) {
    this.high = other.high;
    this.mid = other.mid;
    this.low = other.low;
  }

  @Override
  public MdmlCompUInt128 add(MdmlCompUInt128 other) {
    long newLow = Integer.toUnsignedLong(this.low) + Integer.toUnsignedLong(other.low);
    long lowOverflow = newLow >>> 32;
    long newMid = Integer.toUnsignedLong(this.mid)
        + Integer.toUnsignedLong(other.mid)
        + lowOverflow;
    long midOverflow = newMid >>> 32;
    long newHigh = this.high + other.high + midOverflow;
    return new MdmlCompUInt128(newHigh, (int) newMid, (int) newLow);
  }

  @Override
  public MdmlCompUInt128 multiply(MdmlCompUInt128 other) {
    long thisLowAsLong = MdmlUInt.toUnLong(this.low);
    long thisMidAsLong = MdmlUInt.toUnLong(this.mid);
    long otherLowAsLong = MdmlUInt.toUnLong(other.low);
    long otherMidAsLong = MdmlUInt.toUnLong(other.mid);

    // low
    long t1 = thisLowAsLong * otherLowAsLong;
    long t2 = thisLowAsLong * otherMidAsLong;
    long t3 = thisLowAsLong * other.high;

    // mid
    long t4 = thisMidAsLong * otherLowAsLong;
    long t5 = thisMidAsLong * otherMidAsLong;
    int t6 = (int) (this.mid * other.high);

    // high
    long t7 = this.high * otherLowAsLong;
    int t8 = (int) (this.high * other.mid);
    // we don't need the product of this.high and other.high since those overflow 2^128

    long m1 = (t1 >>> 32) + (t2 & 0xffffffffL);
    int m2 = (int) m1;
    long newMid = MdmlUInt.toUnLong(m2) + (t4 & 0xffffffffL);

    long newHigh = (t2 >>> 32)
        + t3
        + (t4 >>> 32)
        + t5
        + (MdmlUInt.toUnLong(t6) << 32)
        + t7
        + (MdmlUInt.toUnLong(t8) << 32)
        + (m1 >>> 32)
        + (newMid >>> 32);
    return new MdmlCompUInt128(newHigh, (int) newMid, (int) t1);
  }

  @Override
  public MdmlCompUInt128 subtract(MdmlCompUInt128 other) {
//    long newLow = Integer.toUnsignedLong(this.low) - Integer.toUnsignedLong(other.low);
//    long lowOverflow = newLow >>> 32;
//    long newMid = Integer.toUnsignedLong(this.mid)
//        - Integer.toUnsignedLong(other.mid)
//        - lowOverflow;
//    long midOverflow = newMid >>> 32;
//    long newHigh = this.high - other.high - midOverflow;
//    return new CompUInt128(newHigh, (int) newMid, (int) newLow);
    return this.add(other.negate());
  }

  @Override
  public MdmlCompUInt128 negate() {
    return new MdmlCompUInt128(~high, ~mid, ~low).add(ONE);
  }

  @Override
  public boolean isZero() {
    return low == 0 && mid == 0 && high == 0;
  }

  @Override
  public boolean isOne() {
    return low == 1 && mid == 0 && high == 0;
  }

  @Override
  public BigInteger toBigInteger() {
    return new BigInteger(1, toByteArray());
  }

  public BigInteger toSignedBigInteger() {
    BigInteger uIntInfo = new BigInteger(1, toByteArray64());
    if (uIntInfo.compareTo(BigInteger.ONE.shiftLeft(63)) > 0) {
      uIntInfo = uIntInfo.subtract(BigInteger.ONE.shiftLeft(64));
    }
    return uIntInfo;
  }

  @Override
  public MdmlUInt64 getLeastSignificant() {
    return new MdmlUInt64(toLong());
  }

  @Override
  public MdmlUInt64 getMostSignificant() {
    return new MdmlUInt64(high);
  }

  @Override
  public MdmlUInt64 getLeastSignificantAsHigh() {
    return getLeastSignificant();
  }

  @Override
  public MdmlCompUInt128 shiftLeftSmall(int n) {
    if (n >= Long.SIZE) {
      throw new IllegalArgumentException(
          "Shift step must be less than " + Long.SIZE + " but was " + n);
    }
    if (n <= 0) {
      return this;
    }
    int nInv = (Long.SIZE - n);
    long midAndLow = (MdmlUInt.toUnLong(mid) << 32) | MdmlUInt.toUnLong(low);
    long newHigh = (high << n) | (midAndLow >>> nInv);
    long midAndLowShifted = midAndLow << n;
    return new MdmlCompUInt128(newHigh, (int) (midAndLowShifted >>> 32), (int) midAndLowShifted);
  }

  @Override
  public MdmlCompUInt128 shiftRightSmall(int n) {
    if (n >= Long.SIZE) {
      throw new IllegalArgumentException(
          "Shift step must be less than " + Long.SIZE + " but was " + n);
    }
    if (n <= 0) {
      return this;
    }
    int nInv = (Long.SIZE - n);
    long midAndLow = (MdmlUInt.toUnLong(mid) << 32) | MdmlUInt.toUnLong(low);
    long newHigh = high >>> n;
    long midAndLowShifted = (high << nInv) | (midAndLow >>> n);
    return new MdmlCompUInt128(newHigh, (int) (midAndLowShifted >>> 32), (int) midAndLowShifted);
  }

  @Override
  public MdmlCompUInt128 shiftRightLowOnly(int n) {
    if (n >= Long.SIZE) {
      throw new IllegalArgumentException(
          "Shift step must be less than " + Long.SIZE + " but was " + n);
    }
    if (n <= 0) {
      return this;
    }
    long midAndLow = ((MdmlUInt.toUnLong(mid) << 32) | MdmlUInt.toUnLong(low)) >>> n;
    return new MdmlCompUInt128(high, (int) (midAndLow >>> 32), (int) midAndLow);
  }

  public MdmlCompUInt128 shiftRightLowOnlySigned(int n) {
    if (n >= Long.SIZE) {
      throw new IllegalArgumentException(
              "Shift step must be less than " + Long.SIZE + " but was " + n);
    }
    if (n <= 0) {
      return this;
    }
    long midAndLow = ((MdmlUInt.toUnLong(mid) << 32) | MdmlUInt.toUnLong(low)) >> n;
    return new MdmlCompUInt128(high, (int) (midAndLow >>> 32), (int) midAndLow);
  }

  @Override
  public long toLong() {
    return (MdmlUInt.toUnLong(this.mid) << 32) + MdmlUInt.toUnLong(this.low);
  }

  @Override
  public int toInt() {
    return low;
  }

  @Override
  public MdmlCompUInt128 shiftLowIntoHigh() {
    return new MdmlCompUInt128(toLong(), 0, 0);
  }

  @Override
  public MdmlCompUInt128 clearAboveBitAt(int bitPos) {
    if (bitPos < Integer.SIZE) {
      int mask = ~(0x80000000 >> (31 - bitPos));
      return new MdmlCompUInt128(0L, 0, low & mask);
    } else if (bitPos < Long.SIZE) {
      int mask = ~(0x80000000 >> (63 - bitPos));
      return new MdmlCompUInt128(0L, mid & mask, low);
    } else {
      long mask = ~(0x8000000000000000L >> (127 - bitPos));
      return new MdmlCompUInt128(high & mask, mid, low);
    }
  }

  @Override
  public int bitValue() {
    return low & 1; // lowest bit
  }

  @Override
  public MdmlCompUInt128 multiplyByBit(int value) {
    return multiply(new MdmlCompUInt128(0L, 0, value));
  }

  @Override
  public MdmlCompUInt128 clearHighBits() {
    return new MdmlCompUInt128(0, mid, low);
  }

  @Override
  public MdmlCompUInt128 toBitRep() {
    return new MdmlCompUInt128Bit(
        (high << 63) + (MdmlUInt.toUnLong(mid) << 31) + (MdmlUInt.toUnLong(low) >>> 1),
        low << 31,
        0);
  }

  @Override
  public MdmlCompUInt128 toArithmeticRep() {
    throw new IllegalStateException("Already arithmetic");
  }

  @Override
  public int getLowBitLength() {
    return 64;
  }

  @Override
  public int getHighBitLength() {
    return 64;
  }

  @Override
  public String toString() {
    return toBigInteger().toString();
  }

  @Override
  public byte[] toByteArray() {
    byte[] bytes = new byte[16];
    toByteArray(bytes, 0, low);
    toByteArray(bytes, 4, mid);
    toByteArrayLong(bytes, 8, high);
    return bytes;
  }

  public byte[] toByteArray64() {
    byte[] bytes = new byte[8];
    toByteArray(bytes, 0, low);
    toByteArray(bytes, 4, mid);
    return bytes;
  }

  @Override
  public boolean testBit(int bit) {
    // TODO optimize if bottle-neck
    long section;
    int relative;
    if (bit < Integer.SIZE) {
      section = low;
      relative = bit;
    } else if (bit < Long.SIZE) {
      section = mid;
      relative = bit - Integer.SIZE;
    } else {
      section = high;
      relative = bit - Long.SIZE;
    }
    return (((1L << relative) & section) >>> relative) == 1;
  }

  @Override
  public MdmlCompUInt128 testBitAsUInt(int bit) {
    return testBit(bit) ? ONE : ZERO;
  }

  @Override
  public OInt out() {
    return this;
  }

  private void toByteArrayLong(byte[] bytes, int start, long value) {
    int offset = bytes.length - start - 1;
    for (int i = 0; i < 8; i++) {
      bytes[offset - i] = (byte) (value & 0xFF);
      value >>>= 8;
    }
  }

  private void toByteArray(byte[] bytes, int start, int value) {
    int offset = bytes.length - start - 1;
    for (int i = 0; i < 4; i++) {
      bytes[offset - i] = (byte) (value & 0xFF);
      value >>>= 8;
    }
  }

}
