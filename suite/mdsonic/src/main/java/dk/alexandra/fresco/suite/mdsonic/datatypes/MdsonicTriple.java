package dk.alexandra.fresco.suite.mdsonic.datatypes;

public class MdsonicTriple<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SIntT extends MdsonicASInt<PlainT>> {

    private final SIntT left;
    private final SIntT right;
    private final SIntT product;

    public MdsonicTriple(SIntT left, SIntT right, SIntT product) {
        this.left = left;
        this.right = right;
        this.product = product;
    }

    public SIntT getLeft() {
        return left;
    }

    public SIntT getRight() {
        return right;
    }

    public SIntT getProduct() {
        return product;
    }

    @Override
    public String toString() {
        return "MdsonicTriple{" +
                "left=" + left +
                ", right=" + right +
                ", product=" + product +
                '}';
    }
}
