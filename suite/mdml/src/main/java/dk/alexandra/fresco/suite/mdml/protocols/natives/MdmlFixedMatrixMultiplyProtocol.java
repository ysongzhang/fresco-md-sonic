package dk.alexandra.fresco.suite.mdml.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.lib.real.fixed.SFixed;
import dk.alexandra.fresco.suite.mdml.datatypes.*;
import dk.alexandra.fresco.suite.mdml.resource.MdmlResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric.MatrixTruncationPair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Native protocol from computing the matrix product of two SReal with truncation.
 */
public class MdmlFixedMatrixMultiplyProtocol<PlainT extends MdmlCompUInt<?, ?, PlainT>>
        extends MdmlNativeProtocol<Matrix<DRes<SReal>>, PlainT> {

  // Input:
  private final DRes<Matrix<DRes<SReal>>> x;
  private final DRes<Matrix<DRes<SReal>>> y;
  private final int shifts;

  // Computations:
  private MdmlMatrixTriple<PlainT, MdmlASIntArithmetic<PlainT>> matrixTriple;  // from getNextMatrixTripleShares, contains a, b, c
  private Matrix<PlainT> delta_x;  // from getNextMatrixOpenedDelta
  private Matrix<PlainT> delta_y;  // from getNextMatrixOpenedDelta
  private MatrixTruncationPair matrixTruncationPair;
  private Matrix<MdmlASIntArithmetic<PlainT>> Delta_z_prime;

  // ASSToMSS:
  private Matrix<MdmlASIntArithmetic<PlainT>> openedSecrets;

  // Output:
  private Matrix<DRes<SReal>> out;

  /**
   * Creates new {@link MdmlFixedMatrixMultiplyProtocol}.
   */

  public MdmlFixedMatrixMultiplyProtocol(DRes<Matrix<DRes<SReal>>> x, DRes<Matrix<DRes<SReal>>> y, int shifts) {
    this.x = x;
    this.y = y;
    this.shifts = shifts;
  }

  @Override
  public NativeProtocol.EvaluationStatus evaluate(int round, MdmlResourcePool<PlainT> resourcePool, Network network) {
    if (round == 0) {
      final PlainT macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
      MdmlCompUIntFactory<PlainT> factory = resourcePool.getFactory();
      Matrix<DRes<SReal>> xOut = x.out();
      Matrix<DRes<SReal>> yOut = y.out();
      int n1 = xOut.getHeight();
      int n2 = xOut.getWidth();
      int n3 = yOut.getWidth();
      if (n2 != yOut.getHeight()) {
        throw new IllegalArgumentException(
                "Matrice sizes does not match - " + xOut.getWidth() + " != " + yOut.getHeight());
      }
      matrixTriple = resourcePool.getDataSupplier().getNextMatrixTripleShares(n1, n2, n3);
      delta_x = resourcePool.getDataSupplier().getNextMatrixOpenedDelta(n1 ,n2);
      delta_y = resourcePool.getDataSupplier().getNextMatrixOpenedDelta(n2, n3);
      matrixTruncationPair = resourcePool.getDataSupplier().getNextMatrixTruncationPair(shifts, n1, n3);

      // Resolve Inputs
      int xHeight = xOut.getHeight();
      int xWidth = xOut.getWidth();
      int yHeight = yOut.getHeight();
      int yWidth = yOut.getWidth();
      ArrayList<ArrayList<PlainT>> xOpenedList = new ArrayList<>(xHeight);
      ArrayList<ArrayList<PlainT>> yOpenedList = new ArrayList<>(yHeight);

      for (int i = 0; i < xHeight; i++) {
        ArrayList<PlainT> xOpenedRow = new ArrayList<>(xWidth);
        ArrayList<DRes<SReal>> xRow = xOut.getRow(i);
        for (int j = 0; j < xWidth; j++) {
          MdmlMSIntArithmetic<PlainT> xTemp = factory.toMdmlMSIntArithmetic(((SFixed) xRow.get(j).out()).getSInt());
          xOpenedRow.add(xTemp.getOpened());
        }
        xOpenedList.add(xOpenedRow);
      }
      for (int i = 0; i < yHeight; i++) {
        ArrayList<PlainT> yOpenedRow = new ArrayList<>(yOut.getWidth());
        ArrayList<DRes<SReal>> yRow = yOut.getRow(i);
        for (int j = 0; j < yWidth; j++) {
          MdmlMSIntArithmetic<PlainT> yTemp = factory.toMdmlMSIntArithmetic(((SFixed) yRow.get(j).out()).getSInt());
          yOpenedRow.add(yTemp.getOpened());
        }
        yOpenedList.add(yOpenedRow);
      }

      Matrix<PlainT> xOpenedMatrix = new Matrix<>(xHeight, xWidth, xOpenedList);
      Matrix<PlainT> yOpenedMatrix = new Matrix<>(yHeight, yWidth, yOpenedList);
      Matrix<MdmlASIntArithmetic<PlainT>> xMaskedMatrix = matrixTriple.getLeft();
      Matrix<MdmlASIntArithmetic<PlainT>> yMaskedMatrix = matrixTriple.getRight();

      // Matrix computations
      Matrix<PlainT> Delta_delta_x = add(xOpenedMatrix, delta_x);
      Matrix<PlainT> Delta_delta_y = add(yOpenedMatrix, delta_y);
      Matrix<PlainT> crossOpen = mult(xOpenedMatrix, yOpenedMatrix, this::innerProductOfTwoPublic);
      Matrix<MdmlASIntArithmetic<PlainT>> product1 = addConstant(matrixTriple.getProduct(), crossOpen, macKeyShare, factory.zero(), resourcePool.getMyId() == 1);
      Matrix<MdmlASIntArithmetic<PlainT>> product2 = mult(xMaskedMatrix, yOpenedMatrix,
              (x, y) -> innerProductWithPublicPart(y, x));
//      Matrix<MdmlASIntArithmetic<PlainT>> product3 = mult(xOpenedMatrix, yMaskedMatrix,
//      this::innerProductWithPublicPart);  // unknown bug
      Matrix<MdmlASIntArithmetic<PlainT>> product3 = mult(xMaskedMatrix, yOpenedMatrix, (x, y) -> innerProductWithPublicPart(y, x));
      Delta_z_prime = sub(product1, product2);
      Delta_z_prime = sub(Delta_z_prime, product3);

      // ASS to MSS:
      serializeAndSend(network, factory);
      return NativeProtocol.EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      receiveAndReconstruct(network, resourcePool);
      return NativeProtocol.EvaluationStatus.IS_DONE;
    }
  }

  private void serializeAndSend(Network network, MdmlCompUIntFactory<PlainT> factory) {
    ArrayList<ArrayList<MdmlASIntArithmetic<PlainT>>> openedSecretList = new ArrayList<>(Delta_z_prime.getHeight());
    for (int i = 0; i < Delta_z_prime.getHeight(); i++) {
      ArrayList<MdmlASIntArithmetic<PlainT>> rowProduct = Delta_z_prime.getRow(i);
      ArrayList<DRes<SInt>> rowMasked = matrixTruncationPair.getRPrime().getRow(i);
      ArrayList<MdmlASIntArithmetic<PlainT>> tempList = new ArrayList<>(Delta_z_prime.getWidth());
      for (int j = 0; j < Delta_z_prime.getWidth(); j++) {
        MdmlASIntArithmetic<PlainT> openedSecret = rowProduct.get(j).add(factory.toMdmlASIntArithmetic(rowMasked.get(j)));
        tempList.add(openedSecret);
        network.sendToAll(openedSecret.serializeShare());
      }
      openedSecretList.add(tempList);
    }
    openedSecrets = new Matrix<>(Delta_z_prime.getHeight(), Delta_z_prime.getWidth(), openedSecretList);
  }

  private void receiveAndReconstruct(Network network, MdmlResourcePool<PlainT> resourcePool) {
    OpenedValueStore<MdmlASIntArithmetic<PlainT>, PlainT> openedValueStore = resourcePool.getOpenedValueStore();
    ByteSerializer<PlainT> serializer = resourcePool.getPlainSerializer();

    ArrayList<ArrayList<DRes<SReal>>> outList = new ArrayList<>(Delta_z_prime.getHeight());
    for (int i = 0; i < Delta_z_prime.getHeight(); i++) {
      ArrayList<DRes<SReal>> tempList = new ArrayList<>(Delta_z_prime.getWidth());
      for (int j = 0; j < Delta_z_prime.getWidth(); j++) {
        List<PlainT> shares = serializer.deserializeList(network.receiveFromAll());
        PlainT opened = MdmlUInt.sum(shares);
        openedValueStore.pushOpenedValue(openedSecrets.getRow(i).get(j), opened);
        PlainT truncateOpened = opened.shiftRightLowOnlySigned(shifts);
        MdmlASIntArithmetic<PlainT> truncationR = resourcePool.getFactory().toMdmlASIntArithmetic(matrixTruncationPair.getR().getRow(i).get(j));
        SInt outSInt = new MdmlMSIntArithmetic<>(truncationR, truncateOpened);
        tempList.add(new SFixed(outSInt, shifts));
      }
      outList.add(tempList);
    }
    out = new Matrix<>(Delta_z_prime.getHeight(), Delta_z_prime.getWidth(), outList);
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

  private <A, B, C> Matrix<C> mult(Matrix<A> a, Matrix<B> b,
                                   BiFunction<List<A>, List<B>, C> innerProductOperator) {
    if (a.getWidth() != b.getHeight()) {
      throw new IllegalArgumentException(
              "Matrice sizes does not match - " + a.getWidth() + " != " + b.getHeight());
    }

//    Matrix<B> bTrans = transpose(b);  // Exchange space for time
    Matrix<C> result = new Matrix<>(a.getHeight(), b.getWidth(), i -> {
      ArrayList<C> row = new ArrayList<>(b.getWidth());
      List<A> rowA = a.getRow(i);
      for (int j = 0; j < b.getWidth(); j++) {
        row.add(innerProductOperator.apply(rowA, b.getColumn(j)));
//        row.add(innerProductOperator.apply(rowA, bTrans.getRow(j)));
      }
      return row;
    });
    return result;
  }

  private Matrix<MdmlASIntArithmetic<PlainT>> sub(Matrix<MdmlASIntArithmetic<PlainT>> a, Matrix<MdmlASIntArithmetic<PlainT>> b) {
    Matrix<MdmlASIntArithmetic<PlainT>> result = new Matrix<>(a.getHeight(), a.getWidth(), i -> {
      ArrayList<MdmlASIntArithmetic<PlainT>> row = new ArrayList<>(a.getWidth());
      List<MdmlASIntArithmetic<PlainT>> rowA = a.getRow(i);
      List<MdmlASIntArithmetic<PlainT>> rowB = b.getRow(i);
      for (int j = 0; j < a.getWidth(); j++) {
        row.add(rowA.get(j).subtract(rowB.get(j)));
      }
      return row;
    });
    return result;
  }

  private Matrix<PlainT> add(Matrix<PlainT> a, Matrix<PlainT> b) {
    Matrix<PlainT> result = new Matrix<>(a.getHeight(), a.getWidth(), i -> {
      ArrayList<PlainT> row = new ArrayList<>(a.getWidth());
      List<PlainT> rowA = a.getRow(i);
      List<PlainT> rowB = b.getRow(i);
      for (int j = 0; j < a.getWidth(); j++) {
        row.add(rowA.get(j).add(rowB.get(j)));
      }
      return row;
    });
    return result;
  }

  private Matrix<MdmlASIntArithmetic<PlainT>> addConstant(Matrix<MdmlASIntArithmetic<PlainT>> a, Matrix<PlainT> b, PlainT macKeyShare, PlainT zero, boolean isPartyOne) {
    Matrix<MdmlASIntArithmetic<PlainT>> result = new Matrix<>(a.getHeight(), a.getWidth(), i -> {
      ArrayList<MdmlASIntArithmetic<PlainT>> row = new ArrayList<>(a.getWidth());
      List<MdmlASIntArithmetic<PlainT>> rowA = a.getRow(i);
      List<PlainT> rowB = b.getRow(i);
      for (int j = 0; j < a.getWidth(); j++) {
        row.add(rowA.get(j).addConstant(rowB.get(j), macKeyShare, zero, isPartyOne));
      }
      return row;
    });
    return result;
  }

  private <A> Matrix<A> transpose(Matrix<A> input) {
    Matrix<A> result = new Matrix<>(input.getWidth(), input.getHeight(), i -> {
      return input.getColumnArray(i);
    });
    return result;
  }

  @Override
  public Matrix<DRes<SReal>> out() {
    return out;
  }

}
