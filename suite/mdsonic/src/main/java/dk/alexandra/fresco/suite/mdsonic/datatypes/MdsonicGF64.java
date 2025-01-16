package dk.alexandra.fresco.suite.mdsonic.datatypes;

import cc.redberry.rings.poly.FiniteField;
import cc.redberry.rings.poly.univar.UnivariatePolynomialZp64;
import dk.alexandra.fresco.framework.util.ByteArrayHelper;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.framework.value.OInt;

public class MdsonicGF64 implements MdsonicGF<MdsonicGF64> {
    private final StrictBitVector value;

    public MdsonicGF64(int size) {
        this.value = new StrictBitVector(size);  // all zero
    }

    public MdsonicGF64(byte[] bits) {
        this.value = new StrictBitVector(bits);
    }

    public MdsonicGF64(StrictBitVector bits) {
        this.value = bits;
    }

    @Override
    public MdsonicGF64 add(MdsonicGF64 other) {
        StrictBitVector temp = new StrictBitVector(value.toByteArray().clone());
        temp.xor(other.value);
        return new MdsonicGF64(temp);
    }

    @Override
    public MdsonicGF64 multiply(MdsonicGF64 other) {
        FiniteField<UnivariatePolynomialZp64> module = MdsonicGFFactory.module64;
        UnivariatePolynomialZp64 a = module.parse(ByteArrayHelper.BooleanArrayToGFString(value.getBits()));
        UnivariatePolynomialZp64 b = module.parse(ByteArrayHelper.BooleanArrayToGFString(other.value.getBits()));
//        System.out.println(a.multiply(b).toString());
//        System.out.println(value.getSize());
//        System.out.println(module);
        boolean[] c = ByteArrayHelper.GFStringToBooleanArray(module.multiply(a, b).toString(), value.getSize());
        StrictBitVector temp = new StrictBitVector(value.getSize());
        temp.setBits(c);
        return new MdsonicGF64(temp);
    }

    @Override
    public MdsonicGF64 multiply(boolean bit) {
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
    public MdsonicGF64 getClone() {
        StrictBitVector temp = new StrictBitVector(value.toByteArray().clone());
        return new MdsonicGF64(temp);
    }

    @Override
    public MdsonicGF64 getZero() {
        return new MdsonicGF64(getBitLength());
    }

    @Override
    public int getBitLength() {
        return value.getSize();
    }

    @Override
    public int getSecretParameter() {
        return 64;
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
