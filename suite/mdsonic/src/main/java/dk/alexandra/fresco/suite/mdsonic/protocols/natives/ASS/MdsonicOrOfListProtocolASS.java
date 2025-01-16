package dk.alexandra.fresco.suite.mdsonic.protocols.natives.ASS;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.BooleanSerializer;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;
import dk.alexandra.fresco.suite.mdsonic.protocols.natives.MdsonicNativeProtocol;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;

import java.util.ArrayList;
import java.util.List;

/**
 * Native protocol for computing K-ary logical OR of a list of values.
 */
public class MdsonicOrOfListProtocolASS<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>> extends
        MdsonicNativeProtocol<SBool, PlainT, SecretP> {

  private final boolean useMaskedEvaluation = false;

  // Input:
  private final DRes<List<DRes<SBool>>> bitsDef;

  // Output:
  private SBool res;

  // Intermediate:
  private List<DRes<SBool>> nextRound;
  private List<DRes<SBool>> bitsA;
  private List<DRes<SBool>> bitsB;
  private SBool extraBit;

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

  public MdsonicOrOfListProtocolASS(DRes<List<DRes<SBool>>> bits) {
    this.bitsDef = bits;
//    this.useMaskedEvaluation = false;
  }

  public MdsonicOrOfListProtocolASS(DRes<List<DRes<SBool>>> bits, boolean useMaskedEvaluation) {
    this.bitsDef = bits;
//    this.useMaskedEvaluation = useMaskedEvaluation;
    if (useMaskedEvaluation) {
      throw new IllegalArgumentException("Don't support masked evaluation now");
    }
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
//    System.out.println("Running round " + round);

    SecretP macKeyShareBoolean = resourcePool.getDataSupplier().getSecretSharedKeyBoolean();
    MdsonicGFFactory<SecretP> factoryBoolean = resourcePool.getBooleanFactory();
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

      // reset next round
      final boolean isOdd = nextRoundSize % 2 != 0;
      if (isOdd) {
        extraBit = nextRound.get(nextRoundSize - 1).out();
      } else {
        extraBit = null;
      }
      nextRound = new ArrayList<>(nextRoundSize / 2);

      if (useMaskedEvaluation) {
        maskedSecrets = new ArrayList<>(nextRoundSize / 2);
        openedList = new ArrayList<>(nextRoundSize / 2);
        openedSecrets = new ArrayList<>(nextRoundSize / 2);
        // Computation: MSS -> ASS
        for (int i = 0; i < bitsA.size(); i++) {
          maskedSecrets.add(resourcePool.getDataSupplier().getNextBitShare());

          MdsonicMSBoolBoolean<SecretP> leftBit = (MdsonicMSBoolBoolean<SecretP>) bitsA.get(i).out();
          MdsonicMSBoolBoolean<SecretP> rightBit = (MdsonicMSBoolBoolean<SecretP>) bitsB.get(i).out();
          innerProduct = resourcePool.getDataSupplier().getNextBitTripleProductShare();
          crossOpen = (leftBit.getOpened() & rightBit.getOpened());
          MdsonicASBoolBoolean<SecretP> prod = innerProduct.xorOpen(crossOpen, macKeyShareBoolean, factoryBoolean.zero(), resourcePool.getMyId() == 1)
                  .xor(leftBit.getMaskedSecret().and(rightBit.getOpened()))
                  .xor(rightBit.getMaskedSecret().and(leftBit.getOpened()));
          MdsonicASBoolBoolean<SecretP> xored = leftBit.getMaskedSecret().xor(rightBit.getMaskedSecret())
                  .xorOpen(leftBit.getOpened(), macKeyShareBoolean, factoryBoolean.zero(),resourcePool.getMyId() == 1)
                  .xorOpen(rightBit.getOpened(), macKeyShareBoolean, factoryBoolean.zero(),resourcePool.getMyId() == 1);
          MdsonicASBoolBoolean<SecretP> result = prod.xor(xored);
          // ASS to MSS:
          openedSecrets.add(result.xor(maskedSecrets.get(i)));
        }
        serializeAndSendMSS(network, openedSecrets);
        return EvaluationStatus.HAS_MORE_ROUNDS;
      } else {
        triples = new ArrayList<>(nextRoundSize / 2);
        epsilons = new ArrayList<>(nextRoundSize / 2);
        deltas = new ArrayList<>(nextRoundSize / 2);
        openEpsilons = new ArrayList<>(nextRoundSize / 2);
        openDeltas = new ArrayList<>(nextRoundSize / 2);

        for (int i = 0; i < bitsA.size(); i++) {
          MdsonicBitTriple<SecretP, MdsonicASBoolBoolean<SecretP>> triple = resourcePool
                  .getDataSupplier()
                  .getNextBitTripleShares();
          triples.add(triple);

          MdsonicASBoolBoolean<SecretP> left = (MdsonicASBoolBoolean<SecretP>) bitsA.get(i).out();
          MdsonicASBoolBoolean<SecretP> right = (MdsonicASBoolBoolean<SecretP>) bitsB.get(i).out();

          // fix bug
          epsilons.add(left.xor(triple.getLeft()));
          deltas.add(right.xor(triple.getRight()));
        }
        serializeAndSend(network, epsilons, deltas);
        return EvaluationStatus.HAS_MORE_ROUNDS;
      }
    } else {
      OpenedValueStore<MdsonicASBoolBoolean<SecretP>, Boolean> openedBooleanValueStore = resourcePool.getOpenedBooleanValueStore();
      if (useMaskedEvaluation) {
        receiveAndReconstructMSS(network, resourcePool.getNoOfParties());

        openedBooleanValueStore.pushOpenedValues(openedSecrets, openedList);

        for (int i = 0; i < bitsA.size(); i++) {
          boolean opened = openedList.get(i);
          MdsonicMSBoolBoolean<SecretP> prod = new MdsonicMSBoolBoolean<>(maskedSecrets.get(i), opened);
          nextRound.add(prod);
        }
      } else {
        receiveAndReconstruct(network, resourcePool.getNoOfParties());

        openedBooleanValueStore.pushOpenedValues(epsilons, openEpsilons);
        openedBooleanValueStore.pushOpenedValues(deltas, openDeltas);

        for (int i = 0; i < bitsA.size(); i++) {
          MdsonicBitTriple<SecretP, MdsonicASBoolBoolean<SecretP>> triple = triples.get(i);

          boolean e = openEpsilons.get(i);
          boolean d = openDeltas.get(i);
          MdsonicASBoolBoolean<SecretP> prod = andAfterReceive(e, d, triple, macKeyShareBoolean, factoryBoolean, resourcePool.getMyId());
          MdsonicASBoolBoolean<SecretP> leftBit = (MdsonicASBoolBoolean<SecretP>) bitsA.get(i).out();
          MdsonicASBoolBoolean<SecretP> rightBit = (MdsonicASBoolBoolean<SecretP>) bitsB.get(i).out();
          MdsonicASBoolBoolean<SecretP> xored = leftBit.xor(rightBit);

          nextRound.add(prod.xor(xored));
        }
      }

      if (extraBit != null) {
        nextRound.add(extraBit);
      }
      return EvaluationStatus.HAS_MORE_ROUNDS;
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
  public SBool out() {
    return res;
  }

}
