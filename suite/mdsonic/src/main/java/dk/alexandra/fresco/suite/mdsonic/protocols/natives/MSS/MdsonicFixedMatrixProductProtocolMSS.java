package dk.alexandra.fresco.suite.mdsonic.protocols.natives.MSS;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.mdsonic.AdvancedNumericMdsonic.MatrixTruncationPair;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.lib.real.fixed.SFixed;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;
import dk.alexandra.fresco.suite.mdsonic.protocols.natives.MdsonicNativeProtocol;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Native protocol from computing the matrix product of two SInt with truncation.
 */
public class MdsonicFixedMatrixProductProtocolMSS<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>>
    extends MdsonicNativeProtocol<Matrix<DRes<SReal>>, PlainT, SecretP> {

  // Input:
  private final DRes<Matrix<DRes<SReal>>> x;
  private final DRes<Matrix<DRes<SReal>>> y;
  private MatrixTruncationPair matrixTruncationPair;
  private final int shifts;

  // Computations:
  private Matrix<MdsonicASIntArithmetic<PlainT>> matrixProduct;
  private Matrix<MdsonicASIntArithmetic<PlainT>> product;

  // ASSToMSS:
  private Matrix<MdsonicASIntArithmetic<PlainT>> openedSecrets;

    // Output:
  private Matrix<DRes<SReal>> out;

  /**
   * Creates new {@link MdsonicFixedMatrixProductProtocolMSS}.
   */

  public MdsonicFixedMatrixProductProtocolMSS(DRes<Matrix<DRes<SReal>>> x, DRes<Matrix<DRes<SReal>>> y, int shifts) {
    this.x = x;
    this.y = y;
    this.matrixTruncationPair = null;
    this.shifts = shifts;
  }

  public MdsonicFixedMatrixProductProtocolMSS(DRes<Matrix<DRes<SReal>>> x, DRes<Matrix<DRes<SReal>>> y,
                                              DRes<MatrixTruncationPair> maskedSecret, int shifts) {
    this.x = x;
    this.y = y;
    this.matrixTruncationPair = maskedSecret.out();
    this.shifts = shifts;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool, Network network) {
    if (round == 0) {
      final PlainT macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
      MdsonicCompUIntFactory<PlainT> factory = resourcePool.getFactory();
      Matrix<DRes<SReal>> xOut = x.out();
      Matrix<DRes<SReal>> yOut = y.out();
      if (xOut.getWidth() != yOut.getHeight()) {
        throw new IllegalArgumentException(
                "Matrice sizes does not match - " + xOut.getWidth() + " != " + yOut.getHeight());
      }
      matrixProduct = resourcePool.getDataSupplier().getNextMatrixProductShare(xOut.getHeight(), yOut.getWidth());

      // Resolve inputs
      int xHeight = xOut.getHeight();
      int xWidth = xOut.getWidth();
      int yHeight = yOut.getHeight();
      int yWidth = yOut.getWidth();
      MdsonicASIntArithmetic<PlainT>[][] xMaskedArray = new MdsonicASIntArithmetic[xHeight][xWidth];
      MdsonicASIntArithmetic<PlainT>[][] yMaskedArray = new MdsonicASIntArithmetic[yHeight][yWidth];
      ArrayList<ArrayList<PlainT>> xOpenedList = new ArrayList<>(xHeight);
      ArrayList<ArrayList<PlainT>> yOpenedList = new ArrayList<>(yHeight);

      for (int i = 0; i < xHeight; i++) {
        ArrayList<PlainT> xOpenedRow = new ArrayList<>(xWidth);
        ArrayList<DRes<SReal>> xRow = xOut.getRow(i);
        for (int j = 0; j < xWidth; j++) {
          MdsonicMSIntArithmetic<PlainT> xTemp = factory.toMdsonicMSIntArithmetic(((SFixed) xRow.get(j).out()).getSInt());
          xMaskedArray[i][j] = xTemp.getMaskedSecret();
          xOpenedRow.add(xTemp.getOpened());
        }
        xOpenedList.add(xOpenedRow);
      }
      for (int i = 0; i < yHeight; i++) {
        ArrayList<PlainT> yOpenedRow = new ArrayList<>(yOut.getWidth());
        ArrayList<DRes<SReal>> yRow = yOut.getRow(i);
        for (int j = 0; j < yWidth; j++) {
          MdsonicMSIntArithmetic<PlainT> yTemp = factory.toMdsonicMSIntArithmetic(((SFixed) yRow.get(j).out()).getSInt());
          yMaskedArray[i][j] = yTemp.getMaskedSecret();
          yOpenedRow.add(yTemp.getOpened());
        }
        yOpenedList.add(yOpenedRow);
      }

      Matrix<MdsonicASIntArithmetic<PlainT>> xMaskedMatrix = new Matrix<>(xHeight, xWidth, xMaskedArray);
      Matrix<PlainT> xOpenedMatrix = new Matrix<>(xHeight, xWidth, xOpenedList);
      Matrix<MdsonicASIntArithmetic<PlainT>> yMaskedMatrix = new Matrix<>(yHeight, yWidth, yMaskedArray);
      Matrix<PlainT> yOpenedMatrix = new Matrix<>(yHeight, yWidth, yOpenedList);

      // Matrix computations
      Matrix<MdsonicASIntArithmetic<PlainT>> xmyo = mult(xMaskedMatrix, yOpenedMatrix, (x, y) -> innerProductWithPublicPart(y, x));
      Matrix<MdsonicASIntArithmetic<PlainT>> xoym = mult(xOpenedMatrix, yMaskedMatrix, this::innerProductWithPublicPart);
      Matrix<PlainT> xoyo = mult(xOpenedMatrix, yOpenedMatrix, this::innerProductOfTwoPublic);
      product = new Matrix<>(xOut.getHeight(), yOut.getWidth(), i -> {
        ArrayList<MdsonicASIntArithmetic<PlainT>> row = new ArrayList<>(yOut.getWidth());
        for (int j = 0; j < yOut.getWidth(); j++) {
          row.add(xmyo.getRow(i).get(j)
                  .add(xoym.getRow(i).get(j))
                  .add(matrixProduct.getRow(i).get(j))
                  .addConstant(xoyo.getRow(i).get(j), macKeyShare, factory.zero(), resourcePool.getMyId() == 1));
        }
        return row;
      });

      // ASS to MSS:
      if (matrixTruncationPair == null) {
        matrixTruncationPair = resourcePool.getDataSupplier().getNextMatrixTruncationPair(shifts, product.getHeight(), product.getWidth());
      }
      serializeAndSend(network, factory);
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      receiveAndReconstruct(network, resourcePool);
      return EvaluationStatus.IS_DONE;
    }
  }

  private void serializeAndSend(Network network, MdsonicCompUIntFactory<PlainT> factory) {
    ArrayList<ArrayList<MdsonicASIntArithmetic<PlainT>>> openedSecretList = new ArrayList<>(product.getHeight());
    for (int i = 0; i < product.getHeight(); i++) {
      ArrayList<MdsonicASIntArithmetic<PlainT>> rowProduct = product.getRow(i);
      ArrayList<DRes<SInt>> rowMasked = matrixTruncationPair.getRPrime().getRow(i);
      ArrayList<MdsonicASIntArithmetic<PlainT>> tempList = new ArrayList<>(product.getWidth());
      for (int j = 0; j < product.getWidth(); j++) {
        MdsonicASIntArithmetic<PlainT> openedSecret = rowProduct.get(j).subtract(factory.toMdsonicASIntArithmetic(rowMasked.get(j)));
        tempList.add(openedSecret);
        // send
        network.sendToAll(openedSecret.serializeShare());
      }
      openedSecretList.add(tempList);
    }
    openedSecrets = new Matrix<>(product.getHeight(), product.getWidth(), openedSecretList);
  }

  private void receiveAndReconstruct(Network network, MdsonicResourcePool<PlainT, SecretP> resourcePool) {
    OpenedValueStore<MdsonicASIntArithmetic<PlainT>, PlainT> openedValueStore = resourcePool.getOpenedValueStore();
    ByteSerializer<PlainT> serializer = resourcePool.getPlainSerializer();

    ArrayList<ArrayList<DRes<SReal>>> outList = new ArrayList<>(product.getHeight());
    for (int i = 0; i < product.getHeight(); i++) {
      ArrayList<DRes<SReal>> tempList = new ArrayList<>(product.getWidth());
      for (int j = 0; j < product.getWidth(); j++) {
        List<PlainT> shares = serializer.deserializeList(network.receiveFromAll());
        PlainT opened = MdsonicUInt.sum(shares);
        openedValueStore.pushOpenedValue(openedSecrets.getRow(i).get(j), opened);
        PlainT truncateOpened = opened.shiftRightLowOnlySigned(shifts);  // truncate
        MdsonicASIntArithmetic<PlainT> truncationR = resourcePool.getFactory().toMdsonicASIntArithmetic(matrixTruncationPair.getR().getRow(i).get(j));
        SInt outSInt = new MdsonicMSIntArithmetic<>(truncationR, truncateOpened);
        tempList.add(new SFixed(outSInt, shifts));
      }
      outList.add(tempList);
    }
    out = new Matrix<>(product.getHeight(), product.getWidth(), outList);
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

  private <A, B, C> Matrix<C> mult(Matrix<A> a, Matrix<B> b,
                                   BiFunction<List<A>, List<B>, C> innerProductOperator) {
    if (a.getWidth() != b.getHeight()) {
      throw new IllegalArgumentException(
              "Matrice sizes does not match - " + a.getWidth() + " != " + b.getHeight());
    }

    Matrix<C> result = new Matrix<>(a.getHeight(), b.getWidth(), i -> {
      ArrayList<C> row = new ArrayList<>(b.getWidth());
      List<A> rowA = a.getRow(i);
      for (int j = 0; j < b.getWidth(); j++) {
        row.add(innerProductOperator.apply(rowA, b.getColumn(j)));
      }
      return row;
    });
    return result;
  }

  @Override
  public Matrix<DRes<SReal>> out() {
    return out;
  }

}
