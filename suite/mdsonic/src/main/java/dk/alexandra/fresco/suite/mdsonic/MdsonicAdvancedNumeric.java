package dk.alexandra.fresco.suite.mdsonic;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.mdsonic.AdvancedNumericMdsonic;
import dk.alexandra.fresco.framework.builder.numeric.mdsonic.DaBit;
import dk.alexandra.fresco.framework.builder.numeric.mdsonic.FMedaBit;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdsonic.protocols.computations.ASS.real.TruncateFromPairsASS;
import dk.alexandra.fresco.suite.mdsonic.protocols.computations.MSS.MuxMSS;
import dk.alexandra.fresco.suite.mdsonic.protocols.natives.MSS.TruncateFromPairsMSS;
import dk.alexandra.fresco.suite.mdsonic.protocols.natives.MdsonicDaBitProtocol;
import dk.alexandra.fresco.suite.mdsonic.protocols.natives.MdsonicEdaBitProtocol;
import dk.alexandra.fresco.suite.mdsonic.protocols.natives.MdsonicMatrixTruncationPairProtocol;
import dk.alexandra.fresco.suite.mdsonic.protocols.natives.MdsonicTruncationPairProtocol;

/**
 * MD-SONIC-specific advanced numeric functionality.
 */
public class MdsonicAdvancedNumeric implements AdvancedNumericMdsonic {

  private final BuilderFactoryNumeric factoryNumeric;
  private final ProtocolBuilderNumeric builder;
  private final boolean useMaskedEvaluation;

  MdsonicAdvancedNumeric(
      BuilderFactoryNumeric factoryNumeric,
      ProtocolBuilderNumeric builder, boolean useMaskedEvaluation) {
    this.factoryNumeric = factoryNumeric;
    this.builder = builder;
    this.useMaskedEvaluation = useMaskedEvaluation;
  }

  @Override
  public DRes<DaBit> daBit() {
    return builder.append(new MdsonicDaBitProtocol<>(useMaskedEvaluation));
  }

  @Override
  public DRes<FMedaBit> edaBit() {
    if (useMaskedEvaluation) {
      return builder.append(new MdsonicEdaBitProtocol<>());
    } else {
      return builder.append(new MdsonicEdaBitProtocol<>());
    }
  }

  @Override
  public DRes<FMedaBit> edaBit(int FMLength, int FMDegree) {
    if (useMaskedEvaluation) {
      return builder.append(new MdsonicEdaBitProtocol<>(FMLength, FMDegree));
    } else {
      throw new UnsupportedOperationException("ASS doesn't support to FMedaBit");
    }
  }

  @Override
  public DRes<SInt> truncate(DRes<SInt> input, int shifts) {
    if (useMaskedEvaluation) {
      return builder.append(new TruncateFromPairsMSS<>(input, shifts));
    } else {
      return builder.seq(new TruncateFromPairsASS(input, shifts));
    }
  }

  @Override
  public DRes<SInt> truncate(DRes<SInt> input, int shifts, DRes<SInt> truncationR) {
    if (useMaskedEvaluation) {
      return builder.append(new TruncateFromPairsMSS<>(input, shifts, truncationR));
    } else {
      throw new UnsupportedOperationException("ASS doesn't support to truncate with truncationR");
    }
  }

  @Override
  public DRes<SInt> mux(DRes<SInt> input, DRes<SBool> bit) {
    if (useMaskedEvaluation) {
      return builder.seq(new MuxMSS(input, bit));  // return MSS
    } else {
      throw new UnsupportedOperationException("ASS doesn't support to MUX protocol");
    }
  }

  @Override
  public DRes<TruncationPair> generateTruncationPair(int d) {
    return builder.seq(seq -> seq.append(new MdsonicTruncationPairProtocol<>(d)));
  }

  @Override
  public DRes<MatrixTruncationPair> generateMatrixTruncationPair(int d, int height, int width) {
    return builder.seq(seq -> seq.append(new MdsonicMatrixTruncationPairProtocol<>(d, height, width)));
  }

}
