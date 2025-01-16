package dk.alexandra.fresco.suite.mdsonic.protocols.natives.ASS;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.util.Pair;
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
public class MdsonicFixedMatrixProductProtocolASS<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>>
    extends MdsonicNativeProtocol<Matrix<DRes<SInt>>, PlainT, SecretP> {

  // Input:
  private final DRes<Matrix<DRes<SReal>>> x;
  private final DRes<Matrix<DRes<SReal>>> y;

  // Computations:
  private MdsonicMatrixTriple<PlainT, MdsonicASIntArithmetic<PlainT>> matrixTriple;
  private Matrix<MdsonicASIntArithmetic<PlainT>> epsilon;
  private Matrix<MdsonicASIntArithmetic<PlainT>> delta;

  // Output:
  private Matrix<DRes<SInt>> out;

  /**
   * Creates new {@link MdsonicFixedMatrixProductProtocolASS}.
   */

  public MdsonicFixedMatrixProductProtocolASS(DRes<Matrix<DRes<SReal>>> x, DRes<Matrix<DRes<SReal>>> y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool, Network network) {
    MdsonicCompUIntFactory<PlainT> factory = resourcePool.getFactory();
    if (round == 0) {
      Matrix<DRes<SReal>> xOut = x.out();
      Matrix<DRes<SReal>> yOut = y.out();
      if (xOut.getWidth() != yOut.getHeight()) {
        throw new IllegalArgumentException(
                "Matrice sizes does not match - " + xOut.getWidth() + " != " + yOut.getHeight());
      }
      matrixTriple = resourcePool.getDataSupplier().getNextMatrixTripleShares(xOut.getHeight(), yOut.getHeight(), yOut.getWidth());

      // Resolve inputs
      int xHeight = xOut.getHeight();
      int xWidth = xOut.getWidth();
      int yHeight = yOut.getHeight();
      int yWidth = yOut.getWidth();
      MdsonicASIntArithmetic<PlainT>[][] xArray = new MdsonicASIntArithmetic[xHeight][xWidth];
      MdsonicASIntArithmetic<PlainT>[][] yArray = new MdsonicASIntArithmetic[yHeight][yWidth];
      for (int i = 0; i < xHeight; i++) {
        ArrayList<DRes<SReal>> xRow = xOut.getRow(i);
        for (int j = 0; j < xWidth; j++) {
          MdsonicASIntArithmetic<PlainT> xTemp = factory.toMdsonicASIntArithmetic(((SFixed) xRow.get(j).out()).getSInt());
          xArray[i][j] = xTemp;
        }
      }
      for (int i = 0; i < yHeight; i++) {
        ArrayList<DRes<SReal>> yRow = yOut.getRow(i);
        for (int j = 0; j < yWidth; j++) {
          MdsonicASIntArithmetic<PlainT> yTemp = factory.toMdsonicASIntArithmetic(((SFixed) yRow.get(j).out()).getSInt());
          yArray[i][j] = yTemp;
        }
      }
      Matrix<MdsonicASIntArithmetic<PlainT>> xMatrix = new Matrix<>(xOut.getHeight(), xOut.getWidth(), xArray);
      Matrix<MdsonicASIntArithmetic<PlainT>> yMatrix = new Matrix<>(yOut.getHeight(), yOut.getWidth(), yArray);

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
      Matrix<MdsonicASIntArithmetic<PlainT>> eb = mult(e, matrixTriple.getRight(), this::innerProductWithPublicPart);
      Matrix<MdsonicASIntArithmetic<PlainT>> ad = mult(matrixTriple.getLeft(), d, (x, y) -> innerProductWithPublicPart(y, x));
      Matrix<MdsonicASIntArithmetic<PlainT>> ab = matrixTriple.getProduct();

      out = new Matrix<>(epsilon.getHeight(), delta.getWidth(), i -> {
        ArrayList<DRes<SInt>> row = new ArrayList<>(delta.getWidth());
        ArrayList<PlainT> temp4 = ed.getRow(i);
        for (int j = 0; j < delta.getWidth(); j++) {
          row.add(eb.getRow(i).get(j)
                  .add(ad.getRow(i).get(j))
                  .add(ab.getRow(i).get(j))
                  .addConstant(temp4.get(j), macKeyShare, factory.zero(), resourcePool.getMyId() == 1));
        }
        return row;
      });
      return EvaluationStatus.IS_DONE;
    }
  }

  private void serializeAndSend(Network network) {
    for (int i = 0; i < epsilon.getHeight(); i++) {
      ArrayList<MdsonicASIntArithmetic<PlainT>> row = epsilon.getRow(i);
      for (int j = 0; j < epsilon.getWidth(); j++) {
        MdsonicASIntArithmetic<PlainT> openedSecret = row.get(j);
        // send
        network.sendToAll(openedSecret.serializeShare());
      }
    }
    for (int i = 0; i < delta.getHeight(); i++) {
      ArrayList<MdsonicASIntArithmetic<PlainT>> row = delta.getRow(i);
      for (int j = 0; j < delta.getWidth(); j++) {
        MdsonicASIntArithmetic<PlainT> openedSecret = row.get(j);
        // send
        network.sendToAll(openedSecret.serializeShare());
      }
    }
  }

  private Pair<Matrix<PlainT>, Matrix<PlainT>> receiveAndReconstruct(Network network, MdsonicResourcePool<PlainT, SecretP> resourcePool) {
    OpenedValueStore<MdsonicASIntArithmetic<PlainT>, PlainT> openedValueStore = resourcePool.getOpenedValueStore();
    ByteSerializer<PlainT> serializer = resourcePool.getPlainSerializer();
    ArrayList<ArrayList<PlainT>> eList = new ArrayList<>(epsilon.getHeight());
    ArrayList<ArrayList<PlainT>> dList = new ArrayList<>(delta.getHeight());
    for (int i = 0; i < epsilon.getHeight(); i++) {
      ArrayList<PlainT> tempList = new ArrayList<>(epsilon.getWidth());
      for (int j = 0; j < epsilon.getWidth(); j++) {
        List<PlainT> shares = serializer.deserializeList(network.receiveFromAll());
        PlainT opened = MdsonicUInt.sum(shares);
        openedValueStore.pushOpenedValue(epsilon.getRow(i).get(j), opened);
        tempList.add(opened);
      }
      eList.add(tempList);
    }
    for (int i = 0; i < delta.getHeight(); i++) {
      ArrayList<PlainT> tempList = new ArrayList<>(delta.getWidth());
      for (int j = 0; j < delta.getWidth(); j++) {
        List<PlainT> shares = serializer.deserializeList(network.receiveFromAll());
        PlainT opened = MdsonicUInt.sum(shares);
        openedValueStore.pushOpenedValue(delta.getRow(i).get(j), opened);
        tempList.add(opened);
      }
      dList.add(tempList);
    }
    return new Pair<>(new Matrix<>(epsilon.getHeight(), epsilon.getWidth(), eList), new Matrix<>(delta.getHeight(), delta.getWidth(), dList));
  }

  private MdsonicASIntArithmetic<PlainT> innerProductWithPublicPart(List<PlainT> a, List<MdsonicASIntArithmetic<PlainT>> b) {
    if (a.size() != b.size()) {
      throw new IllegalArgumentException("Vectors must have same size");
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

  private Matrix<MdsonicASIntArithmetic<PlainT>> sub(Matrix<MdsonicASIntArithmetic<PlainT>> a, Matrix<MdsonicASIntArithmetic<PlainT>> b) {
    Matrix<MdsonicASIntArithmetic<PlainT>> result = new Matrix<>(a.getHeight(), a.getWidth(), i -> {
      ArrayList<MdsonicASIntArithmetic<PlainT>> row = new ArrayList<>(a.getWidth());
      List<MdsonicASIntArithmetic<PlainT>> rowA = a.getRow(i);
      List<MdsonicASIntArithmetic<PlainT>> rowB = b.getRow(i);
      for (int j = 0; j < a.getWidth(); j++) {
        row.add(rowA.get(j).subtract(rowB.get(j)));
      }
      return row;
    });
    return result;
  }

  private Matrix<MdsonicASIntArithmetic<PlainT>> add(Matrix<MdsonicASIntArithmetic<PlainT>> a, Matrix<MdsonicASIntArithmetic<PlainT>> b) {
    Matrix<MdsonicASIntArithmetic<PlainT>> result = new Matrix<>(a.getHeight(), a.getWidth(), i -> {
      ArrayList<MdsonicASIntArithmetic<PlainT>> row = new ArrayList<>(a.getWidth());
      List<MdsonicASIntArithmetic<PlainT>> rowA = a.getRow(i);
      List<MdsonicASIntArithmetic<PlainT>> rowB = b.getRow(i);
      for (int j = 0; j < a.getWidth(); j++) {
        row.add(rowA.get(j).add(rowB.get(j)));
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
