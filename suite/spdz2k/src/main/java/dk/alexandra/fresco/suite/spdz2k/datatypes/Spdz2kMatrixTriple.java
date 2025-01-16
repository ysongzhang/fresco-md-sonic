package dk.alexandra.fresco.suite.spdz2k.datatypes;

import dk.alexandra.fresco.lib.collections.Matrix;

public class Spdz2kMatrixTriple<PlainT extends CompUInt<?, ?, PlainT>, SIntT extends Spdz2kSInt<PlainT>> {

    private final Matrix<SIntT> left;
    private final Matrix<SIntT> right;
    private final Matrix<SIntT> product;

    public Spdz2kMatrixTriple(Matrix<SIntT> left, Matrix<SIntT> right, Matrix<SIntT> product) {
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
        return "DomainTriple{" +
                "left=" + left +
                ", right=" + right +
                ", product=" + product +
                '}';
    }
}
