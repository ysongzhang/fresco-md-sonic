package dk.alexandra.fresco.suite.mdsonic.datatypes;

import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.suite.mdsonic.util.UIntSerializer;

import java.math.BigInteger;
import java.security.SecureRandom;

public class MdsonicCompUInt64Factory implements MdsonicCompUIntFactory<MdsonicCompUInt64> {

  private static final MdsonicCompUInt64 ZERO = new MdsonicCompUInt64(0);
  private static final MdsonicCompUInt64 ONE = new MdsonicCompUInt64(1);
  private static final MdsonicCompUInt64 TWO = new MdsonicCompUInt64(2);
  private final SecureRandom random = new SecureRandom();

  @Override
  public MdsonicCompUInt64 createFromBytes(byte[] bytes) {
    return new MdsonicCompUInt64(bytes);
  }

  @Override
  public MdsonicCompUInt64 createRandom() {
    byte[] bytes = new byte[8];
    this.random.nextBytes(bytes);
    return createFromBytes(bytes);
  }

  @Override
  public MdsonicCompUInt64 fromBit(int bit) {
    return new MdsonicCompUInt64Bit(0, (bit) & 1);
  }

  public MdsonicCompUInt64 fromBitAndHigh(long high, int bit){return new MdsonicCompUInt64Bit((int) high, bit);}

  @Override
  public ByteSerializer<MdsonicCompUInt64> createSerializer() {
    return new UIntSerializer<>(this);
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
  public MdsonicCompUInt64 createFromBigInteger(BigInteger value) {
    return value == null ? null : new MdsonicCompUInt64(value);
  }

  @Override
  public MdsonicCompUInt64 fromLong(long value) {
    return new MdsonicCompUInt64(value & 0xffffffffL);
  }

  @Override
  public MdsonicCompUInt64 zero() {
    return ZERO;
  }

  @Override
  public MdsonicCompUInt64 one() {
    return ONE;
  }

  @Override
  public MdsonicCompUInt64 two() {
    return TWO;
  }

}
