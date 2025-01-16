package dk.alexandra.fresco.suite.mdml.datatypes;

import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.suite.mdml.util.UIntSerializer;

import java.math.BigInteger;
import java.security.SecureRandom;

public class MdmlCompUInt128Factory implements MdmlCompUIntFactory<MdmlCompUInt128> {

  private static final MdmlCompUInt128 ZERO = new MdmlCompUInt128(new byte[16]);
  private static final MdmlCompUInt128 ONE = new MdmlCompUInt128(1);
  private static final MdmlCompUInt128 TWO = new MdmlCompUInt128(2);
  private final SecureRandom random = new SecureRandom();

  @Override
  public MdmlCompUInt128 createFromBytes(byte[] bytes) {
    return new MdmlCompUInt128(bytes);
  }

  @Override
  public MdmlCompUInt128 createRandom() {
    byte[] bytes = new byte[16];
    this.random.nextBytes(bytes);
    return createFromBytes(bytes);
  }

  @Override
  public MdmlCompUInt128 fromBit(int bit) {
    return new MdmlCompUInt128Bit(0L, bit);
  }

  public MdmlCompUInt128 fromBitAndHigh(long high, int bit){return new MdmlCompUInt128Bit(high, bit);}

  @Override
  public ByteSerializer<MdmlCompUInt128> createSerializer() {
    return new UIntSerializer<>(this);
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
  public MdmlCompUInt128 createFromBigInteger(BigInteger value) {
    return value == null ? null : new MdmlCompUInt128(value);
  }

  @Override
  public MdmlCompUInt128 fromLong(long value) {
    return new MdmlCompUInt128(value);
  }

  @Override
  public MdmlCompUInt128 zero() {
    return ZERO;
  }

  @Override
  public MdmlCompUInt128 one() {
    return ONE;
  }

  @Override
  public MdmlCompUInt128 two() {
    return TWO;
  }

}
