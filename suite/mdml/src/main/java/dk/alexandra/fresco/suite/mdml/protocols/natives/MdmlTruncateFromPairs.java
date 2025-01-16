package dk.alexandra.fresco.suite.mdml.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdml.datatypes.*;
import dk.alexandra.fresco.suite.mdml.resource.MdmlResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric.TruncationPair;

import java.util.List;

/**
 * Native protocol from computing the truncation under the MSS Mode.
 */
public class MdmlTruncateFromPairs<PlainT extends MdmlCompUInt<?, ?, PlainT>> extends
        MdmlNativeProtocol<SInt, PlainT> {

  private final DRes<SInt> input;
  private final int shifts;
  private MdmlASIntArithmetic<PlainT> c;
  private PlainT openedC;
  private SInt out;

  /**
   * Creates new {@link MdmlTruncateFromPairs}.
   */
  public MdmlTruncateFromPairs(DRes<SInt> input, int shifts) {
    this.input = input;
    this.shifts = shifts;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdmlResourcePool<PlainT> resourcePool,
                                                  Network network) {
    MdmlCompUIntFactory<PlainT> factory = resourcePool.getFactory();
    final PlainT macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
    MdmlMSIntArithmetic<PlainT> inputOut = factory.toMdmlMSIntArithmetic(input);
    PlainT opened = inputOut.getOpened();
    OpenedValueStore<MdmlASIntArithmetic<PlainT>, PlainT> openedValueStore = resourcePool.getOpenedValueStore();

    TruncationPair truncationPairD = resourcePool.getDataSupplier().getNextTruncationPair(shifts);
    if (round == 0) {
      MdmlASIntArithmetic<PlainT> maskedSecret = inputOut.getMaskedSecret();
      MdmlASIntArithmetic<PlainT> openedSInt = new MdmlASIntArithmetic<>(opened, macKeyShare, factory.zero(), resourcePool.getMyId() == 1);
      c = openedSInt.subtract(maskedSecret).add(factory.toMdmlASIntArithmetic(truncationPairD.getRPrime()));
      network.sendToAll(c.serializeShare());
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      ByteSerializer<PlainT> serializer = resourcePool.getPlainSerializer();
      List<PlainT> shares = serializer.deserializeList(network.receiveFromAll());
      openedC = MdmlUInt.sum(shares);
      openedValueStore.pushOpenedValue(c, openedC);
      out = new MdmlMSIntArithmetic<>(factory.toMdmlASIntArithmetic(truncationPairD.getR()), openedC.shiftRightLowOnlySigned(shifts));
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public SInt out() {
    return out;
  }

}
