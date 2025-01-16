package dk.alexandra.fresco.suite.mdsonic.protocols.natives;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.BooleanSerializer;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.util.SBoolPair;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;

import java.util.ArrayList;
import java.util.List;

/**
 * Native protocol for computing one layer of PPA
 * <p> (p,g) = (p2,g2) * (p1,g1) = (p1 \and p2, g2 \xor (p2 \and g1)) </p>
 */
public class MdsonicCarryProtocolOpt<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>> extends
        MdsonicNativeProtocol<List<SBoolPair>, PlainT, SecretP> {
  private final List<SBoolPair> bits;  // little-endian
  private List<MdsonicWRBitTriple<SecretP, MdsonicASBoolBoolean<SecretP>>> triples;
  private List<MdsonicASBoolBoolean<SecretP>> epsilons;
  private List<MdsonicASBoolBoolean<SecretP>> deltas;
  private List<MdsonicASBoolBoolean<SecretP>> packedSend;
  private List<Boolean> openEpsilons;
  private List<Boolean> openDeltas;
  private List<Boolean> packedOpen;
  private List<SBoolPair> carried;

  public MdsonicCarryProtocolOpt(List<SBoolPair> bits) {
    this.bits = bits;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    SecretP macKeyShareBoolean = resourcePool.getDataSupplier().getSecretSharedKeyBoolean();
    MdsonicGFFactory<SecretP> factoryBoolean = resourcePool.getBooleanFactory();
    if (round == 0) {
      triples = new ArrayList<>(bits.size() / 2);
      epsilons = new ArrayList<>(bits.size() / 2);
      deltas = new ArrayList<>(bits.size());
      packedSend = new ArrayList<>(bits.size());
      openEpsilons = new ArrayList<>(bits.size() / 2);
      openDeltas = new ArrayList<>(bits.size());
      packedOpen = new ArrayList<>(bits.size());
      carried = new ArrayList<>(bits.size() / 2);

      for (int i = 0; i < bits.size() / 2; i++) {
        // WR bit triples
        triples.add(resourcePool.getDataSupplier().getNextWRBitTripleShares());

        // little-endian
        SBoolPair left = bits.get(2 * i);  // (p1, g1)
        SBoolPair right = bits.get(2 * i + 1);  // (p2, g2)

        MdsonicWRBitTriple<SecretP, MdsonicASBoolBoolean<SecretP>> wrBitTriple = triples.get(i);

        MdsonicASBoolBoolean<SecretP> p1 = (MdsonicASBoolBoolean<SecretP>) left.getFirst().out();
        MdsonicASBoolBoolean<SecretP> g1 = (MdsonicASBoolBoolean<SecretP>) left.getSecond().out();
        MdsonicASBoolBoolean<SecretP> p2 = (MdsonicASBoolBoolean<SecretP>) right.getFirst().out();

        // p2 * g1
        epsilons.add(p2.xor(wrBitTriple.getLeft()));
        deltas.add(g1.xor(wrBitTriple.getRight1()));

        // p2 * p1
        deltas.add(p1.xor(wrBitTriple.getRight2()));  // len(deltas) == 2 * len(epsilons)

//        if (i == 0) {
//          SBoolPair left = bits.get(0);  // (p1, g1), p1 == null
//          SBoolPair right = bits.get(1);  // (p2, g2)
//
//          MdsonicASBoolBoolean<SecretP> g1 = (MdsonicASBoolBoolean<SecretP>) left.getSecond().out();
//          MdsonicASBoolBoolean<SecretP> p2 = (MdsonicASBoolBoolean<SecretP>) right.getFirst().out();
//
//          MdsonicWRBitTriple<SecretP, MdsonicASBoolBoolean<SecretP>> wrBitTriple = triples.get(i);
//          // p2 * g1
//          epsilons.add(p2.xor(wrBitTriple.getLeft()));
//          deltas.add(g1.xor(wrBitTriple.getRight1()));
//        } else {
//          // little-endian
//          SBoolPair left = bits.get(2 * i);  // (p1, g1)
//          SBoolPair right = bits.get(2 * i + 1);  // (p2, g2)
//
//          MdsonicWRBitTriple<SecretP, MdsonicASBoolBoolean<SecretP>> wrBitTriple = triples.get(i);
//
//          MdsonicASBoolBoolean<SecretP> p1 = (MdsonicASBoolBoolean<SecretP>) left.getFirst().out();
//          MdsonicASBoolBoolean<SecretP> g1 = (MdsonicASBoolBoolean<SecretP>) left.getSecond().out();
//          MdsonicASBoolBoolean<SecretP> p2 = (MdsonicASBoolBoolean<SecretP>) right.getFirst().out();
//
//          // p2 * g1
//          epsilons.add(p2.xor(wrBitTriple.getLeft()));
//          deltas.add(g1.xor(wrBitTriple.getRight1()));
//
//          // p2 * p1
//          deltas.add(p1.xor(wrBitTriple.getRight2()));  // len(deltas) == 2 * len(epsilons)
//        }
      }
      serializeAndSend(network, epsilons, deltas);
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      receiveAndReconstruct(network, resourcePool.getNoOfParties());

      OpenedValueStore<MdsonicASBoolBoolean<SecretP>, Boolean> openedBooleanValueStore = resourcePool.getOpenedBooleanValueStore();
      openedBooleanValueStore.pushOpenedValues(packedSend, packedOpen);

      openEpsilons = packedOpen.subList(0, epsilons.size());
      openDeltas = packedOpen.subList(epsilons.size(), packedOpen.size());

      for (int i = 0; i < bits.size() / 2; i++) {
        MdsonicWRBitTriple<SecretP, MdsonicASBoolBoolean<SecretP>> wrBitTriple = triples.get(i);

        boolean e = openEpsilons.get(i);
        boolean p2g1D = openDeltas.get(2 * i);
        boolean p2p1D = openDeltas.get(2 * i + 1);

        MdsonicASBoolBoolean<SecretP> p = andAfterReceive(e, p2p1D, wrBitTriple.getLeft(), wrBitTriple.getRight2(), wrBitTriple.getProduct2(), macKeyShareBoolean, factoryBoolean, resourcePool.getMyId());

        MdsonicASBoolBoolean<SecretP> g2 = (MdsonicASBoolBoolean<SecretP>) (bits.get(2 * i + 1).getSecond().out());

        MdsonicASBoolBoolean<SecretP> g = andAfterReceive(e, p2g1D, wrBitTriple.getLeft(), wrBitTriple.getRight1(), wrBitTriple.getProduct1(), macKeyShareBoolean, factoryBoolean, resourcePool.getMyId()).xor(g2);
        carried.add(new SBoolPair(p, g));

//        if (i == 0) {
//          boolean e = openEpsilons.get(0);
//          boolean p2g1D = openDeltas.get(0);
//
//          MdsonicASBoolBoolean<SecretP> g2 = (MdsonicASBoolBoolean<SecretP>) (bits.get(1).getSecond().out());
//
//          MdsonicASBoolBoolean<SecretP> g = andAfterReceive(e, p2g1D, wrBitTriple.getLeft(), wrBitTriple.getRight1(), wrBitTriple.getProduct1(), macKeyShareBoolean, factoryBoolean, resourcePool.getMyId()).xor(g2);
//          carried.add(new SBoolPair(null, g));
//        } else {
//          boolean e = openEpsilons.get(i);
//          boolean p2g1D = openDeltas.get(2 * i - 1);
//          boolean p2p1D = openDeltas.get(2 * i);
//
//          MdsonicASBoolBoolean<SecretP> p = andAfterReceive(e, p2p1D, wrBitTriple.getLeft(), wrBitTriple.getRight2(), wrBitTriple.getProduct2(), macKeyShareBoolean, factoryBoolean, resourcePool.getMyId());
//
//          MdsonicASBoolBoolean<SecretP> g2 = (MdsonicASBoolBoolean<SecretP>) (bits.get(2 * i + 1).getSecond().out());
//
//          MdsonicASBoolBoolean<SecretP> g = andAfterReceive(e, p2g1D, wrBitTriple.getLeft(), wrBitTriple.getRight1(), wrBitTriple.getProduct1(), macKeyShareBoolean, factoryBoolean, resourcePool.getMyId()).xor(g2);
//          carried.add(new SBoolPair(p, g));
//        }
      }
      // if we have an odd number of elements the last pair can just be taken directly from the input
      if (bits.size() % 2 != 0) {
        carried.add(bits.get(bits.size() - 1));
      }
      return EvaluationStatus.IS_DONE;
    }
  }

  private MdsonicASBoolBoolean<SecretP> andAfterReceive(
          boolean e,
          boolean d,
          MdsonicASBoolBoolean<SecretP> left,
          MdsonicASBoolBoolean<SecretP> right,
          MdsonicASBoolBoolean<SecretP> product,
          SecretP macKeyShare,
          MdsonicGFFactory<SecretP> factory,
          int myId) {
    boolean ed = (e & d);
    // compute [prod] = [c] XOR epsilon AND [b] XOR delta AND [a] XOR epsilon AND delta
    return product
            .xor(right.and(e))
            .xor(left.and(d))
            .xorOpen(ed, macKeyShare, factory.zero(), myId == 1);
  }

  /**
   * Retrieves shares for epsilons and deltas and reconstructs each.
   */
  private void receiveAndReconstruct(Network network, int noOfParties) {
    byte[] rawRecv = network.receive(1);

    for (int i = 0; i < packedSend.size(); i++) {
      int currentByteIdx = i / Byte.SIZE;
      int bitIndexWithinByte = i % Byte.SIZE;
      boolean d = BooleanSerializer.fromBytes((byte) ((rawRecv[currentByteIdx] >>> bitIndexWithinByte) & 1));
      packedOpen.add(d);
    }

    for (int i = 2; i <= noOfParties; i++) {
      rawRecv = network.receive(i);

      for (int j = 0; j < packedSend.size(); j++) {
        int currentByteIdx = j / Byte.SIZE;
        int bitIndexWithinByte = j % Byte.SIZE;

        packedOpen.set(j, (packedOpen.get(j) ^ (
                BooleanSerializer.fromBytes((byte) ((rawRecv[currentByteIdx] >>> bitIndexWithinByte) & 1))
        )));

      }
    }
  }

  /**
   * Serializes and sends epsilon and delta values.
   */
  private void serializeAndSend(Network network, List<MdsonicASBoolBoolean<SecretP>> epsilons,
                                List<MdsonicASBoolBoolean<SecretP>> deltas) {
    packedSend.addAll(epsilons);
    packedSend.addAll(deltas);
    int numOpen = packedSend.size();
    int numBytes = numOpen / Byte.SIZE;
    if (numOpen % 8 != 0) {
      numBytes++;
    }
    byte[] sendBytes = new byte[numBytes];

    for (int i = 0; i < numOpen; i++) {
      int currentByteIdx = i / Byte.SIZE;
      int bitIndexWithinByte = i % Byte.SIZE;

      int serializedDelta = BooleanSerializer.toBytes(packedSend.get(i).getShare());
      sendBytes[currentByteIdx] |= ((serializedDelta << bitIndexWithinByte) & (1 << bitIndexWithinByte));
    }
    network.sendToAll(sendBytes);
  }

  @Override
  public List<SBoolPair> out() {
    return carried;
  }

}
