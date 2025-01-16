package dk.alexandra.fresco.suite.mdsonic.protocols.natives.MSS;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;
import dk.alexandra.fresco.suite.mdsonic.protocols.natives.MdsonicNativeProtocol;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Native protocol from computing the inner product of two SInt.
 */
public class MdsonicInnerProductProtocolMSS<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>>
    extends MdsonicNativeProtocol<SInt, PlainT, SecretP> {

  // Input:
  private final DRes<List<DRes<SInt>>> x;
  private final DRes<List<DRes<SInt>>> y;

  // Computations:
  private MdsonicASIntArithmetic<PlainT> innerProduct;

  // ASSToMSS:
  private MdsonicASIntArithmetic<PlainT> maskedSecret;
  private MdsonicASIntArithmetic<PlainT> openedSecret;
  private PlainT opened;

  // Output:
  private SInt out;

  /**
   * Creates new {@link MdsonicInnerProductProtocolMSS}.
   */

  public MdsonicInnerProductProtocolMSS(DRes<List<DRes<SInt>>> x, DRes<List<DRes<SInt>>> y) {
    this.x = x;
    this.y = y;
    this.maskedSecret = null;
  }

  public MdsonicInnerProductProtocolMSS(DRes<List<DRes<SInt>>> x, DRes<List<DRes<SInt>>> y, DRes<SInt> maskedSecret) {
    this.x = x;
    this.y = y;
    this.maskedSecret = (MdsonicASIntArithmetic<PlainT>) maskedSecret.out();
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    final PlainT macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
    MdsonicCompUIntFactory<PlainT> factory = resourcePool.getFactory();
    OpenedValueStore<MdsonicASIntArithmetic<PlainT>, PlainT> openedValueStore = resourcePool.getOpenedValueStore();
    if (round == 0) {
      innerProduct = resourcePool.getDataSupplier().getNextInnerProductShare();
      List<DRes<SInt>> xOut = x.out();
      List<DRes<SInt>> yOut = y.out();
      if (xOut.size() != yOut.size()) {
        throw new IllegalArgumentException("Vectors must have same size");
      }

      MdsonicASIntArithmetic<PlainT>[] xMaskedArray = new MdsonicASIntArithmetic[xOut.size()];
      MdsonicASIntArithmetic<PlainT>[] yMaskedArray = new MdsonicASIntArithmetic[xOut.size()];
      List<PlainT> xOpenedList = new ArrayList<>(xOut.size());
      List<PlainT> yOpenedList = new ArrayList<>(xOut.size());

      for (int i = 0; i < xOut.size(); i++) {
        MdsonicMSIntArithmetic<PlainT> xTemp = factory.toMdsonicMSIntArithmetic(xOut.get(i));
        MdsonicMSIntArithmetic<PlainT> yTemp = factory.toMdsonicMSIntArithmetic(yOut.get(i));
        xMaskedArray[i] = xTemp.getMaskedSecret();
        yMaskedArray[i] = yTemp.getMaskedSecret();
        xOpenedList.add(xTemp.getOpened());
        yOpenedList.add(yTemp.getOpened());
      }

      MdsonicASIntArithmetic<PlainT> xmyo = innerProductWithPublicPart(yOpenedList, Arrays.asList(xMaskedArray));
      MdsonicASIntArithmetic<PlainT> xoym = innerProductWithPublicPart(xOpenedList, Arrays.asList(yMaskedArray));
      PlainT xoyo = innerProductOfTwoPublic(xOpenedList, yOpenedList);
      MdsonicASIntArithmetic<PlainT> product = innerProduct
              .add(xmyo)
              .add(xoym)
              .addConstant(xoyo, macKeyShare, factory.zero(), resourcePool.getMyId() == 1);

      // ASS to MSS:
      if (maskedSecret == null) {
        maskedSecret = resourcePool.getDataSupplier().getNextRandomElementShare();
      }
      openedSecret = product.subtract(maskedSecret);
      network.sendToAll(openedSecret.serializeShare());
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      ByteSerializer<PlainT> serializer = resourcePool.getPlainSerializer();
      List<PlainT> shares = serializer.deserializeList(network.receiveFromAll());
      opened = MdsonicUInt.sum(shares);
      openedValueStore.pushOpenedValue(openedSecret, opened);
      this.out = new MdsonicMSIntArithmetic<>(maskedSecret, opened);
      return EvaluationStatus.IS_DONE;
    }
  }

  private MdsonicASIntArithmetic<PlainT> innerProductWithPublicPart(List<PlainT> a, List<MdsonicASIntArithmetic<PlainT>> b) {
    if (a.size() != b.size()) {
      throw new IllegalArgumentException("Vectors must have the same size");
    }
    MdsonicASIntArithmetic<PlainT> product = b.get(0).multiply(a.get(0));
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

  @Override
  public SInt out() {
    return out;
  }

}
