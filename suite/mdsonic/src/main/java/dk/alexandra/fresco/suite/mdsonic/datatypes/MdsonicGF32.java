package dk.alexandra.fresco.suite.mdsonic.datatypes;

import cc.redberry.rings.poly.FiniteField;
import cc.redberry.rings.poly.univar.UnivariatePolynomialZp64;
import dk.alexandra.fresco.framework.util.ByteArrayHelper;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.framework.value.OInt;

public class MdsonicGF32 implements MdsonicGF<MdsonicGF32> {
    private final StrictBitVector value;

    public MdsonicGF32(int size) {
        this.value = new StrictBitVector(size);  // all zero
    }

    public MdsonicGF32(byte[] bits) {
        this.value = new StrictBitVector(bits);
    }

    public MdsonicGF32(StrictBitVector bits) {
        this.value = bits;
    }

    @Override
    public MdsonicGF32 add(MdsonicGF32 other) {
        StrictBitVector temp = new StrictBitVector(value.toByteArray().clone());
        temp.xor(other.value);
        return new MdsonicGF32(temp);
    }

    @Override
    public MdsonicGF32 multiply(MdsonicGF32 other) {
        FiniteField<UnivariatePolynomialZp64> module = MdsonicGFFactory.module32;
        UnivariatePolynomialZp64 a = module.parse(ByteArrayHelper.BooleanArrayToGFString(value.getBits()));
        UnivariatePolynomialZp64 b = module.parse(ByteArrayHelper.BooleanArrayToGFString(other.value.getBits()));
        boolean[] c = ByteArrayHelper.GFStringToBooleanArray(module.multiply(a, b).toString(), value.getSize());
        StrictBitVector temp = new StrictBitVector(value.getSize());
        temp.setBits(c);
        return new MdsonicGF32(temp);
    }

    @Override
    public MdsonicGF32 multiply(boolean bit) {
        if (bit) {
            return this.getClone();
        } else {
            return this.getZero();
        }
    }

    @Override
    public boolean isZero() {
        boolean flag = false;
        for (boolean bit : this.value.getBits()) {
            flag = flag | bit;
        }
        return !flag;
    }

    @Override
    public StrictBitVector getBits() {
        return value;
    }

    @Override
    public MdsonicGF32 getClone() {
        StrictBitVector temp = new StrictBitVector(value.toByteArray().clone());
        return new MdsonicGF32(temp);
    }

    @Override
    public MdsonicGF32 getZero() {
        return new MdsonicGF32(getBitLength());
    }

    @Override
    public int getBitLength() {
        return value.getSize();
    }

    @Override
    public int getSecretParameter() {
        return 32;
    }

    @Override
    public String toString() {
        return value.asBinaryString();
    }

    @Override
    public OInt out() {
        return this;
    }
}
