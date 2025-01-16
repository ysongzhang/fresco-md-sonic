package dk.alexandra.fresco.suite.mdsonic.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;

public class MdsonicMSSToASSArithmeticProtocol<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>> extends
        MdsonicNativeProtocol<SInt, PlainT, SecretP> {

    private DRes<SInt> input;
    private SInt output;

    public MdsonicMSSToASSArithmeticProtocol(DRes<SInt> input) {
        this.input = input;
    }

    public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                    Network network) {
        final PlainT macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
        MdsonicCompUIntFactory<PlainT> factory = resourcePool.getFactory();
        MdsonicMSIntArithmetic<PlainT> inputOut = factory.toMdsonicMSIntArithmetic(input);
        MdsonicASIntArithmetic<PlainT> maskedSecret = inputOut.getMaskedSecret();
        PlainT opened = inputOut.getOpened();
        this.output = maskedSecret.addConstant(opened, macKeyShare, factory.zero(), resourcePool.getMyId() == 1);
        return EvaluationStatus.IS_DONE;
    }

    public SInt out() {
        return output;
    }
}
