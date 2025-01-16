package dk.alexandra.fresco.suite.mdsonic.resource.storage;

import dk.alexandra.fresco.framework.util.AesCtrDrbgFactory;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.StrictBitVector;

public class BytePrg {
    private final Drbg drbg;

    public BytePrg(byte[] seed) {  // 32Bytes
        drbg = AesCtrDrbgFactory.fromDerivedSeed(seed);
    }

    public StrictBitVector getNext(int size) {
        return new StrictBitVector(size, drbg);
    }
}
