package dk.alexandra.fresco.suite.mdml.datatypes;

public class MdmlTriple<PlainT extends MdmlCompUInt<?, ?, PlainT>, SIntT extends MdmlSInt<PlainT>> {

  private final SIntT left;
  private final SIntT right;
  private final SIntT product;

  public MdmlTriple(SIntT left, SIntT right, SIntT product) {
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
    return "Spdz2kTriple{" +
        "left=" + left +
        ", right=" + right +
        ", product=" + product +
        '}';
  }
}
