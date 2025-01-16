package dk.alexandra.fresco.suite.mdsonic;

import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.lib.real.RealNumericContext;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicCompUInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicCompUIntConverter;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicGF;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicUInt;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;
import dk.alexandra.fresco.suite.mdsonic.synchronization.MdsonicRoundSynchronization;

/**
 * The Mdsonic protocol suite. <p>This suite works with ring elements. Each ring element, represented
 * by {@link PlainT} is conceptually composed of two smaller ring elements, represented by {@link
 * HighT} and {@link LowT}, i.e., a most significant bit portion and a least significant bit
 * portion. The least-significant bit portion is used to store the actual value (or secret-share
 * thereof) we are computing on. The most-significant bit portion is required for security and is
 * used in the mac-check protocol implemented in {@link dk.alexandra.fresco.suite.mdsonic.protocols.computations.MdsonicArithmeticMacCheckComputation}.</p>
 *
 * @param <HighT> type representing most significant bit portion of open values
 * @param <LowT> type representing least significant bit portion of open values
 * @param <PlainT> the type representing open values
 */
public abstract class MdsonicProtocolSuite<
    HighT extends MdsonicUInt<HighT>,
    LowT extends MdsonicUInt<LowT>,
    PlainT extends MdsonicCompUInt<HighT, LowT, PlainT>, SecretP extends MdsonicGF<SecretP>>
    implements ProtocolSuiteNumeric<MdsonicResourcePool<PlainT, SecretP>> {

  private static final int DEFAULT_FIXED_POINT_PRECISION = 8;  // 16

  private final MdsonicCompUIntConverter<HighT, LowT, PlainT> converter;
  private final boolean useMaskedEvaluation;
  private final int fixedPointPrecision;

  /**
   * Constructs new {@link MdsonicProtocolSuite}.
   *
   * @param converter helper which allows converting {@link HighT}, and {@link LowT} instances to
   * {@link PlainT}. This is necessary for the mac-check protocol where we perform arithmetic
   * between these different types.
   * @param useMaskedEvaluation flag for switching to masked evaluation mode.
   */
  MdsonicProtocolSuite(MdsonicCompUIntConverter<HighT, LowT, PlainT> converter,
                       boolean useMaskedEvaluation,
                       int fixedPointPrecision) {
    this.converter = converter;
    this.useMaskedEvaluation = useMaskedEvaluation;
    this.fixedPointPrecision = fixedPointPrecision;
  }

  MdsonicProtocolSuite(MdsonicCompUIntConverter<HighT, LowT, PlainT> converter, int fixedPointPrecision) {
    this(converter, false, fixedPointPrecision);
  }

  MdsonicProtocolSuite(MdsonicCompUIntConverter<HighT, LowT, PlainT> converter, boolean useMaskedEvaluation) {
    this(converter, useMaskedEvaluation, DEFAULT_FIXED_POINT_PRECISION);
  }

  MdsonicProtocolSuite(MdsonicCompUIntConverter<HighT, LowT, PlainT> converter) {
    this(converter, false, DEFAULT_FIXED_POINT_PRECISION);
  }

  @Override
  public BuilderFactoryNumeric init(MdsonicResourcePool<PlainT, SecretP> resourcePool, Network network) {
    return new MdsonicBuilder<>(resourcePool.getFactory(),
        createBasicNumericContext(resourcePool),
        createRealNumericContext(),
        useMaskedEvaluation);
  }

  @Override
  public RoundSynchronization<MdsonicResourcePool<PlainT, SecretP>> createRoundSynchronization() {
    return new MdsonicRoundSynchronization<>(this);
  }

  public BasicNumericContext createBasicNumericContext(MdsonicResourcePool<PlainT, SecretP> resourcePool) {
    return new BasicNumericContext(
        resourcePool.getMaxBitLength(), resourcePool.getModulus(), resourcePool.getMyId(),
        resourcePool.getNoOfParties());
  }

  public RealNumericContext createRealNumericContext() {
    return new RealNumericContext(fixedPointPrecision);
  }

}
