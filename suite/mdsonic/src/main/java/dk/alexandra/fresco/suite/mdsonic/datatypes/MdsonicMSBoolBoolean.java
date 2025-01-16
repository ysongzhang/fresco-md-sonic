package dk.alexandra.fresco.suite.mdsonic.datatypes;

import dk.alexandra.fresco.framework.value.SBool;

public class MdsonicMSBoolBoolean<SecretP extends MdsonicGF<SecretP>> implements SBool {  // The class of MSS over F2: value = maskedSecret + opened
    protected final MdsonicASBoolBoolean<SecretP> maskedSecret;
    protected final boolean opened;

    public MdsonicMSBoolBoolean(MdsonicASBoolBoolean<SecretP> maskedSecret, boolean opened) {
        this.maskedSecret = maskedSecret;
        this.opened = opened;
    }

    /**
     * Return share.
     */
    public boolean getOpened() {
        return opened;
    }

    /**
     * Return mac share.
     */
    public MdsonicASBoolBoolean<SecretP> getMaskedSecret() {
        return maskedSecret;
    }

    public SBool out() {
        return this;
    }

    public MdsonicMSBoolBoolean<SecretP> xor(MdsonicMSBoolBoolean<SecretP> other) {
        return new MdsonicMSBoolBoolean<>(maskedSecret.xor(other.getMaskedSecret()), opened ^ other.getOpened());
    }

    public MdsonicMSBoolBoolean<SecretP> and(boolean other) {
        return new MdsonicMSBoolBoolean<>(maskedSecret.and(other), opened & other);
    }

    public MdsonicMSBoolBoolean<SecretP> xorOpen(boolean other) {
        return new MdsonicMSBoolBoolean<>(maskedSecret, opened ^ other);
    }

    public String toString() {
        return "MdsonicMSBoolBoolean{" +
                "opened=" + opened +
                ", maskedShare=" + maskedSecret.getShare() +
                '}';
    }
}
