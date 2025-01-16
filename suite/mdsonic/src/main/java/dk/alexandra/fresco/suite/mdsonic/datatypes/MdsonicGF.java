package dk.alexandra.fresco.suite.mdsonic.datatypes;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.framework.value.OInt;

import java.util.List;

public interface MdsonicGF<T extends MdsonicGF> extends OInt {
    T add(T other);

    T multiply(T other);

    T multiply(boolean bit);

    int getBitLength();

    int getSecretParameter();

    StrictBitVector getBits();

    T getClone();

    T getZero();

    boolean isZero();

    /**
     * Compute sum of elements.
     */
    static <S extends MdsonicGF<S>> S sum(List<S> elements) {
        return elements.stream().reduce(MdsonicGF::add).orElse(elements.get(0));
    }

    static <S extends MdsonicGF<S>> S innerProductWithBool(List<S> left, List<Boolean> right) {
        S accumulator = left.get(0).multiply(right.get(0));
        for (int i = 1; i < left.size(); i++) {
            accumulator = accumulator.add(left.get(i).multiply(right.get(i)));
        }
        return accumulator;
    }
}
