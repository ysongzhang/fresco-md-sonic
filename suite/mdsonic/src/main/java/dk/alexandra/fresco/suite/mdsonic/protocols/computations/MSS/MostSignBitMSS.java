package dk.alexandra.fresco.suite.mdsonic.protocols.computations.MSS;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.mdsonic.*;
import dk.alexandra.fresco.framework.network.serializers.BooleanSerializer;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdsonic.protocols.computations.MSS.lt.CarryOutLog2MSS;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicCompUInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicCompUIntFactory;

import java.util.Collections;
import java.util.List;

/**
 * Extract the value of the most significant bit of value.
 */
public class MostSignBitMSS<PlainT extends MdsonicCompUInt<?, ?, PlainT>> implements
    Computation<SBool, ProtocolBuilderNumeric> {

  private final DRes<SInt> value;
  private final MdsonicCompUIntFactory<PlainT> factory;
  private final int k;
  private final int FMDegree;

  public MostSignBitMSS(DRes<SInt> value, MdsonicCompUIntFactory<PlainT> factory) {
    this.value = value;
    this.factory = factory;
    this.k = factory.getLowBitLength();
    this.FMDegree = 2;
  }

  @Override
  public DRes<SBool> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> seq.advancedNumericMdsonic().edaBit(k - 1, FMDegree))
        .seq((seq, edaBit) -> {
          NumericMdsonic numeric = seq.numericMdsonic();
          DRes<OInt> a = numeric.getOpened(value);
          final Pair<DRes<OInt>, FMedaBit> resPair = new Pair<>(a, edaBit);
          return () -> resPair;
        }).seq((seq, pair) -> {
          LogicalBoolean logical = seq.logicalBoolean(false);
          FMedaBit edaBit = pair.getSecond();
          PlainT a = factory.fromOInt(pair.getFirst());
          PlainT beta = a.add(factory.fromOInt(edaBit.getOpenedValue()));
          List<DRes<SBool>> rBits = edaBit.getValueBooleanList();  // little-endian
          List<DRes<SBool>> rBitskd1 = rBits.subList(0, k - 1);
          List<DRes<SBool>> prods = edaBit.getValueProductList();
          DRes<SBool> rMSB = rBits.get(k - 1);
          boolean betaMSB = BooleanSerializer.fromBytes((byte) beta.testBitAsUInt(k - 1).bitValue());
          List<Boolean> openBits = seq.getOIntArithmetic().toBitsBoolean(pair.getFirst().out(), k - 1);
          Collections.reverse(openBits);  // little-endian
          DRes<SBool> c = seq.seq(new CarryOutLog2MSS(() -> openBits, () -> rBitskd1, () -> prods));  // ASS
          return logical.xorKnown(() -> betaMSB, logical.xor(rMSB, c));  // return ASS
        });
  }
}
