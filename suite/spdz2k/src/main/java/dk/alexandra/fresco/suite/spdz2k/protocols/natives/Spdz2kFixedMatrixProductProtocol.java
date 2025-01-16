package dk.alexandra.fresco.suite.spdz2k.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.lib.real.fixed.SFixed;
import dk.alexandra.fresco.suite.spdz2k.datatypes.*;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Native protocol from computing the matrix product of two SInt with truncation.
 */
public class Spdz2kFixedMatrixProductProtocol<PlainT extends CompUInt<?, ?, PlainT>>
    extends Spdz2kNativeProtocol<Matrix<DRes<SInt>>, PlainT> {

  // Input:
  private final DRes<Matrix<DRes<SReal>>> x;
  private final DRes<Matrix<DRes<SReal>>> y;

  // Computations:
  private Spdz2kMatrixTriple<PlainT, Spdz2kSIntArithmetic<PlainT>> matrixTriple;
  private Matrix<Spdz2kSIntArithmetic<PlainT>> epsilon;
  private Matrix<Spdz2kSIntArithmetic<PlainT>> delta;

  // Output:
  private Matrix<DRes<SInt>> out;

  /**
   * Creates new {@link Spdz2kFixedMatrixProductProtocol}.
   */

  public Spdz2kFixedMatrixProductProtocol(DRes<Matrix<DRes<SReal>>> x, DRes<Matrix<DRes<SReal>>> y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public EvaluationStatus evaluate(int round, Spdz2kResourcePool<PlainT> resourcePool, Network network) {
    CompUIntFactory<PlainT> factory = resourcePool.getFactory();
    if (round == 0) {
      Matrix<DRes<SReal>> xOut = x.out();
      Matrix<DRes<SReal>> yOut = y.out();
      if (xOut.getWidth() != yOut.getHeight()) {
        throw new IllegalArgumentException(
                "Matrice sizes does not match - " + xOut.getWidth() + " != " + yOut.getHeight());
      }
      matrixTriple = resourcePool.getDataSupplier().getNextMatrixTripleShares(xOut.getHeight(), yOut.getHeight(), yOut.getWidth());

      ArrayList<ArrayList<Spdz2kSIntArithmetic<PlainT>>> xList = new ArrayList<>(xOut.getHeight());
      ArrayList<ArrayList<Spdz2kSIntArithmetic<PlainT>>> yList = new ArrayList<>(yOut.getHeight());
      for (int i = 0; i < xOut.getHeight(); i++) {
        ArrayList<Spdz2kSIntArithmetic<PlainT>> xRowList = new ArrayList<>(xOut.getWidth());
        ArrayList<DRes<SReal>> xRow = xOut.getRow(i);
        for (int j = 0; j < xOut.getWidth(); j++) {
          Spdz2kSIntArithmetic<PlainT> xTemp = factory.toSpdz2kSIntArithmetic(((SFixed) xRow.get(j).out()).getSInt());
          xRowList.add(xTemp);
        }
        xList.add(xRowList);
      }
      for (int i = 0; i < yOut.getHeight(); i++) {
        ArrayList<Spdz2kSIntArithmetic<PlainT>> yRowList = new ArrayList<>(yOut.getWidth());
        ArrayList<DRes<SReal>> yRow = yOut.getRow(i);
        for (int j = 0; j < yOut.getWidth(); j++) {
          Spdz2kSIntArithmetic<PlainT> yTemp = factory.toSpdz2kSIntArithmetic(((SFixed) yRow.get(j).out()).getSInt());
          yRowList.add(yTemp);
        }
        yList.add(yRowList);
      }
      Matrix<Spdz2kSIntArithmetic<PlainT>> xMatrix = new Matrix<>(xOut.getHeight(), xOut.getWidth(), xList);
      Matrix<Spdz2kSIntArithmetic<PlainT>> yMatrix = new Matrix<>(yOut.getHeight(), yOut.getWidth(), yList);

      epsilon = sub(xMatrix, matrixTriple.getLeft());
      delta = sub(yMatrix, matrixTriple.getRight());
      serializeAndSend(network);
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      final PlainT macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
      Pair<Matrix<PlainT>, Matrix<PlainT>> epsilonAndDelta = receiveAndReconstruct(network, resourcePool);
      Matrix<PlainT> e = epsilonAndDelta.getFirst();
      Matrix<PlainT> d = epsilonAndDelta.getSecond();
      Matrix<PlainT> ed = mult(e, d, this::innerProductOfTwoPublic);
      Matrix<Spdz2kSIntArithmetic<PlainT>> eb = mult(e, matrixTriple.getRight(), this::innerProductWithPublicPart);
      Matrix<Spdz2kSIntArithmetic<PlainT>> ad = mult(matrixTriple.getLeft(), d, (x, y) -> innerProductWithPublicPart(y, x));
      Matrix<Spdz2kSIntArithmetic<PlainT>> ab = matrixTriple.getProduct();
      out = new Matrix<>(epsilon.getHeight(), delta.getWidth(), i -> {
        ArrayList<DRes<SInt>> row = new ArrayList<>(delta.getWidth());
        ArrayList<Spdz2kSIntArithmetic<PlainT>> temp1 = eb.getRow(i);
        ArrayList<Spdz2kSIntArithmetic<PlainT>> temp2 = ad.getRow(i);
        ArrayList<Spdz2kSIntArithmetic<PlainT>> temp3 = ab.getRow(i);
        ArrayList<PlainT> temp4 = ed.getRow(i);
        for (int j = 0; j < delta.getWidth(); j++) {
          row.add(temp1.get(j)
                  .add(temp2.get(j))
                  .add(temp3.get(j))
                  .addConstant(temp4.get(j), macKeyShare, factory.zero(), resourcePool.getMyId() == 1));
        }
        return row;
      });
      return EvaluationStatus.IS_DONE;
    }
  }

  private void serializeAndSend(Network network) {
    for (int i = 0; i < epsilon.getHeight(); i++) {
      ArrayList<Spdz2kSIntArithmetic<PlainT>> row = epsilon.getRow(i);
      for (int j = 0; j < epsilon.getWidth(); j++) {
        Spdz2kSIntArithmetic<PlainT> openedSecret = row.get(j);
        // send
        network.sendToAll(openedSecret.serializeShare());
      }
    }
    for (int i = 0; i < delta.getHeight(); i++) {
      ArrayList<Spdz2kSIntArithmetic<PlainT>> row = delta.getRow(i);
      for (int j = 0; j < delta.getWidth(); j++) {
        Spdz2kSIntArithmetic<PlainT> openedSecret = row.get(j);
        // send
        network.sendToAll(openedSecret.serializeShare());
      }
    }
  }

  private Pair<Matrix<PlainT>, Matrix<PlainT>> receiveAndReconstruct(Network network, Spdz2kResourcePool<PlainT> resourcePool) {
    OpenedValueStore<Spdz2kSIntArithmetic<PlainT>, PlainT> openedValueStore = resourcePool.getOpenedValueStore();
    ByteSerializer<PlainT> serializer = resourcePool.getPlainSerializer();
    ArrayList<ArrayList<PlainT>> eList = new ArrayList<>(epsilon.getHeight());
    ArrayList<ArrayList<PlainT>> dList = new ArrayList<>(delta.getHeight());
    for (int i = 0; i < epsilon.getHeight(); i++) {
      ArrayList<PlainT> tempList = new ArrayList<>(epsilon.getWidth());
      for (int j = 0; j < epsilon.getWidth(); j++) {
        List<PlainT> shares = serializer.deserializeList(network.receiveFromAll());
        PlainT opened = UInt.sum(shares);
        openedValueStore.pushOpenedValue(epsilon.getRow(i).get(j), opened);
        tempList.add(opened);
      }
      eList.add(tempList);
    }
    for (int i = 0; i < delta.getHeight(); i++) {
      ArrayList<PlainT> tempList = new ArrayList<>(delta.getWidth());
      for (int j = 0; j < delta.getWidth(); j++) {
        List<PlainT> shares = serializer.deserializeList(network.receiveFromAll());
        PlainT opened = UInt.sum(shares);
        openedValueStore.pushOpenedValue(delta.getRow(i).get(j), opened);
        tempList.add(opened);
      }
      dList.add(tempList);
    }
    return new Pair<>(new Matrix<>(epsilon.getHeight(), epsilon.getWidth(), eList), new Matrix<>(delta.getHeight(), delta.getWidth(), dList));
  }

  private Spdz2kSIntArithmetic<PlainT> innerProductWithPublicPart(List<PlainT> a, List<Spdz2kSIntArithmetic<PlainT>> b) {
    if (a.size() != b.size()) {
      throw new IllegalArgumentException("Vectors must have same size");
    }
    Spdz2kSIntArithmetic<PlainT> product = b.get(0).multiply(a.get(0));
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

  private Matrix<Spdz2kSIntArithmetic<PlainT>> sub(Matrix<Spdz2kSIntArithmetic<PlainT>> a, Matrix<Spdz2kSIntArithmetic<PlainT>> b) {
    Matrix<Spdz2kSIntArithmetic<PlainT>> result = new Matrix<>(a.getHeight(), a.getWidth(), i -> {
      ArrayList<Spdz2kSIntArithmetic<PlainT>> row = new ArrayList<>(a.getWidth());
      List<Spdz2kSIntArithmetic<PlainT>> rowA = a.getRow(i);
      List<Spdz2kSIntArithmetic<PlainT>> rowB = b.getRow(i);
      for (int j = 0; j < a.getWidth(); j++) {
        row.add(rowA.get(j).subtract(rowB.get(j)));
      }
      return row;
    });
    return result;
  }

  @Override
  public Matrix<DRes<SInt>> out() {
    return out;
  }

}
