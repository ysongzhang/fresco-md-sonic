package dk.alexandra.fresco.suite.mdsonic;

import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicCompUInt64;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicCompUIntConverter64;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicGF32;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicUInt32;

/**
 * Protocol suite using {@link MdsonicCompUInt64} as the underlying plain-value type.
 */
public class MdsonicProtocolSuite64 extends MdsonicProtocolSuite<MdsonicUInt32, MdsonicUInt32, MdsonicCompUInt64, MdsonicGF32> {

  public MdsonicProtocolSuite64(boolean useMaskedEvaluation) {
    super(new MdsonicCompUIntConverter64(), useMaskedEvaluation);
  }

  public MdsonicProtocolSuite64(boolean useMaskedEvaluation, int fixedPointPrecision) {
    super(new MdsonicCompUIntConverter64(), useMaskedEvaluation, fixedPointPrecision);
  }

  public MdsonicProtocolSuite64() {
    this(false);
  }

}
