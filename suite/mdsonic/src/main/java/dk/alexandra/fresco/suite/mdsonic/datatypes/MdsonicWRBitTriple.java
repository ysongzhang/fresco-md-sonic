package dk.alexandra.fresco.suite.mdsonic.datatypes;

public class MdsonicWRBitTriple<SecretP extends MdsonicGF<SecretP>, SBoolT extends MdsonicASBool<SecretP>> {
    private final SBoolT left;
    private final SBoolT right1;
    private final SBoolT right2;
    private final SBoolT product1;
    private final SBoolT product2;

    public MdsonicWRBitTriple(SBoolT left, SBoolT right1, SBoolT right2, SBoolT product1, SBoolT product2) {
        this.left = left;
        this.right1 = right1;
        this.product1 = product1;
        this.right2 = right2;
        this.product2 = product2;
    }

    public SBoolT getLeft() {
        return left;
    }

    public SBoolT getRight1() {
        return right1;
    }

    public SBoolT getRight2() {
        return right2;
    }

    public SBoolT getProduct1() {
        return product1;
    }

    public SBoolT getProduct2() {
        return product2;
    }

    @Override
    public String toString() {
        return "MdsonicBitTriple{" +
                "left=" + left +
                ", right1=" + right1 +
                ", right2=" + right2 +
                ", product1=" + product1 +
                ", product2=" + product2 +
                '}';
    }
}
