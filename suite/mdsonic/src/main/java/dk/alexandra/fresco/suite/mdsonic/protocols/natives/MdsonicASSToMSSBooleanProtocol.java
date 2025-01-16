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

public class MdsonicASSToMSSBooleanProtocol<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>> extends
        MdsonicNativeProtocol<SBool, PlainT, SecretP> {

    private DRes<SBool> input;
    private MdsonicASBoolBoolean<SecretP> maskedSecret;
    private MdsonicASBoolBoolean<SecretP> openedSecret;
    private boolean opened;
    private SBool output;

    public MdsonicASSToMSSBooleanProtocol(DRes<SBool> input) {
        this.input = input;
        this.maskedSecret = null;
    }

    public MdsonicASSToMSSBooleanProtocol(DRes<SBool> input, DRes<SBool> maskedSecret) {
        this.input = input;
        this.maskedSecret = (MdsonicASBoolBoolean<SecretP>) maskedSecret.out();
    }

    public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                    Network network) {
        if (round == 0) {
            if (maskedSecret == null) {
                maskedSecret = resourcePool.getDataSupplier().getNextBitShare();
            }
            MdsonicASBoolBoolean<SecretP> inputOut = (MdsonicASBoolBoolean<SecretP>) input.out();
            openedSecret = inputOut.xor(maskedSecret);
            network.sendToAll(new byte[]{BooleanSerializer.toBytes(openedSecret.getShare())});
            return EvaluationStatus.HAS_MORE_ROUNDS;
        } else {
            List<byte[]> buffers = network.receiveFromAll();
            List<Boolean> shares = new ArrayList<>();
            for (byte[] buffer : buffers) {
                shares.add(BooleanSerializer.fromBytes(buffer[0]));
            }
            opened = BoolUtils.xor(shares);

            OpenedValueStore<MdsonicASBoolBoolean<SecretP>, Boolean> openedBooleanValueStore = resourcePool.getOpenedBooleanValueStore();
            openedBooleanValueStore.pushOpenedValue(openedSecret, opened);

            output = new MdsonicMSBoolBoolean<>(maskedSecret, opened);
            return EvaluationStatus.IS_DONE;
        }
    }

    public SBool out() {
        return output;
    }
}
