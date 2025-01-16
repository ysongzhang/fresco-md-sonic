package dk.alexandra.fresco.suite.mdsonic.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.BooleanSerializer;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;
import dk.alexandra.fresco.suite.mdsonic.util.BoolUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Native protocol for converting boolean shares to arithmetic shares.
 */
public class MdsonicMaskOpenProtocol<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>> extends
        MdsonicNativeProtocol<Boolean, PlainT, SecretP> {

  private final DRes<SBool> bool;
  private final DRes<SBool> mask;
  private MdsonicASBoolBoolean<SecretP> openedShare;
  private Boolean c;

  public MdsonicMaskOpenProtocol(DRes<SBool> bool, DRes<SBool> mask) {
    this.bool = bool;
    this.mask = mask;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    if (round == 0) {

      MdsonicASBoolBoolean<SecretP> maskBoolean = (MdsonicASBoolBoolean<SecretP>) mask.out();
      MdsonicASBoolBoolean<SecretP> inputBoolean = (MdsonicASBoolBoolean<SecretP>) bool.out();
      openedShare = inputBoolean.xor(maskBoolean);
      network.sendToAll(new byte[]{BooleanSerializer.toBytes(openedShare.getShare())});
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      List<byte[]> buffers = network.receiveFromAll();
      List<Boolean> shares = new ArrayList<>();
      for (byte[] buffer : buffers) {
        shares.add(BooleanSerializer.fromBytes(buffer[0]));
      }
      boolean opened = BoolUtils.xor(shares);

      // 加入check channel
      OpenedValueStore<MdsonicASBoolBoolean<SecretP>, Boolean> openedBooleanValueStore = resourcePool.getOpenedBooleanValueStore();
      openedBooleanValueStore.pushOpenedValue(openedShare, opened);

      c = opened;
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public Boolean out() {
    return c;
  }

}
