package dk.alexandra.fresco.framework.builder.numeric.mdsonic;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.SInt;

import java.util.Collections;
import java.util.List;

public class FMedaBit {  // The highest bit is in the last. little-endian

    private final DRes<SInt> value;  // ASS

    private final int FMDegree;  // 0 or 2 or 4
    private final int FMLength;

    private final int bitLength;

    private List<DRes<SBool>> valueBooleanList;  // ASS

    private List<DRes<SBool>> valueProductList;  // ASS

    private DRes<OInt> openedValue;

    public FMedaBit(DRes<SInt> value, int bitLength, int FMDegree, int FMLength,
                    List<DRes<SBool>> valueBooleanList,
                    List<DRes<SBool>> valueProductList,
                    DRes<OInt> openedValue) {
        this.bitLength= bitLength;
        this.value = value;
        this.FMDegree = FMDegree;
        this.FMLength = FMLength;
        this.valueBooleanList = valueBooleanList;
        this.valueProductList = valueProductList;
        this.openedValue = openedValue;
    }

    public FMedaBit(DRes<SInt> value, int bitLength,
                    List<DRes<SBool>> valueBooleanList) {
        this.bitLength= bitLength;
        this.value = value;
        this.FMDegree = 0;
        this.FMLength = 0;
        this.valueBooleanList = valueBooleanList;
        this.valueProductList = null;
        this.openedValue = null;
    }

    public DRes<SInt> getValue() {
        return this.value;
    }

    public int getFMDegree() {  // 使用前需要检查degree是否等于0
        return this.FMDegree;
    }

    public int getFMLength() {
        return this.FMLength;
    }

    public List<DRes<SBool>> getValueBooleanList() {
        return this.valueBooleanList;
    }

    public List<DRes<SBool>> getValueBooleanListBigOrder() {
        List<DRes<SBool>> valueBooleanListBigOrder = valueBooleanList;
        Collections.reverse(valueBooleanListBigOrder);
        return valueBooleanListBigOrder;
    }

    public List<DRes<SBool>> getValueProductList() {
        return this.valueProductList;
    }

    public List<DRes<SBool>> getValueProductListBigOrder() {
        List<DRes<SBool>> valueProductListBigOrder = valueProductList;
        Collections.reverse(valueProductListBigOrder);
        return valueProductListBigOrder;
    }

    public DRes<OInt> getOpenedValue() {
        return this.openedValue;
    }

    public int getBitLength() {
        return this.bitLength;
    }
}
