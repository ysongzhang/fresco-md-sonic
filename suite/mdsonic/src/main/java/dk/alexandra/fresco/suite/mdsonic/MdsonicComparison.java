package dk.alexandra.fresco.suite.mdsonic;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.*;
import dk.alexandra.fresco.framework.builder.numeric.mdsonic.ComparisonMdsonic;
import dk.alexandra.fresco.framework.builder.numeric.mdsonic.LogicalBoolean;
import dk.alexandra.fresco.framework.util.SBoolPair;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicCompUInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicCompUIntFactory;
import dk.alexandra.fresco.suite.mdsonic.protocols.computations.ASS.lt.BitLessThanOpenASS;
import dk.alexandra.fresco.suite.mdsonic.protocols.computations.ASS.MostSignBitASS;
import dk.alexandra.fresco.suite.mdsonic.protocols.computations.MSS.MostSignBitMSS;
import dk.alexandra.fresco.suite.mdsonic.protocols.computations.MSS.ReluMSS;
import dk.alexandra.fresco.suite.mdsonic.protocols.natives.ASS.MdsonicCarryProtocolASS;
import dk.alexandra.fresco.suite.mdsonic.protocols.natives.MdsonicCarryProtocolOpt;

import java.util.List;

/**
 * Mdsonic optimized protocols for comparison.
 */
public class MdsonicComparison<PlainT extends MdsonicCompUInt<?, ?, PlainT>> implements ComparisonMdsonic {

  private final BuilderFactoryNumeric factoryNumeric;
  private final MdsonicCompUIntFactory<PlainT> factory;
  private final ProtocolBuilderNumeric builder;

  private final boolean useMaskedEvaluation;

  protected MdsonicComparison(BuilderFactoryNumeric factoryNumeric,
                              ProtocolBuilderNumeric builder,
                              MdsonicCompUIntFactory<PlainT> factory,
                              boolean useMaskedEvaluation) {
    this.factoryNumeric = factoryNumeric;
    this.builder = builder;
    this.factory = factory;
    this.useMaskedEvaluation = useMaskedEvaluation;
  }

  @Override
  public DRes<SBool> compareLT(DRes<SInt> x1, DRes<SInt> x2) {
    DRes<SInt> diff = builder.numericMdsonic().sub(x1, x2);
    if (useMaskedEvaluation) {
      return builder.seq(new MostSignBitMSS<>(diff, factory));
    } else {
      return builder.seq(new MostSignBitASS<>(diff, factory));
    }
  }

  @Override
  public DRes<SInt> relu(DRes<SInt> input) {
    if (useMaskedEvaluation) {
      return builder.seq(new ReluMSS<>(input, factory));  // return MSS
    } else {
      throw new UnsupportedOperationException("ASS doesn't support to relu protocol for SInt");
    }
  }

  @Override
  public DRes<SBool> compareLEQ(DRes<SInt> x, DRes<SInt> y) {
    DRes<SBool> compare = compareLT(y, x);
    boolean oint = true;
    LogicalBoolean logicalBuilder = builder.logicalBoolean();
    return logicalBuilder.xorKnown(() -> oint, compare);
  }

  @Override
  public DRes<SBool> compareLTBits(DRes<OInt> openValue, DRes<List<DRes<SBool>>> secretBits) {
    if (useMaskedEvaluation) {
      throw new UnsupportedOperationException("MSS: Don't support bitwise-less-than");
    } else {
      return builder.seq(new BitLessThanOpenASS(openValue, secretBits));
    }
  }

  @Override
  public DRes<List<SBoolPair>> carry(List<SBoolPair> bitPairs) {
    if (useMaskedEvaluation) {
      return builder.append(new MdsonicCarryProtocolOpt<>(bitPairs));  // Our optimized carry-out bit computation
//      return builder.append(new MdsonicCarryProtocolASS<>(bitPairs));  // use ASS directly
    } else {
      return builder.append(new MdsonicCarryProtocolASS<>(bitPairs));
    }
  }

  @Override
  public DRes<SBool> equals(DRes<SInt> x, DRes<SInt> y, int bitlength) {
    throw new UnsupportedOperationException("Mdsonic: Don't support equal now");
  }

  @Override
  public DRes<SBool> equals(DRes<SInt> x, DRes<SInt> y) {
    int bitLength = factoryNumeric.getBasicNumericContext().getMaxBitLength();
    return equals(x, y, bitLength);
  }

  @Override
  public DRes<SBool> compareZero(DRes<SInt> x, int bitlength) {
    throw new UnsupportedOperationException("Mdsonic: Don't support compare zero now");
  }

  protected MdsonicCompUIntFactory<PlainT> getFactory() {
    return factory;
  }

}
