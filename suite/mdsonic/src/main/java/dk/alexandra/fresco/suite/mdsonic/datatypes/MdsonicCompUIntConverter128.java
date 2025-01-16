package dk.alexandra.fresco.suite.mdsonic.datatypes;

public class MdsonicCompUIntConverter128 implements MdsonicCompUIntConverter<MdsonicUInt64, MdsonicUInt64, MdsonicCompUInt128> {

  @Override
  public MdsonicCompUInt128 createFromHigh(MdsonicUInt64 value) {
    return new MdsonicCompUInt128(value);
  }

  @Override
  public MdsonicCompUInt128 createFromLow(MdsonicUInt64 value) {
    return new MdsonicCompUInt128(value);
  }

}
