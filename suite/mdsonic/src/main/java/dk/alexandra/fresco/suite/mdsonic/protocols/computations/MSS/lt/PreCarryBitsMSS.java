package dk.alexandra.fresco.suite.mdsonic.protocols.computations.MSS.lt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.SBoolPair;
import dk.alexandra.fresco.framework.value.SBool;

import java.util.List;

public class PreCarryBitsMSS implements Computation<SBool, ProtocolBuilderNumeric> {

  private final List<SBoolPair> pairsDef;  // little-endian

  PreCarryBitsMSS(List<SBoolPair> pairs) {
    this.pairsDef = pairs;
  }

  @Override
  public DRes<SBool> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> () -> pairsDef)
        .whileLoop((pairs) -> pairs.size() > 1,
            (prevScope, pairs) -> prevScope.par(par -> par.comparisonMdsonic().carry(pairs)))
        .seq((seq, out) -> out.get(0).getSecond());
  }

}
