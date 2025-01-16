package dk.alexandra.fresco.suite.mdml;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.DefaultComparison;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.SIntPair;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUIntFactory;
import dk.alexandra.fresco.suite.mdml.protocols.computations.lt.MostSignBitMdml;
import dk.alexandra.fresco.suite.mdml.protocols.natives.MdmlCarryProtocol;

import java.math.BigInteger;
import java.util.List;

/**
 * Mdml optimized protocols for comparison.
 */
public class MdmlComparison<PlainT extends MdmlCompUInt<?, ?, PlainT>> extends DefaultComparison {

  private final MdmlCompUIntFactory<PlainT> factory;

  protected MdmlComparison(
      BuilderFactoryNumeric factoryNumeric,
      ProtocolBuilderNumeric builder, MdmlCompUIntFactory<PlainT> factory) {
    super(factoryNumeric, builder);
    this.factory = factory;
  }

  @Override
  public DRes<SInt> compareLT(DRes<SInt> x1, DRes<SInt> x2, Algorithm algorithm) {
    if (algorithm == Algorithm.CONST_ROUNDS) {
      throw new UnsupportedOperationException(
          "No constant round comparison protocol implemented for Mdml");
    } else {
      DRes<SInt> diff = builder.numeric().sub(x1, x2);
      return builder.seq(new MostSignBitMdml<>(diff, factory));
    }
  }

  public DRes<SInt> compareLEQ(DRes<SInt> x, DRes<SInt> y) {
    DRes<SInt> compare = compareLT(y, x, Algorithm.LOG_ROUNDS);
    BigInteger oint = BigInteger.valueOf(1);
    Numeric numericBuilder = builder.numeric();
    return numericBuilder.sub(oint, compare);
  }

  public DRes<SInt> compareLEQLong(DRes<SInt> x, DRes<SInt> y) {
    throw new IllegalArgumentException("Don't support LEQLong now!");
  }

  @Override
  public DRes<SInt> compareLTBits(DRes<OInt> openValue, DRes<List<DRes<SInt>>> secretBits) {
    DRes<List<DRes<SInt>>> converted = builder.conversion().toBooleanBatch(secretBits);
    return super.compareLTBits(openValue, converted);
  }

  @Override
  public DRes<SInt> compareZero(DRes<SInt> x, int bitLength, Algorithm algorithm) {
    throw new IllegalArgumentException("Don't support zero test now!");
  }

  @Override
  public DRes<SInt> equals(DRes<SInt> x, DRes<SInt> y, int bitlength, Algorithm algorithm) {
    throw new IllegalArgumentException("Don't support equal now!");
  }


  @Override
  public DRes<List<SIntPair>> carry(List<SIntPair> bitPairs) {
    return builder.append(new MdmlCarryProtocol<>(bitPairs));
  }

  @Override
  public DRes<Pair<List<DRes<SInt>>, SInt>> argMin(List<DRes<SInt>> xs) {
    throw new UnsupportedOperationException("Not supported to argMin in Comparison");
  }


  protected MdmlCompUIntFactory<PlainT> getFactory() {
    return factory;
  }

}
