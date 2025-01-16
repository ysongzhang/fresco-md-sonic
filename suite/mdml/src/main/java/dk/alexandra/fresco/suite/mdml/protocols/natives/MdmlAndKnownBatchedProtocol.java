package dk.alexandra.fresco.suite.mdml.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUIntFactory;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlSIntBoolean;
import dk.alexandra.fresco.suite.mdml.resource.MdmlResourcePool;

import java.util.ArrayList;
import java.util.List;

public class MdmlAndKnownBatchedProtocol<PlainT extends MdmlCompUInt<?, ?, PlainT>> extends
        MdmlNativeProtocol<List<DRes<SInt>>, PlainT> {

  private final DRes<List<OInt>> left;
  private final DRes<List<DRes<SInt>>> right;
  private List<DRes<SInt>> result;

  public MdmlAndKnownBatchedProtocol(
      DRes<List<OInt>> left,
      DRes<List<DRes<SInt>>> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdmlResourcePool<PlainT> resourcePool,
      Network network) {
    MdmlCompUIntFactory<PlainT> factory = resourcePool.getFactory();
    List<OInt> leftOut = left.out();
    List<DRes<SInt>> rightOut = right.out();
    if (leftOut.size() != rightOut.size()) {
      throw new IllegalArgumentException("Lists must be same size");
    }
    this.result = new ArrayList<>(leftOut.size());
    for (int i = 0; i < leftOut.size(); i++) {
      PlainT knownBit = factory.fromOInt(leftOut.get(i)).toBitRep();
      DRes<SInt> secretBit = rightOut.get(i);
      MdmlSIntBoolean<PlainT> andedBit = factory.toMdmlSIntBoolean(secretBit)
          .and(knownBit.bitValue());
      result.add(andedBit);
    }
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public List<DRes<SInt>> out() {
    return result;
  }

}
