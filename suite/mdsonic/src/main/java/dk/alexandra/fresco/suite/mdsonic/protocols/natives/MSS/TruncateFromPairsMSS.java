package dk.alexandra.fresco.suite.mdsonic.protocols.natives.MSS;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.mdsonic.AdvancedNumericMdsonic.TruncationPair;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;
import dk.alexandra.fresco.suite.mdsonic.protocols.natives.MdsonicNativeProtocol;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;

import java.util.List;

/**
 * Native protocol from computing the truncation under the MSS Mode.
 */
public class TruncateFromPairsMSS<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>>
    extends MdsonicNativeProtocol<SInt, PlainT, SecretP> {

  private final DRes<SInt> input;
  private final int shifts;
  private final DRes<SInt> truncationR;
  private final boolean containTruncationR;
  private MdsonicASIntArithmetic<PlainT> c;
  private PlainT openedC;
  private SInt out;

  /**
   * Creates new {@link TruncateFromPairsMSS}.
   */

  public TruncateFromPairsMSS(DRes<SInt> input, int shifts, DRes<SInt> truncationR) {
    this.input = input;
    this.shifts = shifts;
    this.truncationR = truncationR;
    this.containTruncationR = true;
  }

  public TruncateFromPairsMSS(DRes<SInt> input, int shifts) {
    this.input = input;
    this.shifts = shifts;
    this.truncationR = null;
    this.containTruncationR = false;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    MdsonicCompUIntFactory<PlainT> factory = resourcePool.getFactory();
    final PlainT macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
    MdsonicMSIntArithmetic<PlainT> inputOut = factory.toMdsonicMSIntArithmetic(input);
    PlainT opened = inputOut.getOpened();
    OpenedValueStore<MdsonicASIntArithmetic<PlainT>, PlainT> openedValueStore = resourcePool.getOpenedValueStore();

    if (containTruncationR) {
      PlainT truncateOpened = opened.shiftRightLowOnlySigned(shifts);
      out = new MdsonicMSIntArithmetic<>(factory.toMdsonicASIntArithmetic(truncationR), truncateOpened);
      return EvaluationStatus.IS_DONE;
    } else {
      TruncationPair truncationPairD = resourcePool.getDataSupplier().getNextTruncationPair(shifts);
      if (round == 0) {
        MdsonicASIntArithmetic<PlainT> maskedSecret = inputOut.getMaskedSecret();
        c = maskedSecret.addConstant(opened, macKeyShare, factory.zero(), resourcePool.getMyId() == 1)
                .subtract(factory.toMdsonicASIntArithmetic(truncationPairD.getRPrime()));
        network.sendToAll(c.serializeShare());
        return EvaluationStatus.HAS_MORE_ROUNDS;
      } else {
        ByteSerializer<PlainT> serializer = resourcePool.getPlainSerializer();
        List<PlainT> shares = serializer.deserializeList(network.receiveFromAll());
        openedC = MdsonicUInt.sum(shares);
        openedValueStore.pushOpenedValue(c, openedC);
        out = new MdsonicMSIntArithmetic<>(factory.toMdsonicASIntArithmetic(truncationPairD.getR()), openedC.shiftRightLowOnlySigned(shifts));
        return EvaluationStatus.IS_DONE;
      }
    }
  }

  @Override
  public SInt out() {
    return out;
  }

}
