package dk.alexandra.fresco.suite.mdsonic.protocols.natives.ASS;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.BooleanSerializer;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.util.SBoolPair;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;
import dk.alexandra.fresco.suite.mdsonic.protocols.natives.MdsonicNativeProtocol;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;

import java.util.ArrayList;
import java.util.List;

/**
 * Native protocol for computing one layer of PPA
 * <p> (p,g) = (p2,g2) * (p1,g1) = (p1 \and p2, g2 \xor (p2 \and g1)) </p>
 */
public class MdsonicCarryProtocolASS<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>> extends
        MdsonicNativeProtocol<List<SBoolPair>, PlainT, SecretP> {

  private final boolean useMaskedEvaluation = false;
  private final List<SBoolPair> bits;
  private List<MdsonicBitTriple<SecretP, MdsonicASBoolBoolean<SecretP>>> triples;
  private List<MdsonicASBoolBoolean<SecretP>> epsilons;
  private List<MdsonicASBoolBoolean<SecretP>> deltas;
  private List<Boolean> openEpsilons;
  private List<Boolean> openDeltas;
  private List<SBoolPair> carried;

  public MdsonicCarryProtocolASS(List<SBoolPair> bits) {
    this.bits = bits;
  }

  public MdsonicCarryProtocolASS(List<SBoolPair> bits, boolean useMaskedEvaluation) {
    this.bits = bits;
    if (useMaskedEvaluation) {
      throw new IllegalArgumentException("Don't support masked evaluation now");
    }
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    SecretP macKeyShareBoolean = resourcePool.getDataSupplier().getSecretSharedKeyBoolean();
    MdsonicGFFactory<SecretP> factoryBoolean = resourcePool.getBooleanFactory();
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

        // High to low, such that the index of the highest bit is 0
        SBoolPair left = bits.get(2 * i + 1);
        SBoolPair right = bits.get(2 * i);

        // no wire reuse
        MdsonicBitTriple<SecretP, MdsonicASBoolBoolean<SecretP>> p1p2Triple = triples.get(2 * i + 1);
        MdsonicBitTriple<SecretP, MdsonicASBoolBoolean<SecretP>> p2g1Triple = triples.get(2 * i);

        MdsonicASBoolBoolean<SecretP> p1 = (MdsonicASBoolBoolean<SecretP>) left.getFirst().out();
        MdsonicASBoolBoolean<SecretP> g1 = (MdsonicASBoolBoolean<SecretP>) left.getSecond().out();
        MdsonicASBoolBoolean<SecretP> p2 = (MdsonicASBoolBoolean<SecretP>) right.getFirst().out();

        // p2 * g1
        epsilons.add(p2.xor(p2g1Triple.getLeft()));
        deltas.add(g1.xor(p2g1Triple.getRight()));

        // p1 * p2
        epsilons.add(p1.xor(p1p2Triple.getLeft()));
        deltas.add(p2.xor(p1p2Triple.getRight()));
      }

      serializeAndSend(network, epsilons, deltas);
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      receiveAndReconstruct(network, resourcePool.getNoOfParties());

      OpenedValueStore<MdsonicASBoolBoolean<SecretP>, Boolean> openedBooleanValueStore = resourcePool.getOpenedBooleanValueStore();
      openedBooleanValueStore.pushOpenedValues(epsilons, openEpsilons);
      openedBooleanValueStore.pushOpenedValues(deltas, openDeltas);

      for (int i = 0; i < bits.size() / 2; i++) {
        MdsonicBitTriple<SecretP, MdsonicASBoolBoolean<SecretP>> p1p2Triple = triples.get(2 * i + 1);
        MdsonicBitTriple<SecretP, MdsonicASBoolBoolean<SecretP>> p2g1Triple = triples.get(2 * i);

        boolean p1p2E = openEpsilons.get(2 * i + 1);
        boolean p2g1E = openEpsilons.get(2 * i);
        boolean p1p2D = openDeltas.get(2 * i + 1);
        boolean p2g1D = openDeltas.get(2 * i);

        MdsonicASBoolBoolean<SecretP> p = andAfterReceive(p1p2E, p1p2D, p1p2Triple, macKeyShareBoolean, factoryBoolean, resourcePool.getMyId());

        MdsonicASBoolBoolean<SecretP> g2 = (MdsonicASBoolBoolean<SecretP>) (bits.get(2 * i).getSecond().out());

        MdsonicASBoolBoolean<SecretP> g = andAfterReceive(p2g1E, p2g1D, p2g1Triple, macKeyShareBoolean, factoryBoolean, resourcePool.getMyId()).xor(g2);
        carried.add(new SBoolPair(p, g));
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
          MdsonicBitTriple<SecretP, MdsonicASBoolBoolean<SecretP>> triple,
          SecretP macKeyShare,
          MdsonicGFFactory<SecretP> factory,
          int myId) {
    boolean ed = (e & d);
    // compute [prod] = [c] XOR epsilon AND [b] XOR delta AND [a] XOR epsilon AND delta
    MdsonicASBoolBoolean<SecretP> tripleLeft = triple.getLeft();
    MdsonicASBoolBoolean<SecretP> tripleRight = triple.getRight();
    MdsonicASBoolBoolean<SecretP> tripleProduct = triple.getProduct();
    return tripleProduct
            .xor(tripleRight.and(e))
            .xor(tripleLeft.and(d))
            .xorOpen(ed, macKeyShare, factory.zero(), myId == 1);
  }

  /**
   * Retrieves shares for epsilons and deltas and reconstructs each.
   */
  private void receiveAndReconstruct(Network network, int noOfParties) {
    byte[] rawEpsilons = network.receive(1);
    byte[] rawDeltas = network.receive(1);

    for (int i = 0; i < epsilons.size(); i++) {
      int currentByteIdx = i / Byte.SIZE;
      int bitIndexWithinByte = i % Byte.SIZE;
      boolean e = BooleanSerializer.fromBytes((byte) ((rawEpsilons[currentByteIdx] >>> bitIndexWithinByte) & 1));
      openEpsilons.add(e);
      boolean d = BooleanSerializer.fromBytes((byte) ((rawDeltas[currentByteIdx] >>> bitIndexWithinByte) & 1));
      openDeltas.add(d);
    }

    for (int i = 2; i <= noOfParties; i++) {
      rawEpsilons = network.receive(i);
      rawDeltas = network.receive(i);

      for (int j = 0; j < epsilons.size(); j++) {
        int currentByteIdx = j / Byte.SIZE;
        int bitIndexWithinByte = j % Byte.SIZE;

        openEpsilons.set(j, (openEpsilons.get(j) ^ (
                BooleanSerializer.fromBytes((byte) ((rawEpsilons[currentByteIdx] >>> bitIndexWithinByte) & 1))
        )));
        openDeltas.set(j, (openDeltas.get(j) ^ (
                BooleanSerializer.fromBytes((byte) ((rawDeltas[currentByteIdx] >>> bitIndexWithinByte) & 1))
        )));
      }
    }
  }

  /**
   * Serializes and sends epsilon and delta values.
   */
  private void serializeAndSend(Network network, List<MdsonicASBoolBoolean<SecretP>> epsilons,
                                List<MdsonicASBoolBoolean<SecretP>> deltas) {
    int numOpen = epsilons.size();
    int numBytes = numOpen / Byte.SIZE;
    if (numOpen % 8 != 0) {
      numBytes++;
    }
    byte[] epsilonBytes = new byte[numBytes];
    byte[] deltaBytes = new byte[numBytes];

    for (int i = 0; i < epsilons.size(); i++) {
      int currentByteIdx = i / Byte.SIZE;
      int bitIndexWithinByte = i % Byte.SIZE;

      int serializedEpsilon = BooleanSerializer.toBytes(epsilons.get(i).getShare());
      epsilonBytes[currentByteIdx] |= ((serializedEpsilon << bitIndexWithinByte) & (1 << bitIndexWithinByte));

      int serializedDelta = BooleanSerializer.toBytes(deltas.get(i).getShare());
      deltaBytes[currentByteIdx] |= ((serializedDelta << bitIndexWithinByte) & (1 << bitIndexWithinByte));
    }
    network.sendToAll(epsilonBytes);
    network.sendToAll(deltaBytes);
  }

  @Override
  public List<SBoolPair> out() {
    return carried;
  }

}
