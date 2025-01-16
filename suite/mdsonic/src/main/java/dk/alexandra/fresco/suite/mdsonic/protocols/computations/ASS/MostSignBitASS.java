package dk.alexandra.fresco.suite.mdsonic.protocols.computations.ASS;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.mdsonic.*;
import dk.alexandra.fresco.framework.network.serializers.BooleanSerializer;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.OIntArithmetic;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicCompUInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicCompUIntFactory;

import java.util.List;

/**
 * Extract the value of the most significant bit of value.
 */
public class MostSignBitASS<PlainT extends MdsonicCompUInt<?, ?, PlainT>> implements
    Computation<SBool, ProtocolBuilderNumeric> {

  private final DRes<SInt> value;
  private final MdsonicCompUIntFactory<PlainT> factory;
  private final int k;

  public MostSignBitASS(DRes<SInt> value, MdsonicCompUIntFactory<PlainT> factory) {
    this.value = value;
    this.factory = factory;
    this.k = factory.getLowBitLength();
  }

  @Override
  public DRes<SBool> buildComputation(ProtocolBuilderNumeric builder) {
    OIntArithmetic arithmetic = builder.getOIntArithmetic();
    DRes<OInt> twoTo2k1 = arithmetic.twoTo(k - 1);  // 2^{k-1}
    return builder.seq(seq -> seq.advancedNumericMdsonic().edaBit())
        .seq((seq, edaBit) -> {
          NumericMdsonic numeric = seq.numericMdsonic();
          DRes<SInt> r = edaBit.getValue();
          DRes<SInt> c = numeric.add(value, r);
          DRes<OInt> cOpen = numeric.openAsOInt(c);
          final Pair<DRes<OInt>, FMedaBit> resPair = new Pair<>(cOpen, edaBit);
          return () -> resPair;
        }).seq((seq, pair) -> {
          NumericMdsonic nb = seq.numericMdsonic();
          ConversionMdsonic convert = seq.conversionMdsonic();
          // 计算rPrime
          FMedaBit edaBit = pair.getSecond();
          DRes<SInt> r = edaBit.getValue();
          List<DRes<SBool>> rBits = edaBit.getValueBooleanList();
          DRes<SBool> rMSB = rBits.get(k - 1);
          DRes<SInt> rMSBSInt = convert.toArithmetic(rMSB);
          DRes<SInt> rPrime = nb.sub(r, nb.multByOpen(twoTo2k1, rMSBSInt));
          PlainT cOpen = factory.fromOInt(pair.getFirst());
          PlainT cPrime = cOpen.clearAboveBitAt(k - 1);
          List<DRes<SBool>> rBitskd1 = rBits.subList(0, k - 1);
          DRes<SBool> u = seq.comparisonMdsonic().compareLTBits(cPrime, () -> rBitskd1);

          // B2A
          DRes<SInt> uSInt = convert.toArithmetic(u);
          DRes<SInt> aPrime = nb.add(nb.subFromOpen(() -> cPrime, rPrime), nb.multByOpen(twoTo2k1, uSInt));
          return nb.sub(value, aPrime);
        }).seq((seq, d) -> {
          DRes<DaBit> daBit = seq.advancedNumericMdsonic().daBit();
          final Pair<DRes<DaBit>, DRes<SInt>> resPair = new Pair<>(daBit, d);
          return () -> resPair;
        }).seq((seq, pair) -> {
          NumericMdsonic nb = seq.numericMdsonic();
          DRes<SInt> b = pair.getFirst().out().getBitB();
          DRes<SInt> e = nb.add(pair.getSecond(), nb.multByOpen(twoTo2k1, b));
          DRes<OInt> eOpen = nb.openAsOInt(e);
          final Pair<DRes<OInt>, DRes<SBool>> resPair = new Pair<>(eOpen, pair.getFirst().out().getBitA());
          return () -> resPair;
        }).seq((seq, pair) -> {
          LogicalBoolean nb = seq.logicalBoolean();
          PlainT eOpen = factory.fromOInt(pair.getFirst());
          DRes<SBool> b = pair.getSecond();
          boolean eMsb = BooleanSerializer.fromBytes((byte) eOpen.testBitAsUInt(k - 1).bitValue());
          return nb.xorKnown(() -> eMsb, b);
        });
  }
}
