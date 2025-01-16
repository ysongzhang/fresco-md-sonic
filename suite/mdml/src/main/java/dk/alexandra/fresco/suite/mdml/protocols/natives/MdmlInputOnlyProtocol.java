package dk.alexandra.fresco.suite.mdml.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUIntFactory;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlInputMask;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlMSIntArithmetic;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlASIntArithmetic;
import dk.alexandra.fresco.suite.mdml.protocols.computations.InputComputationMdml;
import dk.alexandra.fresco.suite.mdml.resource.MdmlResourcePool;
import dk.alexandra.fresco.suite.mdml.resource.storage.MdmlDataSupplier;

/**
 * Native protocol for inputting data. <p>This is used by native computation {@link
 * InputComputationMdml}. The result of
 * this protocol is this party's share of the input, as well as the bytes of the masked input which
 * are later used in a broadcast validation.</p>
 */
public class MdmlInputOnlyProtocol<PlainT extends MdmlCompUInt<?, ?, PlainT>>
    extends MdmlNativeProtocol<Pair<DRes<SInt>, byte[]>, PlainT> {

  private final PlainT input;
  private final int inputPartyId;
  private MdmlInputMask<PlainT> inputMask;
  private Pair<DRes<SInt>, byte[]> shareAndMaskBytes;

  /**
   * Creates new {@link MdmlInputOnlyProtocol}.
   *
   * @param input value to secret-share
   * @param inputPartyId id of input party
   */
  public MdmlInputOnlyProtocol(PlainT input, int inputPartyId) {
    this.input = input;
    this.inputPartyId = inputPartyId;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdmlResourcePool<PlainT> resourcePool,
      Network network) {
    MdmlCompUIntFactory<PlainT> factory = resourcePool.getFactory();
    int myId = resourcePool.getMyId();
    ByteSerializer<PlainT> serializer = resourcePool.getPlainSerializer();
    MdmlDataSupplier<PlainT> dataSupplier = resourcePool.getDataSupplier();
    if (round == 0) {
      inputMask = dataSupplier.getNextInputMask(inputPartyId);
      if (myId == inputPartyId) {
        PlainT bcValue = this.input.add(inputMask.getOpenValue());
        network.sendToAll(serializer.serialize(bcValue));
      }
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      byte[] inputMaskBytes = network.receive(inputPartyId);
      MdmlASIntArithmetic<PlainT> maskShare = inputMask.getMaskShare();
      MdmlMSIntArithmetic<PlainT> out = new MdmlMSIntArithmetic<>(maskShare, serializer.deserialize(inputMaskBytes));
      this.shareAndMaskBytes = new Pair<>(out, inputMaskBytes);
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public Pair<DRes<SInt>, byte[]> out() {
    return shareAndMaskBytes;
  }

}
