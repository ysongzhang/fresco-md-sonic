package dk.alexandra.fresco.lib.math.integer.binary;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.List;

/**
 * Represents a random element and its bit decomposition.
 */
public class RandomBitMask {

  private final DRes<List<DRes<SInt>>> bits;  // bits中是先低位后高位
  private final DRes<SInt> value;

  RandomBitMask(DRes<List<DRes<SInt>>> bits, DRes<SInt> value) {
    this.bits = bits;
    this.value = value;
  }

  public DRes<List<DRes<SInt>>> getBits() {
    return bits;
  }

  public DRes<SInt> getValue() {
    return value;
  }

}
