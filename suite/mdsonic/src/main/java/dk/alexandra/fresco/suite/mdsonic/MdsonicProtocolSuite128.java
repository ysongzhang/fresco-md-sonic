package dk.alexandra.fresco.suite.mdsonic;

import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicCompUInt128;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicCompUIntConverter128;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicGF64;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicUInt64;

/**
 * Protocol suite using {@link MdsonicCompUInt128} as the underlying plain-value type.
 */
public class MdsonicProtocolSuite128 extends MdsonicProtocolSuite<MdsonicUInt64, MdsonicUInt64, MdsonicCompUInt128, MdsonicGF64> {

  public MdsonicProtocolSuite128(boolean useMaskedEvaluation) {
    super(new MdsonicCompUIntConverter128(), useMaskedEvaluation);
  }

  public MdsonicProtocolSuite128(boolean useMaskedEvaluation, int fixedPointPrecision) {
    super(new MdsonicCompUIntConverter128(), useMaskedEvaluation, fixedPointPrecision);
  }

  public MdsonicProtocolSuite128() {
    this(false);
  }

}
