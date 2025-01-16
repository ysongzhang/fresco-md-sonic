package dk.alexandra.fresco.suite.mdml;

import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.lib.real.RealNumericContext;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUIntConverter;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlUInt;
import dk.alexandra.fresco.suite.mdml.protocols.computations.MdmlMacCheckComputation;
import dk.alexandra.fresco.suite.mdml.resource.MdmlResourcePool;
import dk.alexandra.fresco.suite.mdml.synchronization.MdmlRoundSynchronization;

/**
 * The Mdml protocol suite. <p>This suite works with ring elements. Each ring element, represented
 * by {@link PlainT} is conceptually composed of two smaller ring elements, represented by {@link
 * HighT} and {@link LowT}, i.e., a most significant bit portion and a least significant bit
 * portion. The least-significant bit portion is used to store the actual value (or secret-share
 * thereof) we are computing on. The most-significant bit portion is required for security and is
 * used in the mac-check protocol implemented in {@link MdmlMacCheckComputation}.</p>
 *
 * @param <HighT> type representing most significant bit portion of open values
 * @param <LowT> type representing least significant bit portion of open values
 * @param <PlainT> the type representing open values
 */
public abstract class MdmlProtocolSuite<
    HighT extends MdmlUInt<HighT>,
    LowT extends MdmlUInt<LowT>,
    PlainT extends MdmlCompUInt<HighT, LowT, PlainT>>
    implements ProtocolSuiteNumeric<MdmlResourcePool<PlainT>> {

  private static final int DEFAULT_FIXED_POINT_PRECISION = 16;  // 16

  private final MdmlCompUIntConverter<HighT, LowT, PlainT> converter;
  private final int fixedPointPrecision;

  /**
   * Constructs new {@link MdmlProtocolSuite}.
   *
   * @param converter helper which allows converting {@link HighT}, and {@link LowT} instances to
   * {@link PlainT}. This is necessary for the mac-check protocol where we perform arithmetic
   * between these different types.
   */
  MdmlProtocolSuite(MdmlCompUIntConverter<HighT, LowT, PlainT> converter,
                    int fixedPointPrecision) {
    this.converter = converter;
    this.fixedPointPrecision = fixedPointPrecision;
  }

  MdmlProtocolSuite(MdmlCompUIntConverter<HighT, LowT, PlainT> converter) {
    this(converter, DEFAULT_FIXED_POINT_PRECISION);
  }

  @Override
  public BuilderFactoryNumeric init(MdmlResourcePool<PlainT> resourcePool, Network network) {
    return new MdmlBuilder<>(resourcePool.getFactory(),
        createBasicNumericContext(resourcePool),
        createRealNumericContext());
  }

  @Override
  public RoundSynchronization<MdmlResourcePool<PlainT>> createRoundSynchronization() {
    return new MdmlRoundSynchronization<>(this);
  }

  public BasicNumericContext createBasicNumericContext(MdmlResourcePool<PlainT> resourcePool) {
    return new BasicNumericContext(
        resourcePool.getMaxBitLength(), resourcePool.getModulus(), resourcePool.getMyId(),
        resourcePool.getNoOfParties());
  }

  public RealNumericContext createRealNumericContext() {
    return new RealNumericContext(fixedPointPrecision);
  }

}
