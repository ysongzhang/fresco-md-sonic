package dk.alexandra.fresco.suite.mdsonic.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;
import dk.alexandra.fresco.suite.mdsonic.resource.storage.MdsonicDataSupplier;

import java.util.List;

/**
 * Native protocol for opening a secret value to all parties.
 */
public class MdsonicOutputToAll<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>>
    extends MdsonicNativeProtocol<OInt, PlainT, SecretP>
    implements RequiresMacCheck {

  private final boolean useMaskedEvaluation;
  private final DRes<SInt> share;
  private PlainT opened;
  private MdsonicASIntArithmetic<PlainT> authenticatedElement;

  /**
   * Creates new {@link MdsonicOutputToAll}.
   *
   * @param share value to open
   */
  public MdsonicOutputToAll(DRes<SInt> share) {
    this.share = share;
    this.useMaskedEvaluation = false;
  }

  public MdsonicOutputToAll(DRes<SInt> share, boolean useMaskedEvaluation) {
    this.share = share;
    this.useMaskedEvaluation = useMaskedEvaluation;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    OpenedValueStore<MdsonicASIntArithmetic<PlainT>, PlainT> openedValueStore = resourcePool.getOpenedValueStore();
    MdsonicCompUIntFactory<PlainT> factory = resourcePool.getFactory();
    MdsonicDataSupplier<PlainT, SecretP> supplier = resourcePool.getDataSupplier();
    if (round == 0) {
      if (useMaskedEvaluation) {
        // MSS to ASS
        final PlainT macKeyShare = supplier.getSecretSharedKey();
        MdsonicMSIntArithmetic<PlainT> shareOut = factory.toMdsonicMSIntArithmetic(share);
        MdsonicASIntArithmetic<PlainT> maskedSecret = shareOut.getMaskedSecret();
        PlainT opened = shareOut.getOpened();
        authenticatedElement = maskedSecret.addConstant(opened, macKeyShare, factory.zero(), resourcePool.getMyId() == 1);
      } else {
        authenticatedElement = factory.toMdsonicASIntArithmetic(share);
      }
      MdsonicASIntArithmetic<PlainT> r = supplier.getNextRandomElementShare();
      MdsonicASIntArithmetic<PlainT> highR = new MdsonicASIntArithmetic<>(r.getShare().shiftLowIntoHigh(), r.getMacShare().shiftLowIntoHigh());
      authenticatedElement = authenticatedElement.add(highR);
      network.sendToAll(authenticatedElement.serializeShare());
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      ByteSerializer<PlainT> serializer = resourcePool.getPlainSerializer();
      List<PlainT> shares = serializer.deserializeList(network.receiveFromAll());
      PlainT recombined = MdsonicUInt.sum(shares);
      openedValueStore.pushOpenedValue(authenticatedElement, recombined);
      this.opened = recombined.clearHighBits();  // only the last k bits
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public OInt out() {
    return opened;
  }

}
