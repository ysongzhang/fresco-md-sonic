package dk.alexandra.fresco.suite.mdsonic.datatypes;

import cc.redberry.rings.poly.univar.UnivariatePolynomialArithmetic;
import cc.redberry.rings.poly.univar.UnivariatePolynomialZp64;
import dk.alexandra.fresco.framework.util.ByteAndBitConverter;
import dk.alexandra.fresco.framework.util.ByteArrayHelper;
import dk.alexandra.fresco.framework.util.StrictBitVector;

import java.util.List;

public class MdsonicGFFactory64 implements MdsonicGFFactory<MdsonicGF64> {

    @Override
    public MdsonicGF64 zero() {
        return new MdsonicGF64(64);
    }

    @Override
    public MdsonicGF64 createFromBytes(byte[] bytes) {
        return new MdsonicGF64(bytes);
    }

    @Override
    public MdsonicGF64 createFromStrictVector(StrictBitVector value) {
        return new MdsonicGF64(value);
    }

    @Override
    public int getBitLength() {
        return 64;
    }

    @Override
    public MdsonicGF64 innerProduct(List<MdsonicGF64> left, List<MdsonicGF64> right) {
        if (left.size() != right.size()) {
            throw new IllegalArgumentException("The vectors are not of equal length");
        }
        long resultLow = 0;
        long resultHigh = 0;
//        System.out.println("time4: " + System.currentTimeMillis());
        for (int i = 0; i < left.size(); i++) {
            long leftLong = ByteAndBitConverter.toLong(left.get(i).getBits().toByteArray(), 0, 8);
            long rightLong = ByteAndBitConverter.toLong(right.get(i).getBits().toByteArray(), 0, 8);
            long leftLow = leftLong & 0xffffffffL;
            long leftHigh = (leftLong >>> 32) & 0xffffffffL;
            long rightLow = rightLong & 0xffffffffL;
            long rightHigh = (rightLong >>> 32) & 0xffffffffL;
            long res1 = implMul64(leftLow, rightLow);
            long res2 = implMul64(leftLow, rightHigh);
            long res3 = implMul64(leftHigh, rightLow);
            long res4 = implMul64(leftHigh, rightHigh);
            // 拼接
            long low = res1 ^ (res2 << 32) ^ (res3 << 32);
            long high = res4 ^ ((res2 >>> 32) & 0xffffffffL) ^ ((res3 >>> 32) & 0xffffffffL);
            resultLow ^= low;
            resultHigh ^= high;
        }
        byte[] resByte = ByteAndBitConverter.toByteArray(new long[]{resultLow, resultHigh});

        // new method: directly
//        System.out.println("time4: " + System.currentTimeMillis());
        UnivariatePolynomialZp64 result = ByteArrayHelper.bigEndianByteArrayToGf2x(resByte);
        result = UnivariatePolynomialArithmetic.polyMod(result, GF2K_POLYNOMIAL_64, false);
//        System.out.println("time4: " + System.currentTimeMillis());


//        // old method
//        String res = ByteArrayHelper.BooleanArrayToGFString(ByteArrayHelper.getBits(resByte));
//        UnivariatePolynomialZp64 result = module64.parse(res);

        boolean[] r = ByteArrayHelper.GFStringToBooleanArray(result.toString(), getBitLength());
        StrictBitVector temp = new StrictBitVector(getBitLength());
        temp.setBits(r);
        return new MdsonicGF64(temp);
    }
}
