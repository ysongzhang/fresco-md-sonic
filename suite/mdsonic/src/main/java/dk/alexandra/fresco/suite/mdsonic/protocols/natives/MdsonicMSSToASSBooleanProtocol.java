package dk.alexandra.fresco.suite.mdsonic.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;

public class MdsonicMSSToASSBooleanProtocol<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>> extends
        MdsonicNativeProtocol<SBool, PlainT, SecretP> {

    private DRes<SBool> input;
    private SBool output;

    public MdsonicMSSToASSBooleanProtocol(DRes<SBool> input) {
        this.input = input;
    }

    public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                    Network network) {
        SecretP macKeyShareBoolean = resourcePool.getDataSupplier().getSecretSharedKeyBoolean();
        MdsonicGFFactory<SecretP> factoryBoolean = resourcePool.getBooleanFactory();
        MdsonicMSBoolBoolean<SecretP> inputOut = (MdsonicMSBoolBoolean<SecretP>) input.out();
        MdsonicASBoolBoolean<SecretP> maskedSecret = inputOut.getMaskedSecret();
        boolean opened = inputOut.getOpened();
        this.output = maskedSecret.xorOpen(opened, macKeyShareBoolean, factoryBoolean.zero(), resourcePool.getMyId() == 1);
        return EvaluationStatus.IS_DONE;
    }

    public SBool out() {
        return output;
    }
}
