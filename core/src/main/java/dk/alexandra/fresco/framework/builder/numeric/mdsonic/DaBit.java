package dk.alexandra.fresco.framework.builder.numeric.mdsonic;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.SInt;

public class DaBit {
    // bitA == bitB
    private final DRes<SBool> bitA;  // bit value in the binary sharing

    private final DRes<SInt> bitB; // bit value in the arithmetic sharing

    public DaBit(DRes<SBool> bitA, DRes<SInt> bitB) {
        this.bitA = bitA;
        this.bitB = bitB;
    }

    public DRes<SBool> getBitA() {
        return bitA;
    }

    public DRes<SInt> getBitB() {
        return bitB;
    }
}
