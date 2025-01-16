package dk.alexandra.fresco.suite.mdsonic.datatypes;

import cc.redberry.rings.poly.FiniteField;
import cc.redberry.rings.poly.univar.UnivariatePolynomialZp64;
import dk.alexandra.fresco.framework.util.StrictBitVector;

import java.util.List;

import static cc.redberry.rings.Rings.GF;

public interface MdsonicGFFactory<SecretP extends MdsonicGF<SecretP>> {
    /**
     * module for GF(2^128): f(x) = x^128 + x^7 + x^2 + x + 1
     */
    UnivariatePolynomialZp64 GF2K_POLYNOMIAL_128 = UnivariatePolynomialZp64.create(
            2, new long[] {
                    1L, 1L, 1L, 0L, 0L, 0L, 0L, 1L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,
                    0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,
                    0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,
                    0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,
                    0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,
                    0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,
                    0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,
                    0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,
                    1L,
            }
    );

    /**
     * module for GF(2^64): f(x) = x^64 + x^4 + x^3 + 1
     */
    UnivariatePolynomialZp64 GF2K_POLYNOMIAL_64 = UnivariatePolynomialZp64.create(
            2, new long[] {
                    1L, 0L, 0L, 1L, 1L, 0L, 0L, 1L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,
                    0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,
                    0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,
                    0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,
                    1L,
            });

    /**
     * module for GF(2^32): f(x) = x^32 + x^2 + 1
     */
    UnivariatePolynomialZp64 GF2K_POLYNOMIAL_32 = UnivariatePolynomialZp64.create(
            2, new long[] {
                    1L, 0L, 1L, 0L, 0L, 0L, 0L, 1L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,
                    0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,
                    1L,
            });
    FiniteField<UnivariatePolynomialZp64> module32 = GF(GF2K_POLYNOMIAL_32);
    FiniteField<UnivariatePolynomialZp64> module64 = GF(GF2K_POLYNOMIAL_64);
    FiniteField<UnivariatePolynomialZp64> module128 = GF(GF2K_POLYNOMIAL_128);

//    /**
//     * The following two parse statements must exist, otherwise it will seriously affect efficiency. It is suspected that the parse method will start some kind of instance, and after starting, the calculation will be faster (old method)
//     */
//    UnivariatePolynomialZp64 zeroValue64 = module64.parse("0");
//    UnivariatePolynomialZp64 zeroValue32 = module32.parse("0");

    SecretP zero();

    SecretP createFromBytes(byte[] bytes);

    SecretP createFromStrictVector(StrictBitVector value);

    int getBitLength();

    SecretP innerProduct(List<SecretP> left, List<SecretP> right);

    default long implMul64(long x, long y)
    {
        long x0 = x & 0x1111111111111111L;
        long x1 = x & 0x2222222222222222L;
        long x2 = x & 0x4444444444444444L;
        long x3 = x & 0x8888888888888888L;

        long y0 = y & 0x1111111111111111L;
        long y1 = y & 0x2222222222222222L;
        long y2 = y & 0x4444444444444444L;
        long y3 = y & 0x8888888888888888L;

        long z0 = (x0 * y0) ^ (x1 * y3) ^ (x2 * y2) ^ (x3 * y1);
        long z1 = (x0 * y1) ^ (x1 * y0) ^ (x2 * y3) ^ (x3 * y2);
        long z2 = (x0 * y2) ^ (x1 * y1) ^ (x2 * y0) ^ (x3 * y3);
        long z3 = (x0 * y3) ^ (x1 * y2) ^ (x2 * y1) ^ (x3 * y0);

        z0 &= 0x1111111111111111L;
        z1 &= 0x2222222222222222L;
        z2 &= 0x4444444444444444L;
        z3 &= 0x8888888888888888L;

        return z0 | z1 | z2 | z3;
    }

    default long[] implMul128(long x, long y)
    {
        long x0Low = x & 0x1111111111111111L;
        long x1Low = x & 0x2222222222222222L;
        long x2Low = x & 0x4444444444444444L;
        long x3Low = x & 0x8888888888888888L;

        long y0Low = y & 0x1111111111111111L;
        long y1Low = y & 0x2222222222222222L;
        long y2Low = y & 0x4444444444444444L;
        long y3Low = y & 0x8888888888888888L;

        long[] z0 = implXOR(
                implXOR(
                        implXOR(implMultiply(x0Low, y0Low), implMultiply(x1Low, y3Low)),
                        implMultiply(x2Low, y2Low)),
                implMultiply(x3Low, y1Low));
        long[] z1 = implXOR(
                implXOR(
                        implXOR(implMultiply(x0Low, y1Low), implMultiply(x1Low, y0Low)),
                        implMultiply(x2Low, y3Low)),
                implMultiply(x3Low, y2Low));
        long[] z2 = implXOR(
                implXOR(
                        implXOR(implMultiply(x0Low, y2Low), implMultiply(x1Low, y1Low)),
                        implMultiply(x2Low, y0Low)),
                implMultiply(x3Low, y3Low));
        long[] z3 = implXOR(
                implXOR(
                        implXOR(implMultiply(x0Low, y3Low), implMultiply(x1Low, y2Low)),
                        implMultiply(x2Low, y1Low)),
                implMultiply(x3Low, y0Low));

        z0 = new long[]{z0[0] & 0x1111111111111111L, z0[1] & 0x1111111111111111L};
        z1 = new long[]{z1[0] & 0x2222222222222222L, z1[1] & 0x2222222222222222L};
        z2 = new long[]{z2[0] & 0x4444444444444444L, z2[1] & 0x4444444444444444L};
        z3 = new long[]{z3[0] & 0x8888888888888888L, z3[1] & 0x8888888888888888L};

        return new long[]{z0[0] | z1[0] | z2[0] | z3[0], z0[1] | z1[1] | z2[1] | z3[1]};
    }

    default long[] implMultiply(long x, long y) {
        long xLow = x & 0xffffffffL;
        long xHigh = (x >>> 32) & 0xffffffffL;
        long yLow = y & 0xffffffffL;
        long yHigh = (y >>> 32) & 0xffffffffL;
        long t1 = xLow * yLow;
        long t2 = xLow * yHigh;
        long t3 = xHigh * yLow;
        long t4 = xHigh * yHigh;
        long resultLow = t1 + ((t2 & 0xffffffffL) << 32) + ((t3 & 0xffffffffL) << 32);
        long carry = (((t1 >>> 32) & 0xffffffffL) + (t2 & 0xffffffffL) + (t3 & 0xffffffffL)) >>> 32;
        long resultHigh = t4 + ((t2 >>> 32) & 0xffffffffL) + ((t3 >>> 32) & 0xffffffffL) + carry;
        return new long[]{resultLow, resultHigh};  // low first
    }

    default long[] implXOR(long[] x, long[] y) {
        return new long[]{x[0] ^ y[0], x[1] ^ y[1]};  // low first
    }
}
