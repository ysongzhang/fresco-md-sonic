package dk.alexandra.fresco.suite.mdml.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUIntFactory;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlASIntArithmetic;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlMSIntArithmetic;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlUInt;
import dk.alexandra.fresco.suite.mdml.resource.MdmlResourcePool;
import dk.alexandra.fresco.suite.mdml.resource.storage.MdmlDataSupplier;

import java.util.List;

/**
 * Native protocol for opening a secret value to all parties.
 */
public class MdmlOutputToAll<PlainT extends MdmlCompUInt<?, ?, PlainT>>
    extends MdmlNativeProtocol<OInt, PlainT>
    implements RequiresMacCheck {

  private final DRes<SInt> share;
  private PlainT opened;
  private MdmlASIntArithmetic<PlainT> authenticatedElement;

  /**
   * Creates new {@link MdmlOutputToAll}.
   *
   * @param share value to open
   */
  public MdmlOutputToAll(DRes<SInt> share) {
    this.share = share;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdmlResourcePool<PlainT> resourcePool,
      Network network) {
    OpenedValueStore<MdmlASIntArithmetic<PlainT>, PlainT> openedValueStore = resourcePool.getOpenedValueStore();
    MdmlCompUIntFactory<PlainT> factory = resourcePool.getFactory();
    MdmlDataSupplier<PlainT> supplier = resourcePool.getDataSupplier();
    if (round == 0) {
      final PlainT macKeyShare = supplier.getSecretSharedKey();
      MdmlMSIntArithmetic<PlainT> shareOut = factory.toMdmlMSIntArithmetic(share);
      MdmlASIntArithmetic<PlainT> maskedSecret = shareOut.getMaskedSecret();
      PlainT opened = shareOut.getOpened();
      MdmlASIntArithmetic<PlainT> openedSInt = new MdmlASIntArithmetic<>(opened, macKeyShare, factory.zero(), resourcePool.getMyId() == 1);
      authenticatedElement = openedSInt.subtract(maskedSecret);

      MdmlASIntArithmetic<PlainT> r = supplier.getNextRandomElementShare();
      MdmlASIntArithmetic<PlainT> highR = new MdmlASIntArithmetic<>(r.getShare().shiftLowIntoHigh(), r.getMacShare().shiftLowIntoHigh());
      authenticatedElement = authenticatedElement.add(highR);
      network.sendToAll(authenticatedElement.serializeShare());
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      ByteSerializer<PlainT> serializer = resourcePool.getPlainSerializer();
      List<PlainT> shares = serializer.deserializeList(network.receiveFromAll());
      PlainT recombined = MdmlUInt.sum(shares);
      openedValueStore.pushOpenedValue(authenticatedElement, recombined);
      this.opened = recombined.clearHighBits();
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public OInt out() {
    return opened;
  }

}
