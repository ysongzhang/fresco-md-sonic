package dk.alexandra.fresco.suite.mdsonic.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;
import dk.alexandra.fresco.suite.mdsonic.resource.storage.MdsonicDataSupplier;

import java.util.List;

/**
 * Native protocol for opening a secret value to a single party.
 */
public class MdsonicOutputSinglePartyProtocol<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>>
    extends MdsonicNativeProtocol<OInt, PlainT, SecretP>
    implements RequiresMacCheck {

  private final boolean useMaskedEvaluation;
  private final DRes<SInt> share;
  private final int outputParty;
  private PlainT opened;
  private MdsonicASIntArithmetic<PlainT> shareOutASS;
  private MdsonicInputMask<PlainT> inputMask;
  private MdsonicASIntArithmetic<PlainT> inMinusMask;

  /**
   * Creates new {@link MdsonicOutputSinglePartyProtocol}.
   *
   * @param share value to open
   * @param outputParty party to open to
   */
  public MdsonicOutputSinglePartyProtocol(DRes<SInt> share, int outputParty) {
    this.share = share;
    this.outputParty = outputParty;
    this.useMaskedEvaluation = false;
  }

  public MdsonicOutputSinglePartyProtocol(DRes<SInt> share, int outputParty, boolean useMaskedEvaluation) {
    this.share = share;
    this.outputParty = outputParty;
    this.useMaskedEvaluation = useMaskedEvaluation;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    OpenedValueStore<MdsonicASIntArithmetic<PlainT>, PlainT> openedValueStore = resourcePool.getOpenedValueStore();
    MdsonicDataSupplier<PlainT, SecretP> supplier = resourcePool.getDataSupplier();
    MdsonicCompUIntFactory<PlainT> factory = resourcePool.getFactory();
    if (round == 0) {
      if (useMaskedEvaluation) {
        // MSS to ASS
        final PlainT macKeyShare = supplier.getSecretSharedKey();
        MdsonicMSIntArithmetic<PlainT> shareOut = factory.toMdsonicMSIntArithmetic(share);
        MdsonicASIntArithmetic<PlainT> maskedSecret = shareOut.getMaskedSecret();
        PlainT opened = shareOut.getOpened();
        this.shareOutASS = maskedSecret.addConstant(opened, macKeyShare, factory.zero(), resourcePool.getMyId() == 1);
      } else {
        this.shareOutASS = factory.toMdsonicASIntArithmetic(share);
      }
      this.inputMask = supplier.getNextInputMask(outputParty);
      inMinusMask = shareOutASS.subtract(this.inputMask.getMaskShare());
      // the mask for the first s bits
      MdsonicASIntArithmetic<PlainT> r = supplier.getNextRandomElementShare();
      // highR = 2^k * r
      MdsonicASIntArithmetic<PlainT> highR = new MdsonicASIntArithmetic<>(r.getShare().shiftLowIntoHigh(), r.getMacShare().shiftLowIntoHigh());
      inMinusMask = inMinusMask.add(highR);
      network.sendToAll(inMinusMask.serializeShare());
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      List<PlainT> shares = resourcePool.getPlainSerializer()
          .deserializeList(network.receiveFromAll());
      PlainT recombined = MdsonicUInt.sum(shares);
      openedValueStore.pushOpenedValue(inMinusMask, recombined);
      if (outputParty == resourcePool.getMyId()) {
        this.opened = recombined.add(inputMask.getOpenValue()).clearHighBits();  // only the last k bits
      }
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public OInt out() {
    return opened;
  }

}
