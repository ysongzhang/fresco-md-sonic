package dk.alexandra.fresco.suite.mdml.protocols.natives;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUIntFactory;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlMSIntArithmetic;
import dk.alexandra.fresco.suite.mdml.resource.MdmlResourcePool;
import dk.alexandra.fresco.suite.mdml.resource.storage.MdmlDataSupplier;

/**
 * Native protocol for converting a public constant into a secret MSS value.
 */
public class MdmlKnownSIntProtocol<PlainT extends MdmlCompUInt<?, ?, PlainT>>
    extends MdmlNativeProtocol<SInt, PlainT> {

  private final PlainT input;
  private SInt out;

  /**
   * Creates new {@link MdmlKnownSIntProtocol}.
   *
   * @param input public value to input
   */
  public MdmlKnownSIntProtocol(PlainT input) {
    this.input = input;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdmlResourcePool<PlainT> resourcePool,
      Network network) {
    MdmlCompUIntFactory<PlainT> factory = resourcePool.getFactory();
    MdmlDataSupplier<PlainT> dataSupplier = resourcePool.getDataSupplier();
    boolean isPartyOne = (resourcePool.getMyId() == 1);
    out = new MdmlMSIntArithmetic<>(input, dataSupplier.getSecretSharedKey(), factory.zero(), isPartyOne);
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public SInt out() {
    return out;
  }

}
