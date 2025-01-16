package dk.alexandra.fresco.suite.mdml.datatypes;

public class MdmlCompUInt128Bit extends MdmlCompUInt128 {

  public MdmlCompUInt128Bit(long high, int mid, int low) {
    super(high, mid, low);
  }

  public MdmlCompUInt128Bit(long high, int bit) {
    super(high, bit << 31, 0);
  }

  @Override
  public MdmlCompUInt128 multiply(MdmlCompUInt128 other) {
    int bit = mid >>> 31;
    int otherBit = other.mid >>> 31;
    return new MdmlCompUInt128Bit(
        ((high * other.high) << 1) + (high * otherBit) + (other.high * bit),
        mid & other.mid,
        0);
  }

  @Override
  public MdmlCompUInt128 add(MdmlCompUInt128 other) {
    int carry = ((mid >>> 31) + (other.mid >>> 31)) >> 1;
    return new MdmlCompUInt128Bit(high + other.high + carry, mid ^ other.mid, 0);
  }

  @Override
  public MdmlCompUInt128 subtract(MdmlCompUInt128 other) {
    throw new UnsupportedOperationException("Subtraction not supported by bit representation");
  }

  @Override
  public MdmlCompUInt128 negate() {
    throw new UnsupportedOperationException("Negation not supported by bit representation");
  }

  @Override
  public MdmlCompUInt128 toBitRep() {
    throw new IllegalStateException("Already in bit form");
  }

  @Override
  public MdmlCompUInt128 toArithmeticRep() {
    return new MdmlCompUInt128(high, mid, low);
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
  public byte[] serializeAll() {
    byte[] high = getMostSignificant().toByteArray();
    byte low = (byte) (mid >>> 31);
    byte[] result = new byte[high.length + 1];
    System.arraycopy(high, 0, result, 0, high.length);
    result[high.length] = low;
    return result;
  }

  @Override
  public MdmlCompUInt128 clearHighBits() {
    return new MdmlCompUInt128Bit(0L, mid, 0);
  }

  @Override
  public int bitValue() {
    return (mid & 0x80000000) >>> 31;
  }

  @Override
  public MdmlCompUInt128 multiplyByBit(int value) {
    return multiply(new MdmlCompUInt128Bit(0L, value));
  }

}
