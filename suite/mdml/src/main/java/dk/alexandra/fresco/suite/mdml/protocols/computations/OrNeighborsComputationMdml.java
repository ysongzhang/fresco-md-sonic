package dk.alexandra.fresco.suite.mdml.protocols.computations;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdml.protocols.natives.MdmlOrBatchedProtocol;

import java.util.ArrayList;
import java.util.List;

/**
 * Computation for computing one layer of K-ary logical OR
 */
public class OrNeighborsComputationMdml implements
    Computation<List<DRes<SInt>>, ProtocolBuilderNumeric> {

  private final List<DRes<SInt>> bits;

  public OrNeighborsComputationMdml(List<DRes<SInt>> bits) {
    this.bits = bits;
  }

  @Override
  public DRes<List<DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder) {
    List<DRes<SInt>> leftBits = new ArrayList<>(bits.size() / 2);
    List<DRes<SInt>> rightBits = new ArrayList<>(bits.size() / 2);
    for (int i = 0; i < bits.size() - 1; i += 2) {
      leftBits.add(bits.get(i));
      rightBits.add(bits.get(i + 1));
    }
    final boolean isOdd = bits.size() % 2 != 0;
    return builder.append(new MdmlOrBatchedProtocol<>(
        () -> leftBits,
        () -> rightBits,
        isOdd ? bits.get(bits.size() - 1) : null));
  }
}
