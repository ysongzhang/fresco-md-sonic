package dk.alexandra.fresco.suite.mdsonic.protocols.natives.ASS;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.mdsonic.DaBit;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.BooleanSerializer;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;
import dk.alexandra.fresco.suite.mdsonic.protocols.natives.MdsonicNativeProtocol;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;

import java.util.List;

/**
 * Native protocol for converting arithmetic shares to boolean shares.
 */
public class MdsonicArithmeticToBooleanProtocolASS<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>> extends
        MdsonicNativeProtocol<SBool, PlainT, SecretP> {

  private final DRes<SInt> bit;
  private SBool bool;
  private DaBit daBit;
  private MdsonicASIntArithmetic<PlainT> c;

  public MdsonicArithmeticToBooleanProtocolASS(DRes<SInt> bit) {
    this.bit = bit;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    OpenedValueStore<MdsonicASIntArithmetic<PlainT>, PlainT> openedValueStore = resourcePool.getOpenedValueStore();
    SecretP macKeyShareBoolean = resourcePool.getDataSupplier().getSecretSharedKeyBoolean();
    MdsonicGFFactory<SecretP> factoryBoolean = resourcePool.getBooleanFactory();
    if (round == 0) {
      daBit = resourcePool.getDataSupplier().getNextDaBit();
      MdsonicASIntArithmetic<PlainT> daBitArithmetic = (MdsonicASIntArithmetic<PlainT>) daBit.getBitB().out();
      MdsonicASIntArithmetic<PlainT> input = (MdsonicASIntArithmetic<PlainT>) bit.out();
      c = input.add(daBitArithmetic);
      network.sendToAll(c.serializeShare());
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      ByteSerializer<PlainT> serializer = resourcePool.getPlainSerializer();
      List<PlainT> shares = serializer.deserializeList(network.receiveFromAll());
      PlainT recombined = MdsonicUInt.sum(shares);
      openedValueStore.pushOpenedValue(c, recombined);

      boolean opened = BooleanSerializer.fromBytes((byte) recombined.bitValue());
      MdsonicASBoolBoolean<SecretP> daBitBoolean = (MdsonicASBoolBoolean<SecretP>) daBit.getBitA().out();
      boolean isPartyOne = resourcePool.getMyId() == 1;
      bool = daBitBoolean.xorOpen(opened, macKeyShareBoolean, factoryBoolean.zero(), isPartyOne);
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public SBool out() {
    return bool;
  }

}
