package dk.alexandra.fresco.suite.mdml.protocols.natives;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.ByteAndBitConverter;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.util.SIntPair;
import dk.alexandra.fresco.suite.mdml.datatypes.*;
import dk.alexandra.fresco.suite.mdml.resource.MdmlResourcePool;

import java.util.ArrayList;
import java.util.List;

/**
 * Native protocol for computing one layer of PPA
 * <p> (p,g) = (p2,g2) * (p1,g1) = (p1 \and p2, g2 \xor (p2 \and g1)) </p>
 */
public class MdmlCarryProtocol<PlainT extends MdmlCompUInt<?, ?, PlainT>> extends
    MdmlNativeProtocol<List<SIntPair>, PlainT> {

  private final List<SIntPair> bits;
  private List<MdmlTriple<PlainT, MdmlSIntBoolean<PlainT>>> triples;
  private List<MdmlSIntBoolean<PlainT>> epsilons;
  private List<MdmlSIntBoolean<PlainT>> deltas;
  private List<PlainT> openEpsilons;
  private List<PlainT> openDeltas;
  private List<SIntPair> carried;

  public MdmlCarryProtocol(List<SIntPair> bits) {
    this.bits = bits;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdmlResourcePool<PlainT> resourcePool,
      Network network) {
    PlainT macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
    MdmlCompUIntFactory<PlainT> factory = resourcePool.getFactory();
    if (round == 0) {
      triples = new ArrayList<>(bits.size());
      epsilons = new ArrayList<>(bits.size());
      deltas = new ArrayList<>(bits.size());
      openEpsilons = new ArrayList<>(bits.size());
      openDeltas = new ArrayList<>(bits.size());
      carried = new ArrayList<>(bits.size() / 2);

      for (int i = 0; i < bits.size() / 2; i++) {
        // two multiplications
        triples.add(resourcePool.getDataSupplier().getNextBitTripleShares());
        triples.add(resourcePool.getDataSupplier().getNextBitTripleShares());

        SIntPair left = bits.get(2 * i + 1);
        SIntPair right = bits.get(2 * i);

        MdmlTriple<PlainT, MdmlSIntBoolean<PlainT>> p1p2Triple = triples.get(2 * i + 1);
        MdmlTriple<PlainT, MdmlSIntBoolean<PlainT>> p2g1Triple = triples.get(2 * i);

        MdmlSIntBoolean<PlainT> p1 = factory.toMdmlSIntBoolean(left.getFirst());
        MdmlSIntBoolean<PlainT> g1 = factory.toMdmlSIntBoolean(left.getSecond());
        MdmlSIntBoolean<PlainT> p2 = factory.toMdmlSIntBoolean(right.getFirst());

        // p2 * g1
        epsilons.add(factory.toMdmlSIntBoolean(p2).xor(p2g1Triple.getLeft()));
        deltas.add(factory.toMdmlSIntBoolean(g1).xor(p2g1Triple.getRight()));

        // p1 * p2
        epsilons.add(factory.toMdmlSIntBoolean(p1).xor(p1p2Triple.getLeft()));
        deltas.add(factory.toMdmlSIntBoolean(p2).xor(p1p2Triple.getRight()));
      }

      serializeAndSend(network, factory, epsilons, deltas);
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      receiveAndReconstruct(network, factory, resourcePool.getNoOfParties());

      OpenedValueStore<MdmlASIntArithmetic<PlainT>, PlainT> openedValueStore = resourcePool
          .getOpenedValueStore();

      for (int i = 0; i < bits.size() / 2; i++) {
        MdmlTriple<PlainT, MdmlSIntBoolean<PlainT>> p1p2Triple = triples.get(2 * i + 1);
        MdmlTriple<PlainT, MdmlSIntBoolean<PlainT>> p2g1Triple = triples.get(2 * i);

        PlainT p1p2E = openEpsilons.get(2 * i + 1);
        openedValueStore.pushOpenedValue(
            epsilons.get(2 * i + 1).asArithmetic(),
            p1p2E.toArithmeticRep()
        );
        PlainT p2g1E = openEpsilons.get(2 * i);
        openedValueStore.pushOpenedValue(
            epsilons.get(2 * i).asArithmetic(),
            p2g1E.toArithmeticRep()
        );

        PlainT p1p2D = openDeltas.get(2 * i + 1);
        openedValueStore.pushOpenedValue(
            deltas.get(2 * i + 1).asArithmetic(),
            p1p2D.toArithmeticRep()
        );
        PlainT p2g1D = openDeltas.get(2 * i);
        openedValueStore.pushOpenedValue(
            deltas.get(2 * i).asArithmetic(),
            p2g1D.toArithmeticRep()
        );
        MdmlSIntBoolean<PlainT> p = andAfterReceive(p1p2E, p1p2D, p1p2Triple, macKeyShare,
            factory,
            resourcePool.getMyId());

        MdmlSIntBoolean<PlainT> g2 = factory.toMdmlSIntBoolean(bits.get(2 * i).getSecond());

        MdmlSIntBoolean<PlainT> g = andAfterReceive(p2g1E, p2g1D, p2g1Triple, macKeyShare,
            factory,
            resourcePool.getMyId()).xor(g2);
        carried.add(new SIntPair(p, g));
      }
      // if we have an odd number of elements the last pair can just be taken directly from the input
      if (bits.size() % 2 != 0) {
        carried.add(bits.get(bits.size() - 1));
      }
      return EvaluationStatus.IS_DONE;
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
  public List<SIntPair> out() {
    return carried;
  }

}
