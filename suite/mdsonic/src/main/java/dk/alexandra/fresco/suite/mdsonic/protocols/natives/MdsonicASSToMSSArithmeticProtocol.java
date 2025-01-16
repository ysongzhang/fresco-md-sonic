package dk.alexandra.fresco.suite.mdsonic.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;

import java.util.List;

public class MdsonicASSToMSSArithmeticProtocol<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>> extends
        MdsonicNativeProtocol<SInt, PlainT, SecretP> {

    private DRes<SInt> input;
    private MdsonicASIntArithmetic<PlainT> maskedSecret;
    private MdsonicASIntArithmetic<PlainT> openedSecret;
    private PlainT opened;
    private SInt output;

    public MdsonicASSToMSSArithmeticProtocol(DRes<SInt> input) {
        this.input = input;
        this.maskedSecret = null;
    }

    public MdsonicASSToMSSArithmeticProtocol(DRes<SInt> input, DRes<SInt> maskedSecret) {
        this.input = input;
        this.maskedSecret = (MdsonicASIntArithmetic<PlainT>) maskedSecret.out();
    }

    public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                    Network network) {
        OpenedValueStore<MdsonicASIntArithmetic<PlainT>, PlainT> openedValueStore = resourcePool.getOpenedValueStore();
        if (round == 0) {
            if (maskedSecret == null) {
                maskedSecret = resourcePool.getDataSupplier().getNextRandomElementShare();
            }
            openedSecret = resourcePool.getFactory().toMdsonicASIntArithmetic(input).subtract(maskedSecret);
            network.sendToAll(openedSecret.serializeShare());
            return EvaluationStatus.HAS_MORE_ROUNDS;
        } else {
            ByteSerializer<PlainT> serializer = resourcePool.getPlainSerializer();
            List<PlainT> shares = serializer.deserializeList(network.receiveFromAll());
            opened = MdsonicUInt.sum(shares);
            openedValueStore.pushOpenedValue(openedSecret, opened);
            output = new MdsonicMSIntArithmetic<>(maskedSecret, opened);
            return EvaluationStatus.IS_DONE;
        }
    }

    public SInt out() {
        return output;
    }
}
