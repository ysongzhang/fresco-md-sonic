package dk.alexandra.fresco.suite.mdsonic.protocols.computations.ASS.real;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.mdsonic.AdvancedNumericMdsonic.TruncationPair;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;

/**
 * Probabilistic truncation protocol. <p>Described by Mohassel and Rindal in
 * https://eprint.iacr.org/2018/403.pdf (Figure 3).</p>
 */
public class TruncateFromPairsASS implements Computation<SInt, ProtocolBuilderNumeric> {

  private final DRes<SInt> input;
  private final int shifts;

  public TruncateFromPairsASS(DRes<SInt> input, int shifts) {
    this.input = input;
    this.shifts = shifts;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    DRes<TruncationPair> truncationPairD = builder.advancedNumericMdsonic().generateTruncationPair(shifts);
    return builder.seq(seq -> {
      TruncationPair truncationPair = truncationPairD.out();
      // TODO look into making fixed-point arithmetic  tests pass when we subtract here (to be
      // consistent with original protocol)
      DRes<SInt> masked = seq.numericMdsonic().add(input, truncationPair.getRPrime());
//      DRes<SInt> masked = seq.numeric().sub(truncationPair.getRPrime(), input);
      return seq.numericMdsonic().openAsOInt(masked);
    }).seq((seq, opened) -> {
      OInt shifted = seq.getOIntArithmetic().shiftRight(opened, shifts);
      DRes<SInt> r = truncationPairD.out().getR();
      return seq.numericMdsonic().subFromOpen(shifted, r);
//      return seq.numeric().subOpen(r, shifted);
    });
  }
}
