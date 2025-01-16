package dk.alexandra.fresco.suite.mdml.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdml.datatypes.*;
import dk.alexandra.fresco.suite.mdml.resource.MdmlResourcePool;

import java.util.List;

/**
 * Native protocol for computing product of two secret numbers.
 */
public class MdmlMultiplyProtocol<PlainT extends MdmlCompUInt<?, ?, PlainT>> extends
    MdmlNativeProtocol<SInt, PlainT> {

  private final DRes<SInt> left;
  private final DRes<SInt> right;
  private MdmlTriple<PlainT, MdmlASIntArithmetic<PlainT>> triple;  // from getNextTripleSharesFull, contains a, b, c
  private PlainT delta_x;  // from getNextOpenedDelta
  private PlainT delta_y;  // from getNextOpenedDelta
  private MdmlASIntArithmetic<PlainT> lambda_z;  // from getNextRandomElementShare
  private MdmlASIntArithmetic<PlainT> Delta_z;
  private PlainT opened;
  private SInt product;

  /**
   * Creates new {@link MdmlMultiplyProtocol}.
   *
   * @param left left factor
   * @param right right factor
   */
  public MdmlMultiplyProtocol(DRes<SInt> left, DRes<SInt> right) {
    this.left = left;
    this.right = right;
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
      lambda_z = resourcePool.getDataSupplier().getNextRandomElementShare();

      MdmlMSIntArithmetic<PlainT> outLeft = factory.toMdmlMSIntArithmetic(left);
      MdmlMSIntArithmetic<PlainT> outRight = factory.toMdmlMSIntArithmetic(right);
      PlainT Delta_delta_x = outLeft.getOpened().add(delta_x);
      PlainT Delta_delta_y = outRight.getOpened().add(delta_y);
      PlainT crossOpen = Delta_delta_x.multiply(Delta_delta_y);
      MdmlASIntArithmetic<PlainT> input = triple.getProduct().addConstant(crossOpen, macKeyShare, factory.zero(), resourcePool.getMyId() == 1)
              .subtract(triple.getLeft().multiply(Delta_delta_y))
              .subtract(triple.getRight().multiply(Delta_delta_x));
      // ASS to MSS:
      Delta_z = input.add(lambda_z);
      network.sendToAll(Delta_z.serializeShare());
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      ByteSerializer<PlainT> serializer = resourcePool.getPlainSerializer();
      List<PlainT> shares = serializer.deserializeList(network.receiveFromAll());
      opened = MdmlUInt.sum(shares);
      openedValueStore.pushOpenedValue(Delta_z, opened);
      this.product = new MdmlMSIntArithmetic<>(lambda_z, opened);
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public SInt out() {
    return product;
  }

}
