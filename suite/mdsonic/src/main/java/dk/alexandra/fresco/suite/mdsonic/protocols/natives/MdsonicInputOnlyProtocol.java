package dk.alexandra.fresco.suite.mdsonic.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;
import dk.alexandra.fresco.suite.mdsonic.protocols.computations.InputComputationMdsonic;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;
import dk.alexandra.fresco.suite.mdsonic.resource.storage.MdsonicDataSupplier;

/**
 * Native protocol for inputting data. <p>This is used by native computation {@link
 * InputComputationMdsonic}. The result of
 * this protocol is this party's share of the input, as well as the bytes of the masked input which
 * are later used in a broadcast validation.</p>
 */
public class MdsonicInputOnlyProtocol<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>>
    extends MdsonicNativeProtocol<Pair<DRes<SInt>, byte[]>, PlainT, SecretP> {

  private final boolean useMaskedEvaluation;
  private final PlainT input;
  private final int inputPartyId;
  private MdsonicInputMask<PlainT> inputMask;
  private Pair<DRes<SInt>, byte[]> shareAndMaskBytes;

  /**
   * Creates new {@link MdsonicInputOnlyProtocol}.
   *
   * @param input value to secret-share
   * @param inputPartyId id of input party
   */
  public MdsonicInputOnlyProtocol(PlainT input, int inputPartyId) {
    this.input = input;
    this.inputPartyId = inputPartyId;
    this.useMaskedEvaluation = false;
  }

  public MdsonicInputOnlyProtocol(PlainT input, int inputPartyId, boolean useMaskedEvaluation) {
    this.input = input;
    this.inputPartyId = inputPartyId;
    this.useMaskedEvaluation = useMaskedEvaluation;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    MdsonicCompUIntFactory<PlainT> factory = resourcePool.getFactory();
    int myId = resourcePool.getMyId();
    ByteSerializer<PlainT> serializer = resourcePool.getPlainSerializer();
    MdsonicDataSupplier<PlainT, SecretP> dataSupplier = resourcePool.getDataSupplier();
    if (round == 0) {
      inputMask = dataSupplier.getNextInputMask(inputPartyId);
      if (myId == inputPartyId) {
        PlainT bcValue = this.input.subtract(inputMask.getOpenValue());
        network.sendToAll(serializer.serialize(bcValue));
      }
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      byte[] inputMaskBytes = network.receive(inputPartyId);
      MdsonicASIntArithmetic<PlainT> maskShare = inputMask.getMaskShare();
      if (useMaskedEvaluation) {
        MdsonicMSIntArithmetic<PlainT> out = new MdsonicMSIntArithmetic<>(maskShare, serializer.deserialize(inputMaskBytes));
        this.shareAndMaskBytes = new Pair<>(out, inputMaskBytes);
      } else {
        PlainT macKeyShare = dataSupplier.getSecretSharedKey();  // MAC Key shares
        MdsonicASIntArithmetic<PlainT> out = maskShare.addConstant(
                serializer.deserialize(inputMaskBytes),
                macKeyShare,
                factory.zero(),
                myId == 1);
        this.shareAndMaskBytes = new Pair<>(out, inputMaskBytes);
      }
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public Pair<DRes<SInt>, byte[]> out() {
    return shareAndMaskBytes;
  }

}
