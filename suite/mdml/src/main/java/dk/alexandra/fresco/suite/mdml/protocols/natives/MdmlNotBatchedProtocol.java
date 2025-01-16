package dk.alexandra.fresco.suite.mdml.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUIntFactory;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlSIntBoolean;
import dk.alexandra.fresco.suite.mdml.resource.MdmlResourcePool;

import java.util.ArrayList;
import java.util.List;

public class MdmlNotBatchedProtocol<PlainT extends MdmlCompUInt<?, ?, PlainT>> extends
        MdmlNativeProtocol<List<DRes<SInt>>, PlainT> {

  private final DRes<List<DRes<SInt>>> bits;
  private List<DRes<SInt>> result;

  public MdmlNotBatchedProtocol(DRes<List<DRes<SInt>>> bits) {
    this.bits = bits;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdmlResourcePool<PlainT> resourcePool,
      Network network) {
    MdmlCompUIntFactory<PlainT> factory = resourcePool.getFactory();
    PlainT secretSharedKey = resourcePool.getDataSupplier().getSecretSharedKey();
    List<DRes<SInt>> bitsOut = bits.out();
    this.result = new ArrayList<>(bitsOut.size());
    for (DRes<SInt> secretBit : bitsOut) {
      MdmlSIntBoolean<PlainT> notBit = factory.toMdmlSIntBoolean(secretBit)
          .xorOpen(factory.one().toBitRep(), secretSharedKey, factory.zero().toBitRep(),
              resourcePool.getMyId() == 1);
      result.add(notBit);
    }
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public List<DRes<SInt>> out() {
    return result;
  }

}
