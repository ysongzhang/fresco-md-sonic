package dk.alexandra.fresco.suite.mdml.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.lib.real.fixed.SFixed;
import dk.alexandra.fresco.suite.mdml.datatypes.*;
import dk.alexandra.fresco.suite.mdml.resource.MdmlResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric.TruncationPair;

import java.util.List;

/**
 * Native protocol for computing product of two secret real numbers with truncation.
 */
public class MdmlFixedMultiplyProtocol<PlainT extends MdmlCompUInt<?, ?, PlainT>> extends
    MdmlNativeProtocol<SReal, PlainT> {

  private final DRes<SReal> left;
  private final DRes<SReal> right;
  private MdmlTriple<PlainT, MdmlASIntArithmetic<PlainT>> triple;  // from getNextTripleSharesFull, contains a, b, c
  private PlainT delta_x;  // from getNextOpenedDelta
  private PlainT delta_y;  // from getNextOpenedDelta
  private MdmlASIntArithmetic<PlainT> lambda_z_prime;  // from getNextTruncationPair
  private MdmlASIntArithmetic<PlainT> lambda_z;  // from getNextTruncationPair
  private MdmlASIntArithmetic<PlainT> Delta_z_prime;
  private PlainT opened;
  private final int shifts;
  private SReal product;

  /**
   * Creates new {@link MdmlFixedMultiplyProtocol}.
   *
   * @param left left factor
   * @param right right factor
   */
  public MdmlFixedMultiplyProtocol(DRes<SReal> left, DRes<SReal> right, int shifts) {
    this.left = left;
    this.right = right;
    this.shifts = shifts;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdmlResourcePool<PlainT> resourcePool,
      Network network) {
    final PlainT macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
    MdmlCompUIntFactory<PlainT> factory = resourcePool.getFactory();
    OpenedValueStore<MdmlASIntArithmetic<PlainT>, PlainT> openedValueStore = resourcePool.getOpenedValueStore();

    if (round == 0) {
      triple = resourcePool.getDataSupplier().getNextTripleSharesFull();
      delta_x = resourcePool.getDataSupplier().getNextOpenedDelta();
      delta_y = resourcePool.getDataSupplier().getNextOpenedDelta();
      TruncationPair truncationPair = resourcePool.getDataSupplier().getNextTruncationPair(shifts);
      lambda_z_prime = factory.toMdmlASIntArithmetic(truncationPair.getRPrime());
      lambda_z = factory.toMdmlASIntArithmetic(truncationPair.getR());

      MdmlMSIntArithmetic<PlainT> outLeft = factory.toMdmlMSIntArithmetic(((SFixed) left.out()).getSInt());
      MdmlMSIntArithmetic<PlainT> outRight = factory.toMdmlMSIntArithmetic(((SFixed) right.out()).getSInt());
      PlainT Delta_delta_x = outLeft.getOpened().add(delta_x);
      PlainT Delta_delta_y = outRight.getOpened().add(delta_y);
      PlainT crossOpen = Delta_delta_x.multiply(Delta_delta_y);
      MdmlASIntArithmetic<PlainT> input = triple.getProduct().addConstant(crossOpen, macKeyShare, factory.zero(), resourcePool.getMyId() == 1)
              .subtract(triple.getLeft().multiply(Delta_delta_y))
              .subtract(triple.getRight().multiply(Delta_delta_x));
      // ASS to MSS:
      Delta_z_prime = input.add(lambda_z_prime);
      network.sendToAll(Delta_z_prime.serializeShare());
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      ByteSerializer<PlainT> serializer = resourcePool.getPlainSerializer();
      List<PlainT> shares = serializer.deserializeList(network.receiveFromAll());
      opened = MdmlUInt.sum(shares);
      openedValueStore.pushOpenedValue(Delta_z_prime, opened);
      this.product = new SFixed(new MdmlMSIntArithmetic<>(lambda_z, opened.shiftRightLowOnlySigned(shifts)), shifts);
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public SReal out() {
    return product;
  }

}
