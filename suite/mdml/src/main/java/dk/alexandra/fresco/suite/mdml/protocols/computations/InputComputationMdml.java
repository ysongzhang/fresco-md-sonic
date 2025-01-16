package dk.alexandra.fresco.suite.mdml.protocols.computations;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUInt;
import dk.alexandra.fresco.suite.mdml.protocols.natives.BroadcastValidationProtocol;
import dk.alexandra.fresco.suite.mdml.protocols.natives.MdmlInputOnlyProtocol;

/**
 * Native computation for inputting private data. <p>Consists of native protocols {@link
 * MdmlInputOnlyProtocol} and {@link BroadcastValidationProtocol}. The first returns this party's
 * share of the input along with the bytes of the masked input. The second step runs a broadcast
 * validation of the bytes of the masked input (if more than two parties are carrying out the
 * computation).</p>
 */
public class InputComputationMdml<PlainT extends MdmlCompUInt<?, ?, PlainT>> implements
    Computation<SInt, ProtocolBuilderNumeric> {

  private final PlainT input;
  private final int inputPartyId;

  public InputComputationMdml(PlainT input, int inputPartyId) {
    this.inputPartyId = inputPartyId;
    this.input = input;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    DRes<Pair<DRes<SInt>, byte[]>> shareAndMaskBytes = builder
        .append(new MdmlInputOnlyProtocol<>(input, inputPartyId));
    return builder.seq(seq -> {
      Pair<DRes<SInt>, byte[]> unwrapped = shareAndMaskBytes.out();
      seq.append(new BroadcastValidationProtocol<>(unwrapped.getSecond()));
      return unwrapped.getFirst();
    });
  }

}
