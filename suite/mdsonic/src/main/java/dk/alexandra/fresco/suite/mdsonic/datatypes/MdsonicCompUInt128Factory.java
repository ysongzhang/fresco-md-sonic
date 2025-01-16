package dk.alexandra.fresco.suite.mdsonic.datatypes;

import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.suite.mdsonic.util.UIntSerializer;

import java.math.BigInteger;
import java.security.SecureRandom;

public class MdsonicCompUInt128Factory implements MdsonicCompUIntFactory<MdsonicCompUInt128> {

  private static final MdsonicCompUInt128 ZERO = new MdsonicCompUInt128(new byte[16]);
  private static final MdsonicCompUInt128 ONE = new MdsonicCompUInt128(1);
  private static final MdsonicCompUInt128 TWO = new MdsonicCompUInt128(2);
  private final SecureRandom random = new SecureRandom();

  @Override
  public MdsonicCompUInt128 createFromBytes(byte[] bytes) {
    return new MdsonicCompUInt128(bytes);
  }

  @Override
  public MdsonicCompUInt128 createRandom() {
    byte[] bytes = new byte[16];
    this.random.nextBytes(bytes);
    return createFromBytes(bytes);
  }

  @Override
  public MdsonicCompUInt128 fromBit(int bit) {
    return new MdsonicCompUInt128Bit(0L, bit);
  }

  public MdsonicCompUInt128 fromBitAndHigh(long high, int bit){return new MdsonicCompUInt128Bit(high, bit);}

  @Override
  public ByteSerializer<MdsonicCompUInt128> createSerializer() {
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
  public MdsonicCompUInt128 createFromBigInteger(BigInteger value) {
    return value == null ? null : new MdsonicCompUInt128(value);
  }

  @Override
  public MdsonicCompUInt128 fromLong(long value) {
    return new MdsonicCompUInt128(value);
  }

  @Override
  public MdsonicCompUInt128 zero() {
    return ZERO;
  }

  @Override
  public MdsonicCompUInt128 one() {
    return ONE;
  }

  @Override
  public MdsonicCompUInt128 two() {
    return TWO;
  }

}
