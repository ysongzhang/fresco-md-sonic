package dk.alexandra.fresco.suite.mdsonic.protocols.computations;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicCompUInt;
import dk.alexandra.fresco.suite.mdsonic.protocols.natives.BroadcastValidationProtocol;
import dk.alexandra.fresco.suite.mdsonic.protocols.natives.MdsonicInputOnlyProtocol;

/**
 * Native computation for inputting private data. <p>Consists of native protocols {@link
 * dk.alexandra.fresco.suite.mdsonic.protocols.natives.MdsonicInputOnlyProtocol} and {@link BroadcastValidationProtocol}.
 * The first returns this party's share of the input along with the bytes of the masked input.
 * The second step runs a broadcast validation of the bytes of the masked input (if more than
 * two parties are carrying out the computation).</p>
 */
public class InputComputationMdsonic<PlainT extends MdsonicCompUInt<?, ?, PlainT>> implements
    Computation<SInt, ProtocolBuilderNumeric> {

  private final boolean useMaskedEvaluation;
  private final PlainT input;
  private final int inputPartyId;

  public InputComputationMdsonic(PlainT input, int inputPartyId) {
    this.inputPartyId = inputPartyId;
    this.input = input;
    this.useMaskedEvaluation = false;
  }

  public InputComputationMdsonic(PlainT input, int inputPartyId, boolean useMaskedEvaluation) {
    this.inputPartyId = inputPartyId;
    this.input = input;
    this.useMaskedEvaluation = useMaskedEvaluation;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    DRes<Pair<DRes<SInt>, byte[]>> shareAndMaskBytes;
    shareAndMaskBytes = builder.append(new MdsonicInputOnlyProtocol<>(input, inputPartyId, useMaskedEvaluation));
    return builder.seq(seq -> {
      Pair<DRes<SInt>, byte[]> unwrapped = shareAndMaskBytes.out();
      seq.append(new BroadcastValidationProtocol<>(unwrapped.getSecond()));
      return unwrapped.getFirst();
    });
  }

}
