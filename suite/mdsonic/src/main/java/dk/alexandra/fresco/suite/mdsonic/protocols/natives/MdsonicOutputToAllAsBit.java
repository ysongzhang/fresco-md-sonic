package dk.alexandra.fresco.suite.mdsonic.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.BooleanSerializer;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;
import dk.alexandra.fresco.suite.mdsonic.resource.storage.MdsonicDataSupplier;
import dk.alexandra.fresco.suite.mdsonic.util.BoolUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Native protocol for opening a secret value to all parties.
 */
public class MdsonicOutputToAllAsBit<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>>
    extends MdsonicNativeProtocol<Boolean, PlainT, SecretP>
    implements RequiresMacCheck {

  private final boolean useMaskedEvaluation;
  private final DRes<SBool> share;
  private Boolean opened;
  private MdsonicASBoolBoolean<SecretP> authenticatedBit;

  /**
   * Creates new {@link MdsonicOutputToAllAsBit}.
   *
   * @param share value to open
   */
  public MdsonicOutputToAllAsBit(DRes<SBool> share) {
    this.share = share;
    this.useMaskedEvaluation = false;
  }

  public MdsonicOutputToAllAsBit(DRes<SBool> share, boolean useMaskedEvaluation) {
    this.share = share;
    this.useMaskedEvaluation = useMaskedEvaluation;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    MdsonicCompUIntFactory<PlainT> factory = resourcePool.getFactory();
    MdsonicDataSupplier<PlainT, SecretP> supplier = resourcePool.getDataSupplier();
    if (round == 0) {
      if (useMaskedEvaluation) {
        SecretP macKeyShareBoolean = resourcePool.getDataSupplier().getSecretSharedKeyBoolean();
        MdsonicGFFactory<SecretP> factoryBoolean = resourcePool.getBooleanFactory();
        // MSS to ASS
        MdsonicMSBoolBoolean<SecretP> shareOut = (MdsonicMSBoolBoolean<SecretP>) share.out();
        MdsonicASBoolBoolean<SecretP> maskedSecret = shareOut.getMaskedSecret();
        boolean opened = shareOut.getOpened();
        authenticatedBit = maskedSecret.xorOpen(opened, macKeyShareBoolean, factoryBoolean.zero(), resourcePool.getMyId() == 1);
      } else {
        authenticatedBit = (MdsonicASBoolBoolean<SecretP>) share.out();
      }
      network.sendToAll(new byte[]{BooleanSerializer.toBytes(authenticatedBit.getShare())});
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      List<byte[]> buffers = network.receiveFromAll();
      List<Boolean> shares = new ArrayList<>();
      for (byte[] buffer : buffers) {
        shares.add(BooleanSerializer.fromBytes(buffer[0]));
      }
      opened = BoolUtils.xor(shares);

      OpenedValueStore<MdsonicASBoolBoolean<SecretP>, Boolean> openedBooleanValueStore = resourcePool.getOpenedBooleanValueStore();
      openedBooleanValueStore.pushOpenedValue(authenticatedBit, opened);

      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public Boolean out() {
    return opened;
  }

}
