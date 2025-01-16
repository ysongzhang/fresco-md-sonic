package dk.alexandra.fresco.suite.mdsonic.datatypes;

import dk.alexandra.fresco.framework.value.SBool;

public abstract class MdsonicASBool<SecretP extends MdsonicGF<SecretP>> implements SBool {  // The base class of ASS over F2
    protected final boolean share;
    protected final SecretP macShare;

    public MdsonicASBool(boolean share, SecretP macShare) {
        this.share = share;
        this.macShare = macShare;
    }

    public boolean getShare() {
        return share;
    }

    public SecretP getMacShare() {
        return macShare;
    }

    public String toString() {
        return "DomainASBool[share=" + share + ", macShare=" + macShare + "]";
    }

    @Override
    public SBool out() {
        return this;
    }
}
