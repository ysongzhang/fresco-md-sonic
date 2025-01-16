package dk.alexandra.fresco.suite.mdml.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUIntFactory;
import dk.alexandra.fresco.suite.mdml.resource.MdmlResourcePool;

public class MdmlXorKnownProtocol<PlainT extends MdmlCompUInt<?, ?, PlainT>> extends
    MdmlNativeProtocol<SInt, PlainT> {

  private final DRes<OInt> left;
  private final DRes<SInt> right;
  private SInt result;

  public MdmlXorKnownProtocol(
      DRes<OInt> left,
      DRes<SInt> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdmlResourcePool<PlainT> resourcePool,
      Network network) {
    MdmlCompUIntFactory<PlainT> factory = resourcePool.getFactory();
    PlainT known = factory.fromOInt(left.out()).toBitRep();
    PlainT secretSharedKey = resourcePool.getDataSupplier().getSecretSharedKey();
    result = factory.toMdmlSIntBoolean(right).xorOpen(known,
        secretSharedKey, factory.zero().toBitRep(), resourcePool.getMyId() == 1);
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return result;
  }

}
