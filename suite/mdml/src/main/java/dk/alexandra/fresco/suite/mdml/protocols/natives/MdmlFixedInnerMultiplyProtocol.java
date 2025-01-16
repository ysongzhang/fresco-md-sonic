package dk.alexandra.fresco.suite.mdml.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric.TruncationPair;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.lib.real.fixed.SFixed;
import dk.alexandra.fresco.suite.mdml.datatypes.*;
import dk.alexandra.fresco.suite.mdml.resource.MdmlResourcePool;

import java.util.ArrayList;
import java.util.List;

/**
 * Native protocol from computing the inner product of two SReal with truncation.
 */
public class MdmlFixedInnerMultiplyProtocol<PlainT extends MdmlCompUInt<?, ?, PlainT>>
    extends MdmlNativeProtocol<SReal, PlainT> {

  // Input:
  private final DRes<List<DRes<SReal>>> x;
  private final DRes<List<DRes<SReal>>> y;

  // Computations:
  private List<MdmlASIntArithmetic<PlainT>> aList;  // from getNextTripleSharesFull
  private List<MdmlASIntArithmetic<PlainT>> bList;  // from getNextTripleSharesFull
  private MdmlASIntArithmetic<PlainT> c;  // from getNextTripleSharesFull
  private List<PlainT> delta_x_list;  // from getNextOpenedDelta
  private List<PlainT> delta_y_list;  // from getNextOpenedDelta
  private MdmlASIntArithmetic<PlainT> lambda_z_prime;  // from getNextTruncationPair
  private MdmlASIntArithmetic<PlainT> lambda_z;  // from getNextTruncationPair
  private MdmlASIntArithmetic<PlainT> Delta_z_prime;
  private PlainT opened;
  private final int shifts;

  // Output:
  private SReal out;

  /**
   * Creates new {@link MdmlFixedInnerMultiplyProtocol}.
   */

  public MdmlFixedInnerMultiplyProtocol(DRes<List<DRes<SReal>>> x, DRes<List<DRes<SReal>>> y, int shifts) {
    this.x = x;
    this.y = y;
    this.shifts = shifts;
  }

  @Override
  public NativeProtocol.EvaluationStatus evaluate(int round, MdmlResourcePool<PlainT> resourcePool,
                                                  Network network) {
    final PlainT macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
    MdmlCompUIntFactory<PlainT> factory = resourcePool.getFactory();
    OpenedValueStore<MdmlASIntArithmetic<PlainT>, PlainT> openedValueStore = resourcePool.getOpenedValueStore();
    if (round == 0) {
      List<DRes<SReal>> xOut = x.out();
      List<DRes<SReal>> yOut = y.out();
      if (xOut.size() != yOut.size()) {
        throw new IllegalArgumentException("Vectors must have same size");
      }

      aList = new ArrayList<>(xOut.size());
      bList = new ArrayList<>(xOut.size());
      delta_x_list = new ArrayList<>(xOut.size());
      delta_y_list = new ArrayList<>(xOut.size());
      for (int i = 0; i < xOut.size(); i++) {
        MdmlTriple<PlainT, MdmlASIntArithmetic<PlainT>> triple = resourcePool.getDataSupplier().getNextTripleSharesFull();
        aList.add(triple.getLeft());
        bList.add(triple.getRight());
        delta_x_list.add(resourcePool.getDataSupplier().getNextOpenedDelta());
        delta_y_list.add(resourcePool.getDataSupplier().getNextOpenedDelta());
        if (i == 0) {
          c = triple.getProduct();
        } else {
          c = c.add(triple.getProduct());
        }
      }
      TruncationPair truncationPair = resourcePool.getDataSupplier().getNextTruncationPair(shifts);
      lambda_z_prime = factory.toMdmlASIntArithmetic(truncationPair.getRPrime());
      lambda_z = factory.toMdmlASIntArithmetic(truncationPair.getR());

      MdmlASIntArithmetic<PlainT>[] xMaskedArray = new MdmlASIntArithmetic[xOut.size()];
      MdmlASIntArithmetic<PlainT>[] yMaskedArray = new MdmlASIntArithmetic[xOut.size()];
      List<PlainT> xOpenedList = new ArrayList<>(xOut.size());
      List<PlainT> yOpenedList = new ArrayList<>(xOut.size());

      for (int i = 0; i < xOut.size(); i++) {
        MdmlMSIntArithmetic<PlainT> xTemp = factory.toMdmlMSIntArithmetic(((SFixed) xOut.get(i).out()).getSInt());
        MdmlMSIntArithmetic<PlainT> yTemp = factory.toMdmlMSIntArithmetic(((SFixed) yOut.get(i).out()).getSInt());
        xMaskedArray[i] = xTemp.getMaskedSecret();
        yMaskedArray[i] = yTemp.getMaskedSecret();
        xOpenedList.add(xTemp.getOpened());
        yOpenedList.add(yTemp.getOpened());
      }

      List<PlainT> Delta_delta_x = addOfTwoPublic(xOpenedList, delta_x_list);
      List<PlainT> Delta_delta_y = addOfTwoPublic(yOpenedList, delta_y_list);
      PlainT crossOpen = innerProductOfTwoPublic(Delta_delta_x, Delta_delta_y);
      MdmlASIntArithmetic<PlainT> product1 = innerProductWithPublicPart(Delta_delta_y, aList);
      MdmlASIntArithmetic<PlainT> product2 = innerProductWithPublicPart(Delta_delta_x, bList);
      MdmlASIntArithmetic<PlainT> input = c.addConstant(crossOpen, macKeyShare, factory.zero(), resourcePool.getMyId() == 1)
              .subtract(product1)
              .subtract(product2);

      // ASS to MSS:
      Delta_z_prime = input.add(lambda_z_prime);
      network.sendToAll(Delta_z_prime.serializeShare());
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      ByteSerializer<PlainT> serializer = resourcePool.getPlainSerializer();
      List<PlainT> shares = serializer.deserializeList(network.receiveFromAll());
      opened = MdmlUInt.sum(shares);
      openedValueStore.pushOpenedValue(Delta_z_prime, opened);
      this.out = new SFixed(new MdmlMSIntArithmetic<>(lambda_z, opened.shiftRightLowOnlySigned(shifts)), shifts);
      return EvaluationStatus.IS_DONE;
    }
  }

  private MdmlASIntArithmetic<PlainT> innerProductWithPublicPart(List<PlainT> a, List<MdmlASIntArithmetic<PlainT>> b) {
    if (a.size() != b.size()) {
      throw new IllegalArgumentException("Vectors must have same size");
    }
    MdmlASIntArithmetic<PlainT> product = b.get(0).multiply(a.get(0));
    for (int i = 1; i < a.size(); i++) {
      product = product.add(b.get(i).multiply(a.get(i)));
    }
    return product;
  }

  private PlainT innerProductOfTwoPublic(List<PlainT> a, List<PlainT> b) {
    if (a.size() != b.size()) {
      throw new IllegalArgumentException("Vectors must have same size");
    }
    PlainT product = a.get(0).multiply(b.get(0));
    for (int i = 1; i < a.size(); i++) {
      product = product.add(a.get(i).multiply(b.get(i)));
    }
    return product;
  }

  private List<PlainT> addOfTwoPublic(List<PlainT> a, List<PlainT> b) {
    if (a.size() != b.size()) {
      throw new IllegalArgumentException("Vectors must have same size");
    }
    List<PlainT> result = new ArrayList<>(a.size());
    for (int i = 0; i < a.size(); i++) {
      result.add(a.get(i).add(b.get(i)));
    }
    return result;
  }

  @Override
  public SReal out() {
    return out;
  }

}
