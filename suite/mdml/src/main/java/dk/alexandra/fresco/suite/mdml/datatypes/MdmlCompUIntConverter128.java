package dk.alexandra.fresco.suite.mdml.datatypes;

public class MdmlCompUIntConverter128 implements MdmlCompUIntConverter<MdmlUInt64, MdmlUInt64, MdmlCompUInt128> {

  @Override
  public MdmlCompUInt128 createFromHigh(MdmlUInt64 value) {
    return new MdmlCompUInt128(value);
  }

  @Override
  public MdmlCompUInt128 createFromLow(MdmlUInt64 value) {
    return new MdmlCompUInt128(value);
  }

}
