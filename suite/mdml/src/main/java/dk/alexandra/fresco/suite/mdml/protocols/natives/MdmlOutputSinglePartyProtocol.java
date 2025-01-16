package dk.alexandra.fresco.suite.mdml.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdml.datatypes.*;
import dk.alexandra.fresco.suite.mdml.resource.MdmlResourcePool;
import dk.alexandra.fresco.suite.mdml.resource.storage.MdmlDataSupplier;

import java.util.List;

/**
 * Native protocol for opening a secret value to a single party.
 */
public class MdmlOutputSinglePartyProtocol<PlainT extends MdmlCompUInt<?, ?, PlainT>>
    extends MdmlNativeProtocol<OInt, PlainT>
    implements RequiresMacCheck {

  private final DRes<SInt> share;
  private final int outputParty;
  private PlainT opened;
  private MdmlInputMask<PlainT> inputMask;
  private MdmlASIntArithmetic<PlainT> inMinusMask;

  /**
   * Creates new {@link MdmlOutputSinglePartyProtocol}.
   *
   * @param share value to open
   * @param outputParty party to open to
   */
  public MdmlOutputSinglePartyProtocol(DRes<SInt> share, int outputParty) {
    this.share = share;
    this.outputParty = outputParty;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdmlResourcePool<PlainT> resourcePool,
      Network network) {
    OpenedValueStore<MdmlASIntArithmetic<PlainT>, PlainT> openedValueStore = resourcePool.getOpenedValueStore();
    MdmlDataSupplier<PlainT> supplier = resourcePool.getDataSupplier();
    MdmlCompUIntFactory<PlainT> factory = resourcePool.getFactory();
    if (round == 0) {
      // MSS to ASS
      final PlainT macKeyShare = supplier.getSecretSharedKey();
      MdmlMSIntArithmetic<PlainT> shareOut = factory.toMdmlMSIntArithmetic(share);
      MdmlASIntArithmetic<PlainT> maskedSecret = shareOut.getMaskedSecret();
      PlainT opened = shareOut.getOpened();
      MdmlASIntArithmetic<PlainT> openedSInt = new MdmlASIntArithmetic<>(opened, macKeyShare, factory.zero(), resourcePool.getMyId() == 1);
      MdmlASIntArithmetic<PlainT> shareOutASS = openedSInt.subtract(maskedSecret);

      // Mask the first s bits
      this.inputMask = supplier.getNextInputMask(outputParty);
      inMinusMask = shareOutASS.subtract(this.inputMask.getMaskShare());
      MdmlASIntArithmetic<PlainT> r = supplier.getNextRandomElementShare();
      // highR = 2^k * r
      MdmlASIntArithmetic<PlainT> highR = new MdmlASIntArithmetic<>(r.getShare().shiftLowIntoHigh(), r.getMacShare().shiftLowIntoHigh());
      inMinusMask = inMinusMask.add(highR);
      network.sendToAll(inMinusMask.serializeShare());
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      List<PlainT> shares = resourcePool.getPlainSerializer()
              .deserializeList(network.receiveFromAll());
      PlainT recombined = MdmlUInt.sum(shares);
      openedValueStore.pushOpenedValue(inMinusMask, recombined);
      if (outputParty == resourcePool.getMyId()) {
        this.opened = recombined.add(inputMask.getOpenValue()).clearHighBits();
      }
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public OInt out() {
    return opened;
  }

}
