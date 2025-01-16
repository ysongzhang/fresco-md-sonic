package dk.alexandra.fresco.suite.mdsonic.datatypes;

public class MdsonicCompUIntConverter64 implements MdsonicCompUIntConverter<MdsonicUInt32, MdsonicUInt32, MdsonicCompUInt64> {

  @Override
  public MdsonicCompUInt64 createFromHigh(MdsonicUInt32 value) {
    return new MdsonicCompUInt64(
        MdsonicUInt.toUnLong(value.toInt())
    );
  }

  @Override
  public MdsonicCompUInt64 createFromLow(MdsonicUInt32 value) {
    return new MdsonicCompUInt64(MdsonicUInt.toUnLong(value.toInt()));
  }

}
