package dk.alexandra.fresco.suite.mdsonic.datatypes;

import cc.redberry.rings.poly.univar.UnivariatePolynomialArithmetic;
import cc.redberry.rings.poly.univar.UnivariatePolynomialZp64;
import dk.alexandra.fresco.framework.util.ByteAndBitConverter;
import dk.alexandra.fresco.framework.util.ByteArrayHelper;
import dk.alexandra.fresco.framework.util.StrictBitVector;

import java.util.List;

public class MdsonicGFFactory32 implements MdsonicGFFactory<MdsonicGF32> {

    @Override
    public MdsonicGF32 zero() {
        return new MdsonicGF32(32);
    }

    @Override
    public MdsonicGF32 createFromBytes(byte[] bytes) {
        return new MdsonicGF32(bytes);
    }

    @Override
    public MdsonicGF32 createFromStrictVector(StrictBitVector value) {
        return new MdsonicGF32(value);
    }

    @Override
    public int getBitLength() {
        return 32;
    }

    @Override
    public MdsonicGF32 innerProduct(List<MdsonicGF32> left, List<MdsonicGF32> right) {
        if (left.size() != right.size()) {
            throw new IllegalArgumentException("The vectors are not of equal length");
        }
        long res = 0;
//        System.out.println("time4: " + System.currentTimeMillis());
        for (int i = 0; i < left.size(); i++) {
            long leftLong = ByteAndBitConverter.toLong(left.get(i).getBits().toByteArray(), 0, 4);
            long rightLong = ByteAndBitConverter.toLong(right.get(i).getBits().toByteArray(), 0, 4);
            res ^= implMul64(leftLong, rightLong);
        }
        byte[] resByte = ByteAndBitConverter.toByteArray(res);

        // new method: directly
//        System.out.println("time4: " + System.currentTimeMillis());
        UnivariatePolynomialZp64 result = ByteArrayHelper.bigEndianByteArrayToGf2x(resByte);
        result = UnivariatePolynomialArithmetic.polyMod(result, GF2K_POLYNOMIAL_32, false);
//        System.out.println("time4: " + System.currentTimeMillis());

//        // old method
//        UnivariatePolynomialZp64 result = module32.parse(ByteArrayHelper.BooleanArrayToGFString(ByteArrayHelper.getBits(resByte)));

        boolean[] r = ByteArrayHelper.GFStringToBooleanArray(result.toString(), getBitLength());
        StrictBitVector temp = new StrictBitVector(getBitLength());
        temp.setBits(r);
        return new MdsonicGF32(temp);
    }
}
