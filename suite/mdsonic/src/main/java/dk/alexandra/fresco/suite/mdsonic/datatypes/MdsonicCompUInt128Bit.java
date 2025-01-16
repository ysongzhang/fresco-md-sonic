package dk.alexandra.fresco.suite.mdsonic.datatypes;

public class MdsonicCompUInt128Bit extends MdsonicCompUInt128 {

  public MdsonicCompUInt128Bit(long high, int mid, int low) {
    super(high, mid, low);
  }

  public MdsonicCompUInt128Bit(long high, int bit) {
    super(high, bit << 31, 0);
  }

  @Override
  public MdsonicCompUInt128 multiply(MdsonicCompUInt128 other) {
    int bit = mid >>> 31;
    int otherBit = other.mid >>> 31;
    return new MdsonicCompUInt128Bit(
        ((high * other.high) << 1) + (high * otherBit) + (other.high * bit),
        mid & other.mid,
        0);
  }

  @Override
  public MdsonicCompUInt128 add(MdsonicCompUInt128 other) {
    int carry = ((mid >>> 31) + (other.mid >>> 31)) >> 1;
    return new MdsonicCompUInt128Bit(high + other.high + carry, mid ^ other.mid, 0);
  }

  @Override
  public MdsonicCompUInt128 subtract(MdsonicCompUInt128 other) {
    throw new UnsupportedOperationException("Subtraction not supported by bit representation");
  }

  @Override
  public MdsonicCompUInt128 negate() {
    throw new UnsupportedOperationException("Negation not supported by bit representation");
  }

  @Override
  public MdsonicCompUInt128 toBitRep() {
    throw new IllegalStateException("Already in bit form");
  }

  @Override
  public MdsonicCompUInt128 toArithmeticRep() {
    return new MdsonicCompUInt128(high, mid, low);
  }

  @Override
  public String toString() {
    return toBigInteger().toString() + "B";
  }

  @Override
  public byte[] serializeLeastSignificant() {
    return new byte[]{(byte) (mid >>> 31)};
  }

  @Override
  public byte[] serializeAll() {  // Real information bit occupies a single byte
    byte[] high = getMostSignificant().toByteArray();  // 8 bytes
    byte low = (byte) (mid >>> 31); // 1 bit, Use one byte storage, where all the first bits are 0
    byte[] result = new byte[high.length + 1];  // 9 bytes
    System.arraycopy(high, 0, result, 0, high.length);
    result[high.length] = low;
    return result;  // 65bits
  }

  @Override
  public MdsonicCompUInt128 clearHighBits() {
    return new MdsonicCompUInt128Bit(0L, mid, 0);
  }

  @Override
  public int bitValue() {
    return (mid & 0x80000000) >>> 31;
  }

  @Override
  public MdsonicCompUInt128 multiplyByBit(int value) {
    return multiply(new MdsonicCompUInt128Bit(0L, value));
  }

}
