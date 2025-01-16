package dk.alexandra.fresco.suite.mdml.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlASIntArithmetic;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlMSIntArithmetic;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlUInt;
import dk.alexandra.fresco.suite.mdml.resource.MdmlResourcePool;

import java.util.List;

public class MdmlASSToMSSProtocol<PlainT extends MdmlCompUInt<?, ?, PlainT>> extends
        MdmlNativeProtocol<SInt, PlainT> {

    private DRes<SInt> input;
    MdmlASIntArithmetic<PlainT> maskedSecret;
    private MdmlASIntArithmetic<PlainT> openedSecret;
    private PlainT opened;
    private SInt output;

    public MdmlASSToMSSProtocol(DRes<SInt> input) {
        this.input = input;
    }

    public EvaluationStatus evaluate(int round, MdmlResourcePool<PlainT> resourcePool,
                                                    Network network) {
        OpenedValueStore<MdmlASIntArithmetic<PlainT>, PlainT> openedValueStore = resourcePool.getOpenedValueStore();
        if (round == 0) {
            maskedSecret = resourcePool.getDataSupplier().getNextRandomElementShare();
            openedSecret = resourcePool.getFactory().toMdmlASIntArithmetic(input).add(maskedSecret);
            network.sendToAll(openedSecret.serializeShare());
            return EvaluationStatus.HAS_MORE_ROUNDS;
        } else {
            ByteSerializer<PlainT> serializer = resourcePool.getPlainSerializer();
            List<PlainT> shares = serializer.deserializeList(network.receiveFromAll());
            opened = MdmlUInt.sum(shares);
            openedValueStore.pushOpenedValue(openedSecret, opened);
            output = new MdmlMSIntArithmetic<>(maskedSecret, opened);
            return EvaluationStatus.IS_DONE;
        }
    }

    public SInt out() {
        return output;
    }
}
