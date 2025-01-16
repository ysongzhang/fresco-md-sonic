package dk.alexandra.fresco.suite.mdml;

import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUInt128;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUIntConverter128;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlUInt64;

/**
 * Protocol suite using {@link MdmlCompUInt128} as the underlying plain-value type.
 */
public class MdmlProtocolSuite128 extends MdmlProtocolSuite<MdmlUInt64, MdmlUInt64, MdmlCompUInt128> {

  public MdmlProtocolSuite128() {
    super(new MdmlCompUIntConverter128());
  }

  public MdmlProtocolSuite128(int fixedPointPrecision) {
    super(new MdmlCompUIntConverter128(), fixedPointPrecision);
  }

}
