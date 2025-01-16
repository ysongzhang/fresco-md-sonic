package dk.alexandra.fresco.framework.util;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SBool;

public class SBoolPair extends Pair<DRes<SBool>, DRes<SBool>> {
    public SBoolPair(DRes<SBool> first,
                    DRes<SBool> second) {
        super(first, second);
    }
}
