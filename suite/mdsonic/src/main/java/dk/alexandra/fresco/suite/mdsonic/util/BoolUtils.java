package dk.alexandra.fresco.suite.mdsonic.util;

import java.util.List;

public class BoolUtils {
    public static boolean xor (List<Boolean> allShares) {
        boolean res = false;
        for (Boolean share : allShares) {
            res ^= share.booleanValue();
        }
        return res;
    }
}
