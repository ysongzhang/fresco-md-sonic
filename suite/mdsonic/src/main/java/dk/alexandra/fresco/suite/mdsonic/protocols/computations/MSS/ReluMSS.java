package dk.alexandra.fresco.suite.mdsonic.protocols.computations.MSS;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicCompUInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicCompUIntFactory;

/**
 * RELU Computation.
 */
public class ReluMSS <PlainT extends MdsonicCompUInt<?, ?, PlainT>> implements
        Computation<SInt, ProtocolBuilderNumeric> {  // return MSS

  private final DRes<SInt> value;  // MSS
  private final MdsonicCompUIntFactory<PlainT> factory;

  public ReluMSS(DRes<SInt> value, MdsonicCompUIntFactory<PlainT> factory) {
    this.value = value;
    this.factory = factory;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(new MostSignBitMSS<>(value, factory))  // ASS
        .par((par, msb) -> {
          DRes<SBool> nMsb = par.logicalBoolean(false).not(msb);
          return par.advancedNumericMdsonic().mux(value, nMsb);
        });
  }
}
