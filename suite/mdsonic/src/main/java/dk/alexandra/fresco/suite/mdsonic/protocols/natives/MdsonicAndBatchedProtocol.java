package dk.alexandra.fresco.suite.mdsonic.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.BooleanSerializer;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;

import java.util.ArrayList;
import java.util.List;

/**
 * Native protocol for computing logical AND of two values in boolean form.
 */
public class MdsonicAndBatchedProtocol<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>> extends
        MdsonicNativeProtocol<List<DRes<SBool>>, PlainT, SecretP> {

  private final boolean useMaskedEvaluation;

  // Input:
  private DRes<List<DRes<SBool>>> bitsADef;
  private DRes<List<DRes<SBool>>> bitsBDef;
  // TODO final LinkedLists?

  // ASS:
  private List<MdsonicBitTriple<SecretP, MdsonicASBoolBoolean<SecretP>>> triples;
  private List<MdsonicASBoolBoolean<SecretP>> epsilons;
  private List<MdsonicASBoolBoolean<SecretP>> deltas;
  private List<Boolean> openEpsilons;
  private List<Boolean> openDeltas;

  // MSS:
  private MdsonicASBoolBoolean<SecretP> innerProduct;
  private boolean crossOpen;

  // ASS to MSS:
  private List<MdsonicASBoolBoolean<SecretP>> maskedSecrets;
  private List<MdsonicASBoolBoolean<SecretP>> openedSecrets;
  private List<Boolean> openedList;

  // Output:
  private List<DRes<SBool>> products;

  public MdsonicAndBatchedProtocol(DRes<List<DRes<SBool>>> bitsA,
                                   DRes<List<DRes<SBool>>> bitsB) {
    this.bitsADef = bitsA;
    this.bitsBDef = bitsB;
    this.useMaskedEvaluation = false;
  }

  public MdsonicAndBatchedProtocol(DRes<List<DRes<SBool>>> bitsA,
                                   DRes<List<DRes<SBool>>> bitsB, boolean useMaskedEvaluation) {
    this.bitsADef = bitsA;
    this.bitsBDef = bitsB;
    this.useMaskedEvaluation = useMaskedEvaluation;
    this.maskedSecrets = null;
  }

  public MdsonicAndBatchedProtocol(DRes<List<DRes<SBool>>> bitsA,
                                   DRes<List<DRes<SBool>>> bitsB, boolean useMaskedEvaluation, List<SBool> maskedSecrets) {
    this.bitsADef = bitsA;
    this.bitsBDef = bitsB;
    this.useMaskedEvaluation = useMaskedEvaluation;
    if (maskedSecrets == null) {
      this.maskedSecrets = null;
    } else {
      this.maskedSecrets = new ArrayList<>(maskedSecrets.size());
      for (int i = 0; i <= maskedSecrets.size(); i++) {
        this.maskedSecrets.add((MdsonicASBoolBoolean<SecretP>) maskedSecrets.get(i));
      }
    }
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    List<DRes<SBool>> bitsA = bitsADef.out();
    List<DRes<SBool>> bitsB = bitsBDef.out();
    if (bitsA.size() != bitsB.size()) {
      throw new IllegalArgumentException("Lists must be same size");
    }
    SecretP macKeyShareBoolean = resourcePool.getDataSupplier().getSecretSharedKeyBoolean();
    MdsonicGFFactory<SecretP> factoryBoolean = resourcePool.getBooleanFactory();
    if (useMaskedEvaluation) {
      if (round == 0) {
        // Init
        openedList = new ArrayList<>(bitsA.size());
        openedSecrets = new ArrayList<>(bitsA.size());
        products = new ArrayList<>(bitsA.size());
        if (maskedSecrets == null) {
          maskedSecrets = new ArrayList<>(bitsA.size());
          for (int i = 0; i < bitsA.size(); i++) {
            maskedSecrets.add(resourcePool.getDataSupplier().getNextBitShare());
          }
        } else {
          if (bitsA.size() != maskedSecrets.size()) {
            throw new IllegalArgumentException("Lists must be same size [masked secrets]");
          }
        }
        // Computation: MSS -> ASS
        for (int i = 0; i < bitsA.size(); i++) {
          MdsonicMSBoolBoolean<SecretP> leftBit = (MdsonicMSBoolBoolean<SecretP>) bitsA.get(i).out();
          MdsonicMSBoolBoolean<SecretP> rightBit = (MdsonicMSBoolBoolean<SecretP>) bitsB.get(i).out();
          innerProduct = resourcePool.getDataSupplier().getNextBitTripleProductShare();
          crossOpen = (leftBit.getOpened() & rightBit.getOpened());
          MdsonicASBoolBoolean<SecretP> prod = innerProduct.xorOpen(crossOpen, macKeyShareBoolean, factoryBoolean.zero(), resourcePool.getMyId() == 1)
                  .xor(leftBit.getMaskedSecret().and(rightBit.getOpened()))
                  .xor(rightBit.getMaskedSecret().and(leftBit.getOpened()));
          // ASS to MSS:
          openedSecrets.add(prod.xor(maskedSecrets.get(i)));
        }
        serializeAndSendMSS(network, openedSecrets);
        return EvaluationStatus.HAS_MORE_ROUNDS;
      } else {
        receiveAndReconstructMSS(network, resourcePool.getNoOfParties());

        OpenedValueStore<MdsonicASBoolBoolean<SecretP>, Boolean> openedBooleanValueStore = resourcePool.getOpenedBooleanValueStore();
        openedBooleanValueStore.pushOpenedValues(openedSecrets, openedList);

        for (int i = 0; i < bitsA.size(); i++) {
          boolean opened = openedList.get(i);
          MdsonicMSBoolBoolean<SecretP> prod = new MdsonicMSBoolBoolean<>(maskedSecrets.get(i), opened);
          products.add(prod);
        }
        return EvaluationStatus.IS_DONE;
      }
    } else {
      if (round == 0) {
        triples = new ArrayList<>(bitsA.size());
        epsilons = new ArrayList<>(bitsA.size());
        deltas = new ArrayList<>(bitsA.size());
        openEpsilons = new ArrayList<>(bitsA.size());
        openDeltas = new ArrayList<>(bitsA.size());
        products = new ArrayList<>(bitsA.size());

        for (int i = 0; i < bitsA.size(); i++) {
          MdsonicBitTriple<SecretP, MdsonicASBoolBoolean<SecretP>> triple = resourcePool
                  .getDataSupplier()
                  .getNextBitTripleShares();
          triples.add(triple);

          MdsonicASBoolBoolean<SecretP> left = (MdsonicASBoolBoolean<SecretP>) bitsA.get(i).out();
          MdsonicASBoolBoolean<SecretP> right = (MdsonicASBoolBoolean<SecretP>) bitsB.get(i).out();

          epsilons.add(left.xor(triple.getLeft()));
          deltas.add(right.xor(triple.getRight()));
        }

        serializeAndSend(network, epsilons, deltas);
        return EvaluationStatus.HAS_MORE_ROUNDS;
      } else {
        receiveAndReconstruct(network, resourcePool.getNoOfParties());

        OpenedValueStore<MdsonicASBoolBoolean<SecretP>, Boolean> openedBooleanValueStore = resourcePool.getOpenedBooleanValueStore();
        openedBooleanValueStore.pushOpenedValues(epsilons, openEpsilons);
        openedBooleanValueStore.pushOpenedValues(deltas, openDeltas);

        for (int i = 0; i < bitsA.size(); i++) {
          MdsonicBitTriple<SecretP, MdsonicASBoolBoolean<SecretP>> triple = triples.get(i);

          boolean e = openEpsilons.get(i);
          boolean d = openDeltas.get(i);
          MdsonicASBoolBoolean<SecretP> prod = andAfterReceive(e, d, triple, macKeyShareBoolean, factoryBoolean, resourcePool.getMyId());

          products.add(prod);
        }
        return EvaluationStatus.IS_DONE;
      }
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
        .xorOpen(ed, macKeyShare, factory.zero(),myId == 1);
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

  private void receiveAndReconstructMSS(Network network, int noOfParties) {
    byte[] rawRecv = network.receive(1);

    for (int i = 0; i < openedSecrets.size(); i++) {
      int currentByteIdx = i / Byte.SIZE;
      int bitIndexWithinByte = i % Byte.SIZE;
      boolean r = BooleanSerializer.fromBytes((byte) ((rawRecv[currentByteIdx] >>> bitIndexWithinByte) & 1));
      openedList.add(r);
    }

    for (int i = 2; i <= noOfParties; i++) {
      rawRecv = network.receive(i);

      for (int j = 0; j < openedSecrets.size(); j++) {
        int currentByteIdx = j / Byte.SIZE;
        int bitIndexWithinByte = j % Byte.SIZE;

        openedList.set(j, (openedList.get(j) ^ (
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

  private void serializeAndSendMSS(Network network, List<MdsonicASBoolBoolean<SecretP>> sendList) {
    int numOpen = sendList.size();
    int numBytes = numOpen / Byte.SIZE;
    if (numOpen % 8 != 0) {
      numBytes++;
    }
    byte[] sendBytes = new byte[numBytes];

    for (int i = 0; i < sendList.size(); i++) {
      int currentByteIdx = i / Byte.SIZE;
      int bitIndexWithinByte = i % Byte.SIZE;

      int serializedSend = BooleanSerializer.toBytes(sendList.get(i).getShare());
      sendBytes[currentByteIdx] |= ((serializedSend << bitIndexWithinByte) & (1 << bitIndexWithinByte));
    }
    network.sendToAll(sendBytes);
  }

  @Override
  public List<DRes<SBool>> out() {
    return products;
  }

}
