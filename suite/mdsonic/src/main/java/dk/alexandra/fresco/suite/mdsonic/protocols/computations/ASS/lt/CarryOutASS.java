package dk.alexandra.fresco.suite.mdsonic.protocols.computations.ASS.lt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.SBoolPair;
import dk.alexandra.fresco.framework.value.SBool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Given values a and b represented as bits, computes if a + b overflows, i.e., if the addition
 * results in a carry.
 */
public class CarryOutASS implements Computation<SBool, ProtocolBuilderNumeric> {

  private final DRes<List<Boolean>> openBitsDef;
  private final DRes<List<DRes<SBool>>> secretBitsDef;
  private final DRes<Boolean> carryIn;
  private final boolean reverseSecretBits;

  /**
   * Constructs new {@link CarryOutASS}.
   *
   * @param clearBits clear bits
   * @param secretBits secret bits
   * @param carryIn an additional carry-in bit which we add to the least-significant bits of the
   * inputs
   * @param reverseSecretBits indicates whether secret bits need to be reverse (to account for
   * endianness)
   */
  public CarryOutASS(DRes<List<Boolean>> clearBits, DRes<List<DRes<SBool>>> secretBits,
                     DRes<Boolean> carryIn, boolean reverseSecretBits) {
    this.secretBitsDef = secretBits;
    this.openBitsDef = clearBits;
    this.carryIn = carryIn;
    this.reverseSecretBits = reverseSecretBits;
  }

  public CarryOutASS(DRes<List<Boolean>> clearBits, DRes<List<DRes<SBool>>> secretBits,
                     DRes<Boolean> carryIn) {
    this(clearBits, secretBits, carryIn, false);
  }

  @Override
  public DRes<SBool> buildComputation(ProtocolBuilderNumeric builder) {
    List<DRes<SBool>> secretBits = secretBitsDef.out();
    if (reverseSecretBits) {  // 转换为BigOrder
      Collections.reverse(secretBits);
    }
    List<Boolean> openBits = openBitsDef.out();
    if (secretBits.size() != openBits.size()) {
      throw new IllegalArgumentException("Number of bits must be the same");
    }
    return builder.par(par -> {
      DRes<List<DRes<SBool>>> xored = par.logicalBoolean().pairWiseXorKnown(openBitsDef, secretBitsDef);
      DRes<List<DRes<SBool>>> anded = par.logicalBoolean().pairWiseAndKnown(openBitsDef, secretBitsDef);
      final Pair<DRes<List<DRes<SBool>>>, DRes<List<DRes<SBool>>>> pair = new Pair<>(xored,
          anded);
      return () -> pair;
    }).par((par, pair) -> {
      List<DRes<SBool>> xoredBits = pair.getFirst().out();
      List<DRes<SBool>> andedBits = pair.getSecond().out();
      List<SBoolPair> pairs = new ArrayList<>(andedBits.size());
      for (int i = 0; i < secretBits.size(); i++) {
        DRes<SBool> xoredBit = xoredBits.get(i);
        DRes<SBool> andedBit = andedBits.get(i);
        pairs.add(new SBoolPair(xoredBit, andedBit));
      }
      return () -> pairs;
    }).seq((seq, pairs) -> {
      // need to account for carry-in bit
      int lastIdx = pairs.size() - 1;
      SBoolPair lastPair = pairs.get(lastIdx);
      DRes<SBool> lastCarryPropagator = seq.logicalBoolean().halfOr(
          lastPair.getSecond(),
          seq.logicalBoolean().andKnown(carryIn, lastPair.getFirst()));
      pairs.set(lastIdx, new SBoolPair(lastPair.getFirst(), lastCarryPropagator));
      return seq.seq(new PreCarryBitsASS(pairs));
    });
  }

}
