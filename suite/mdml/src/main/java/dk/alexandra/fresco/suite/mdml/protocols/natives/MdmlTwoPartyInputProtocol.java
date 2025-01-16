package dk.alexandra.fresco.suite.mdml.protocols.natives;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUIntFactory;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlInputMask;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlASIntArithmetic;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlMSIntArithmetic;
import dk.alexandra.fresco.suite.mdml.resource.MdmlResourcePool;
import dk.alexandra.fresco.suite.mdml.resource.storage.MdmlDataSupplier;

/**
 * Native protocol for inputting data.
 */
public class MdmlTwoPartyInputProtocol<PlainT extends MdmlCompUInt<?, ?, PlainT>>
    extends MdmlNativeProtocol<SInt, PlainT> {

  private final PlainT input;
  private final int inputPartyId;
  private MdmlInputMask<PlainT> inputMask;
  private SInt share;

  /**
   * Creates new {@link MdmlTwoPartyInputProtocol}.
   *
   * @param input value to secret-share
   * @param inputPartyId id of input party
   */
  public MdmlTwoPartyInputProtocol(PlainT input, int inputPartyId) {
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
      this.share = out;
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public SInt out() {
    return share;
  }

}
