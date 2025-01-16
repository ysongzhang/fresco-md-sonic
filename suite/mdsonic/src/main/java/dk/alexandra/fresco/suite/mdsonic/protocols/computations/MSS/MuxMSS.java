package dk.alexandra.fresco.suite.mdsonic.protocols.computations.MSS;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.mdsonic.ConversionMdsonic;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.SInt;

/**
 * MUX Computation.
 */
public class MuxMSS implements Computation<SInt, ProtocolBuilderNumeric> {  // return MSS

  private final DRes<SInt> value;
  private final DRes<SBool> bit;

  public MuxMSS(DRes<SInt> value, DRes<SBool> bit) {
    this.value = value;  // x
    this.bit = bit;  // b'
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> seq.advancedNumericMdsonic().daBit())
        .par((par, daBit) -> {
          // B2A
          DRes<Boolean> sigma = par.logicalBoolean().maskOpen(bit, daBit.getBitA());  // sigma
          DRes<SInt> epsilon = daBit.getBitB();  // epsilon
          DRes<OInt> mu = par.numericMdsonic().getOpened(value);
          DRes<SInt> mid = par.numericMdsonic(false).multByOpen(mu, epsilon);  // epsilon * mu
          DRes<SInt> epsilonX = par.numericMdsonic(false).add(mid, par.numericMdsonic().getProduct()); // epsilon * x
          final Pair<DRes<Boolean>, DRes<SInt>> resPair = new Pair<>(sigma, epsilonX);
          return () -> resPair;
        }).seq((seq, pair) -> {
          ConversionMdsonic conversionMdsonic = seq.conversionMdsonic();
          Boolean sigma = pair.getFirst().out();
          DRes<SInt> epsilonX = pair.getSecond();
          DRes<SInt> res;
          if (sigma) {
              res = seq.numericMdsonic(false).sub(conversionMdsonic.toASSArithmetic(value), epsilonX);
          } else {
              res = epsilonX;
          }
          return conversionMdsonic.toMSSArithmetic(res);  // ASS to MSS
        });
  }
}
