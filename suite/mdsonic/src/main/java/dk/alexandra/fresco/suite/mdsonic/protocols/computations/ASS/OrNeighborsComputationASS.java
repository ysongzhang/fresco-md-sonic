package dk.alexandra.fresco.suite.mdsonic.protocols.computations.ASS;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.mdsonic.protocols.natives.MdsonicOrBatchedProtocol;

import java.util.ArrayList;
import java.util.List;

/**
 * Computation for computing one layer of K-ary logical OR
 */
public class OrNeighborsComputationASS implements
    Computation<List<DRes<SBool>>, ProtocolBuilderNumeric> {

  private final DRes<List<DRes<SBool>>> bits;

  public OrNeighborsComputationASS(DRes<List<DRes<SBool>>> bits) {
    this.bits = bits;
  }

  @Override
  public DRes<List<DRes<SBool>>> buildComputation(ProtocolBuilderNumeric builder) {
    List<DRes<SBool>> bitsOut = bits.out();
    List<DRes<SBool>> leftBits = new ArrayList<>(bitsOut.size() / 2);
    List<DRes<SBool>> rightBits = new ArrayList<>(bitsOut.size() / 2);
    for (int i = 0; i < bitsOut.size() - 1; i += 2) {
      leftBits.add(bitsOut.get(i));
      rightBits.add(bitsOut.get(i + 1));
    }
    final boolean isOdd = bitsOut.size() % 2 != 0;
    return builder.append(new MdsonicOrBatchedProtocol<>(
        () -> leftBits,  // 匿名函数会实现DRes接口
        () -> rightBits,
        isOdd ? bitsOut.get(bitsOut.size() - 1) : null));
  }
}
