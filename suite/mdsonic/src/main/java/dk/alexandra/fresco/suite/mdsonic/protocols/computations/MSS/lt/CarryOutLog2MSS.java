package dk.alexandra.fresco.suite.mdsonic.protocols.computations.MSS.lt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.mdsonic.LogicalBoolean;
import dk.alexandra.fresco.framework.util.SBoolPair;
import dk.alexandra.fresco.framework.value.SBool;

import java.util.ArrayList;
import java.util.List;

/**
 * Given values a and b represented as bits, computes if a + b overflows, i.e., if the addition
 * results in a carry.
 */
public class CarryOutLog2MSS implements Computation<SBool, ProtocolBuilderNumeric> {  // little-endian

  private final DRes<List<Boolean>> openBitsDef;
  private final DRes<List<DRes<SBool>>> secretBitsDef;
  private final DRes<List<DRes<SBool>>> productBitsDef;

  /**
   * Constructs new {@link CarryOutLog2MSS}.
   *
   * @param clearBits clear bits
   * @param secretBits secret bits
   */
  public CarryOutLog2MSS(DRes<List<Boolean>> clearBits, DRes<List<DRes<SBool>>> secretBits, DRes<List<DRes<SBool>>> productBitsDef) {
    this.secretBitsDef = secretBits;
    this.openBitsDef = clearBits;
    this.productBitsDef = productBitsDef;
  }

  @Override
  public DRes<SBool> buildComputation(ProtocolBuilderNumeric builder) {
    List<DRes<SBool>> secretBits = secretBitsDef.out();
    List<DRes<SBool>> productBits = productBitsDef.out();
    List<Boolean> openBits = openBitsDef.out();
    if (secretBits.size() != openBits.size()) {
      throw new IllegalArgumentException("Number of bits must be the same");
    }
    return builder.par(par -> {
      List<SBoolPair> pairs = new ArrayList<>(secretBits.size() / 2);
      LogicalBoolean logicalBoolean = par.logicalBoolean(false);
      for (int i = 0; i < secretBits.size() / 2; i++) {
        Boolean a2ia2ip1 = openBits.get(2 * i) & openBits.get(2 * i +1);
        Boolean a2i = openBits.get(2 * i);
        Boolean a2ip1 = openBits.get(2 * i + 1);
        DRes<SBool> prod = productBits.get(i);
        DRes<SBool> a2ib2ip1 = logicalBoolean.andKnown(() -> a2i, secretBits.get(2 * i + 1));
        DRes<SBool> a2ip1b2i = logicalBoolean.andKnown(() -> a2ip1, secretBits.get(2 * i));
        DRes<SBool> a2ip1b2ip1 = logicalBoolean.andKnown(() -> a2ip1, secretBits.get(2 * i + 1));
        DRes<SBool> a2ia2ip1b2i = logicalBoolean.andKnown(() -> a2ia2ip1, secretBits.get(2 * i));
        DRes<SBool> a2iprod = logicalBoolean.andKnown(() -> a2i, prod);
        DRes<SBool> pValue = logicalBoolean.xor(
                a2ip1b2i, logicalBoolean.xor(
                a2ib2ip1, logicalBoolean.xorKnown(
                        () -> a2ia2ip1, productBits.get(i))));
        DRes<SBool> gValue = logicalBoolean.xor(a2iprod, logicalBoolean.xor(a2ip1b2ip1, a2ia2ip1b2i));
        pairs.add(new SBoolPair(pValue, gValue));
      }
      if (secretBits.size() % 2 != 0) {
        DRes<SBool> xored = logicalBoolean.xorKnown(() -> openBits.get(secretBits.size() - 1), secretBits.get(secretBits.size() - 1));
        DRes<SBool> anded = logicalBoolean.andKnown(() -> openBits.get(secretBits.size() - 1), secretBits.get(secretBits.size() - 1));
        pairs.add(new SBoolPair(xored, anded));
      }
      return par.seq(new PreCarryBitsMSS(pairs));
    });
  }
}
