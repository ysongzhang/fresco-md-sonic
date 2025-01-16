package dk.alexandra.fresco.suite.mdsonic.protocols.computations.ASS.lt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.OIntFactory;
import dk.alexandra.fresco.framework.value.SBool;

import java.util.List;

/**
 * Given known value a and secret value b represented as bits, computes a <? b.
 */
public class BitLessThanOpenASS implements Computation<SBool, ProtocolBuilderNumeric> {

  private final DRes<OInt> openValueDef;
  private final DRes<List<DRes<SBool>>> secretBitsDef;

  public BitLessThanOpenASS(DRes<OInt> openValue, DRes<List<DRes<SBool>>> secretBits) {
    this.openValueDef = openValue;
    this.secretBitsDef = secretBits;
  }

  @Override
  public DRes<SBool> buildComputation(ProtocolBuilderNumeric builder) {
    OIntFactory oIntFactory = builder.getOIntFactory();
    List<DRes<SBool>> secretBits = secretBitsDef.out();
    OInt openValueA = openValueDef.out();
    int numBits = secretBits.size();
    List<Boolean> openBits = builder.getOIntArithmetic().toBitsBoolean(openValueA, numBits);  // return big-endian
    DRes<List<DRes<SBool>>> secretBitsNegated = builder.logicalBoolean().batchedNot(secretBitsDef);
    DRes<SBool> gt = builder
        .seq(new CarryOutASS(() -> openBits, secretBitsNegated, () -> true, true));
    return builder.logicalBoolean().not(gt);
  }

}
