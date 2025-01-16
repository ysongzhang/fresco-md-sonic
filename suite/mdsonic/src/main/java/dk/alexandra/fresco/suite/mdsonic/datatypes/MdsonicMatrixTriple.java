package dk.alexandra.fresco.suite.mdsonic.datatypes;

import dk.alexandra.fresco.lib.collections.Matrix;

public class MdsonicMatrixTriple<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SIntT extends MdsonicASInt<PlainT>> {  // 这里的SIntT表示算术分享下的ASS

    private final Matrix<SIntT> left;
    private final Matrix<SIntT> right;
    private final Matrix<SIntT> product;

    public MdsonicMatrixTriple(Matrix<SIntT> left, Matrix<SIntT> right, Matrix<SIntT> product) {
        if (left.getWidth() != right.getHeight()) {
            throw new IllegalArgumentException(
                    "Matrice sizes does not match - " + left.getWidth() + " != " + right.getHeight());
        }
        if (left.getHeight() != product.getHeight()) {
            throw new IllegalArgumentException(
                    "Matrice sizes does not match - " + left.getHeight() + " != " + product.getHeight());
        }
        if (right.getWidth() != product.getWidth()) {
            throw new IllegalArgumentException(
                    "Matrice sizes does not match - " + right.getWidth() + " != " + product.getWidth());
        }
        this.left = left;
        this.right = right;
        this.product = product;
    }

    public Matrix<SIntT> getLeft() {
        return left;
    }

    public Matrix<SIntT> getRight() {
        return right;
    }

    public Matrix<SIntT> getProduct() {
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
