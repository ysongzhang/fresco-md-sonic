package dk.alexandra.fresco.suite.mdml.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.ByteAndBitConverter;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdml.datatypes.*;
import dk.alexandra.fresco.suite.mdml.resource.MdmlResourcePool;

import java.util.ArrayList;
import java.util.List;

/**
 * Native protocol for computing K-ary logical OR of a list of values.
 */
public class MdmlOrOfListProtocol<PlainT extends MdmlCompUInt<?, ?, PlainT>> extends
        MdmlNativeProtocol<SInt, PlainT> {

  private final DRes<List<DRes<SInt>>> bitsDef;
  private SInt res;
  private List<DRes<SInt>> nextRound;
  private List<DRes<SInt>> bitsA;
  private List<DRes<SInt>> bitsB;
  private List<MdmlTriple<PlainT, MdmlSIntBoolean<PlainT>>> triples;
  private List<MdmlSIntBoolean<PlainT>> epsilons;
  private List<MdmlSIntBoolean<PlainT>> deltas;
  private List<PlainT> openEpsilons;
  private List<PlainT> openDeltas;
  private SInt extraBit;

  public MdmlOrOfListProtocol(DRes<List<DRes<SInt>>> bits) {
    this.bitsDef = bits;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdmlResourcePool<PlainT> resourcePool,
      Network network) {
    PlainT macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
    MdmlCompUIntFactory<PlainT> factory = resourcePool.getFactory();

    if (round % 2 == 0) {
      if (nextRound == null) {
        nextRound = bitsDef.out();
      }
      final int nextRoundSize = nextRound.size();
      if (nextRoundSize == 1) {
        res = nextRound.get(0).out();
        return EvaluationStatus.IS_DONE;
      }

      bitsA = new ArrayList<>(nextRoundSize / 2);
      bitsB = new ArrayList<>(nextRoundSize / 2);
      for (int i = 0; i < nextRoundSize - 1; i += 2) {
        bitsA.add(nextRound.get(i));
        bitsB.add(nextRound.get(i + 1));
      }
      triples = new ArrayList<>(nextRoundSize / 2);
      epsilons = new ArrayList<>(nextRoundSize / 2);
      deltas = new ArrayList<>(nextRoundSize / 2);
      openEpsilons = new ArrayList<>(nextRoundSize / 2);
      openDeltas = new ArrayList<>(nextRoundSize / 2);
      // reset next round
      final boolean isOdd = nextRoundSize % 2 != 0;
      if (isOdd) {
        extraBit = nextRound.get(nextRoundSize - 1).out();
      } else {
        extraBit = null;
      }
      nextRound = new ArrayList<>(nextRoundSize / 2);
      for (int i = 0; i < bitsA.size(); i++) {
        MdmlTriple<PlainT, MdmlSIntBoolean<PlainT>> triple = resourcePool
            .getDataSupplier()
            .getNextBitTripleShares();
        triples.add(triple);

        MdmlSIntBoolean<PlainT> left = factory.toMdmlSIntBoolean(bitsA.get(i));
        MdmlSIntBoolean<PlainT> right = factory.toMdmlSIntBoolean(bitsB.get(i));

        // fix bug
        epsilons.add(left.xor(triple.getLeft()));
        deltas.add(right.xor(triple.getRight()));
      }
      serializeAndSend(network, factory, epsilons, deltas);
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      receiveAndReconstruct(network, factory, resourcePool.getNoOfParties());

      OpenedValueStore<MdmlASIntArithmetic<PlainT>, PlainT> openedValueStore = resourcePool
          .getOpenedValueStore();

      for (int i = 0; i < bitsA.size(); i++) {
        MdmlTriple<PlainT, MdmlSIntBoolean<PlainT>> triple = triples.get(i);

        PlainT e = openEpsilons.get(i);
        openedValueStore.pushOpenedValue(epsilons.get(i).asArithmetic(), e.toArithmeticRep());
        PlainT d = openDeltas.get(i);
        openedValueStore.pushOpenedValue(deltas.get(i).asArithmetic(), d.toArithmeticRep());

        MdmlSIntBoolean<PlainT> prod = andAfterReceive(e, d, triple, macKeyShare, factory,
            resourcePool.getMyId());
        MdmlSIntBoolean<PlainT> xored = factory.toMdmlSIntBoolean(bitsA.get(i))
            .xor(factory.toMdmlSIntBoolean(bitsB.get(i)));

        nextRound.add(prod.xor(xored));
      }
      if (extraBit != null) {
        nextRound.add(extraBit);
      }
      return EvaluationStatus.HAS_MORE_ROUNDS;
    }
  }

  private MdmlSIntBoolean<PlainT> andAfterReceive(
      PlainT e,
      PlainT d,
      MdmlTriple<PlainT, MdmlSIntBoolean<PlainT>> triple,
      PlainT macKeyShare,
      MdmlCompUIntFactory<PlainT> factory,
      int myId) {
    int eBit = e.bitValue();
    int dBit = d.bitValue();
    PlainT ed = e.multiply(d);
    // compute [prod] = [c] XOR epsilon AND [b] XOR delta AND [a] XOR epsilon AND delta
    MdmlSIntBoolean<PlainT> tripleLeft = triple.getLeft();
    MdmlSIntBoolean<PlainT> tripleRight = triple.getRight();
    MdmlSIntBoolean<PlainT> tripleProduct = triple.getProduct();
    return tripleProduct
        .xor(tripleRight.and(eBit))
        .xor(tripleLeft.and(dBit))
        .xorOpen(ed,
            macKeyShare,
            factory.zero().toBitRep(),
            myId == 1);
  }

  /**
   * Retrieves shares for epsilons and deltas and reconstructs each.
   */
  private void receiveAndReconstruct(Network network, MdmlCompUIntFactory<PlainT> factory,
      int noOfParties) {
    byte[] rawEpsilons = network.receive(1);  // Length = numOpen * sLengthByte + numBytes
    byte[] rawDeltas = network.receive(1);

    int numOpen = epsilons.size();
    int sLengthByte = factory.getHighBitLength() / Byte.SIZE;

    for (int i = 0; i < numOpen; i++) {
      int currentByteIdx = i / Byte.SIZE + numOpen * sLengthByte;
      int bitIndexWithinByte = i % Byte.SIZE;

      long eHigh = ByteAndBitConverter.toLong(rawEpsilons, (rawEpsilons.length - (i + 1) * sLengthByte), sLengthByte);
      int eBit = ((rawEpsilons[currentByteIdx] >>> bitIndexWithinByte) & 1);
      PlainT e = factory.fromBitAndHigh(eHigh, eBit);
      openEpsilons.add(e);
      long dHigh = ByteAndBitConverter.toLong(rawDeltas, (rawDeltas.length - (i + 1) * sLengthByte), sLengthByte);
      int dBit = ((rawDeltas[currentByteIdx] >>> bitIndexWithinByte) & 1);
      PlainT d = factory.fromBitAndHigh(dHigh, dBit);
      openDeltas.add(d);
    }

    for (int i = 2; i <= noOfParties; i++) {
      rawEpsilons = network.receive(i);
      rawDeltas = network.receive(i);

      for (int j = 0; j < numOpen; j++) {
        int currentByteIdx = j / Byte.SIZE + numOpen * sLengthByte;
        int bitIndexWithinByte = j % Byte.SIZE;

        long eHigh = ByteAndBitConverter.toLong(rawEpsilons, (rawEpsilons.length - (j + 1) * sLengthByte), sLengthByte);
        int eBit = ((rawEpsilons[currentByteIdx] >>> bitIndexWithinByte) & 1);
        PlainT e = factory.fromBitAndHigh(eHigh, eBit);
        long dHigh = ByteAndBitConverter.toLong(rawDeltas, (rawDeltas.length - (j + 1) * sLengthByte), sLengthByte);
        int dBit = ((rawDeltas[currentByteIdx] >>> bitIndexWithinByte) & 1);
        PlainT d = factory.fromBitAndHigh(dHigh, dBit);
        openEpsilons.set(j, openEpsilons.get(j).add(e));
        openDeltas.set(j, openDeltas.get(j).add(d));
      }
    }
  }

  /**
   * Serializes and sends epsilon and delta values.
   */
  private void serializeAndSend(Network network, MdmlCompUIntFactory<PlainT> factory,
      List<MdmlSIntBoolean<PlainT>> epsilons,
      List<MdmlSIntBoolean<PlainT>> deltas) {
    int numOpen = epsilons.size();
    int numBytes = numOpen / Byte.SIZE;
    if (numOpen % 8 != 0) {
      numBytes++;
    }

    int sLengthByte = factory.getHighBitLength() / Byte.SIZE;
    byte[] epsilonBytes = new byte[numOpen * sLengthByte + numBytes];
    byte[] deltaBytes = new byte[numOpen * sLengthByte + numBytes];
    byte[] epsilonByte;
    byte[] deltaByte;

    for (int i = 0; i < numOpen; i++) {
      int currentByteIdx = i / Byte.SIZE + numOpen * sLengthByte;
      int bitIndexWithinByte = i % Byte.SIZE;

      epsilonByte = epsilons.get(i).serializeShare();
      System.arraycopy(epsilonByte, 0, epsilonBytes, i * sLengthByte, sLengthByte);
      int serializedEpsilon = (epsilonByte[sLengthByte] & 1);
      epsilonBytes[currentByteIdx] |= ((serializedEpsilon << bitIndexWithinByte) & (1 << bitIndexWithinByte));

      deltaByte = deltas.get(i).serializeShare();
      System.arraycopy(deltaByte, 0, deltaBytes, i * sLengthByte, sLengthByte);
      int serializedDelta = (deltaByte[sLengthByte] & 1);
      deltaBytes[currentByteIdx] |= ((serializedDelta << bitIndexWithinByte)
              & (1 << bitIndexWithinByte));
    }
    network.sendToAll(epsilonBytes);
    network.sendToAll(deltaBytes);
  }

  @Override
  public SInt out() {
    return res;
  }

}
