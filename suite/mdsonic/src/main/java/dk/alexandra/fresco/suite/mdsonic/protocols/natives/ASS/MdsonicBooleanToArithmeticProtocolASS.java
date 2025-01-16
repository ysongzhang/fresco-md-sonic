package dk.alexandra.fresco.suite.mdsonic.protocols.natives.ASS;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.mdsonic.DaBit;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.BooleanSerializer;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;
import dk.alexandra.fresco.suite.mdsonic.protocols.natives.MdsonicNativeProtocol;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;
import dk.alexandra.fresco.suite.mdsonic.util.BoolUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Native protocol for converting boolean shares to arithmetic shares.
 */
public class MdsonicBooleanToArithmeticProtocolASS<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>> extends
        MdsonicNativeProtocol<SInt, PlainT, SecretP> {

  private final DRes<SBool> bool;
  private SInt arithmetic;
  private DaBit daBit;
  private MdsonicASBoolBoolean<SecretP> c;

  public MdsonicBooleanToArithmeticProtocolASS(DRes<SBool> bool) {
    this.bool = bool;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    MdsonicCompUIntFactory<PlainT> factory = resourcePool.getFactory();
    if (round == 0) {
      daBit = resourcePool.getDataSupplier().getNextDaBit();
      MdsonicASBoolBoolean<SecretP> daBitBoolean = (MdsonicASBoolBoolean<SecretP>) daBit.getBitA().out();
      MdsonicASBoolBoolean<SecretP> inputBoolean = (MdsonicASBoolBoolean<SecretP>) bool.out();
      c = inputBoolean.xor(daBitBoolean);
      network.sendToAll(new byte[]{BooleanSerializer.toBytes(c.getShare())});
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      List<byte[]> buffers = network.receiveFromAll();
      List<Boolean> shares = new ArrayList<>();
      for (byte[] buffer : buffers) {
        shares.add(BooleanSerializer.fromBytes(buffer[0]));
      }
      boolean opened = BoolUtils.xor(shares);

      OpenedValueStore<MdsonicASBoolBoolean<SecretP>, Boolean> openedBooleanValueStore = resourcePool.getOpenedBooleanValueStore();
      openedBooleanValueStore.pushOpenedValue(c, opened);

      PlainT openCBit = opened ? factory.one() : factory.zero();
      MdsonicASIntArithmetic<PlainT> daBitArithmetic = (MdsonicASIntArithmetic<PlainT>) daBit.getBitB().out();
      PlainT macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
      boolean isPartyOne = resourcePool.getMyId() == 1;
      arithmetic = daBitArithmetic.addConstant(openCBit, macKeyShare, factory.zero(),
          isPartyOne).subtract(daBitArithmetic.multiply(factory.two().multiply(openCBit)));
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public SInt out() {
    return arithmetic;
  }

}
