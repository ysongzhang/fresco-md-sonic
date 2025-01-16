package dk.alexandra.fresco.suite.mdsonic.datatypes;

public class MdsonicBitTriple<SecretP extends MdsonicGF<SecretP>, SBoolT extends MdsonicASBool<SecretP>> {
    private final SBoolT left;
    private final SBoolT right;
    private final SBoolT product;

    public MdsonicBitTriple(SBoolT left, SBoolT right, SBoolT product) {
        this.left = left;
        this.right = right;
        this.product = product;
    }

    public SBoolT getLeft() {
        return left;
    }

    public SBoolT getRight() {
        return right;
    }

    public SBoolT getProduct() {
        return product;
    }

    @Override
    public String toString() {
        return "DomainBitTriple{" +
                "left=" + left +
                ", right=" + right +
                ", product=" + product +
                '}';
    }
}
