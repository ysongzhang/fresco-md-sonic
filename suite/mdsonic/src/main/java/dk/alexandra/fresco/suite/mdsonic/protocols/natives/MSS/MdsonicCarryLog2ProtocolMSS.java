package dk.alexandra.fresco.suite.mdsonic.protocols.natives.MSS;

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
 * Native protocol for computing one layer of PPA, log2 rounds, little-endian
 * <p> (p,g) = (p2,g2) * (p1,g1) = (p1 \and p2, g2 \xor (p2 \and g1)) </p>
 * <p> p2,p1,g1:MSS. g2:ASS</p>
 */
public class MdsonicCarryLog2ProtocolMSS<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>> extends
        MdsonicNativeProtocol<List<SBoolPair>, PlainT, SecretP> {
  private final List<SBoolPair> bits;  // little-endian
  private MdsonicASBoolBoolean<SecretP> innerProduct;
  private boolean crossOpen;
  private List<MdsonicASBoolBoolean<SecretP>> gRemainValues;

  // ASS to MSS:
  private List<MdsonicASBoolBoolean<SecretP>> openedSecrets;
  private List<MdsonicASBoolBoolean<SecretP>> maskedSecrets;
  private List<Boolean> openedList;

  // result
  private List<SBoolPair> carried;

  public MdsonicCarryLog2ProtocolMSS(List<SBoolPair> bits) {
    this.bits = bits;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    SecretP macKeyShareBoolean = resourcePool.getDataSupplier().getSecretSharedKeyBoolean();
    MdsonicGFFactory<SecretP> factoryBoolean = resourcePool.getBooleanFactory();
    if (round == 0) {
      carried = new ArrayList<>(bits.size() / 2);
      gRemainValues = new ArrayList<>(bits.size() / 2);
      maskedSecrets = new ArrayList<>(3 * bits.size() / 4);
      openedSecrets = new ArrayList<>(3 * bits.size() / 4);
      openedList = new ArrayList<>(3 * bits.size() / 4);

      boolean flag = false;
      for (int i = 0; i < bits.size() / 2; i++) {

        if (i == 0) {
          // Only g
          SBoolPair left = bits.get(0);  // (p1, g1), p1 == null
          SBoolPair right = bits.get(1);  // (p2, g2)

          MdsonicMSBoolBoolean<SecretP> g1 = (MdsonicMSBoolBoolean<SecretP>) left.getSecond().out();
          MdsonicMSBoolBoolean<SecretP> p2 = (MdsonicMSBoolBoolean<SecretP>) right.getFirst().out();
          MdsonicASBoolBoolean<SecretP> g2 = (MdsonicASBoolBoolean<SecretP>) right.getSecond().out();

          // p2 * g1
          innerProduct = resourcePool.getDataSupplier().getNextBitTripleProductShare();
          crossOpen = (p2.getOpened() & g1.getOpened());
          MdsonicASBoolBoolean<SecretP> p2g1Prod = innerProduct.xorOpen(crossOpen, macKeyShareBoolean, factoryBoolean.zero(), resourcePool.getMyId() == 1)
                  .xor(p2.getMaskedSecret().and(g1.getOpened()))
                  .xor(g1.getMaskedSecret().and(p2.getOpened()));
          MdsonicASBoolBoolean<SecretP> g = g2.xor(p2g1Prod);
          if (bits.size() == 2) {
            carried.add(new SBoolPair(null, g));  // ASS
            return EvaluationStatus.IS_DONE;
          } else {
            // ASS to MSS
            MdsonicASBoolBoolean<SecretP> masked = resourcePool.getDataSupplier().getNextBitShare();
            maskedSecrets.add(masked);
            openedSecrets.add(g.xor(masked));
          }
        } else {
          // little-endian
          SBoolPair left = bits.get(2 * i);  // (p1, g1)
          SBoolPair right = bits.get(2 * i + 1);  // (p2, g2)

          MdsonicMSBoolBoolean<SecretP> p1 = (MdsonicMSBoolBoolean<SecretP>) left.getFirst().out();
          MdsonicMSBoolBoolean<SecretP> g1 = (MdsonicMSBoolBoolean<SecretP>) left.getSecond().out();
          MdsonicMSBoolBoolean<SecretP> p2 = (MdsonicMSBoolBoolean<SecretP>) right.getFirst().out();
          MdsonicASBoolBoolean<SecretP> g2 = (MdsonicASBoolBoolean<SecretP>) right.getSecond().out();

          // p2 * g1
          innerProduct = resourcePool.getDataSupplier().getNextBitTripleProductShare();
          crossOpen = (p2.getOpened() & g1.getOpened());
          MdsonicASBoolBoolean<SecretP> p2g1Prod = innerProduct.xorOpen(crossOpen, macKeyShareBoolean, factoryBoolean.zero(), resourcePool.getMyId() == 1)
                  .xor(p2.getMaskedSecret().and(g1.getOpened()))
                  .xor(g1.getMaskedSecret().and(p2.getOpened()));
          MdsonicASBoolBoolean<SecretP> g = g2.xor(p2g1Prod);

          // p1 * p2 = p
          innerProduct = resourcePool.getDataSupplier().getNextBitTripleProductShare();
          crossOpen = (p1.getOpened() & p2.getOpened());
          MdsonicASBoolBoolean<SecretP> p = innerProduct.xorOpen(crossOpen, macKeyShareBoolean, factoryBoolean.zero(), resourcePool.getMyId() == 1)
                  .xor(p1.getMaskedSecret().and(p2.getOpened()))
                  .xor(p2.getMaskedSecret().and(p1.getOpened()));

          // first p and then g
          MdsonicASBoolBoolean<SecretP> masked = resourcePool.getDataSupplier().getNextBitShare();
          maskedSecrets.add(masked);
          openedSecrets.add(p.xor(masked));

          if (flag) {  // ASS to MSS
            masked = resourcePool.getDataSupplier().getNextBitShare();
            maskedSecrets.add(masked);
            openedSecrets.add(g.xor(masked));
          } else {  // remain ASS
            gRemainValues.add(g);
          }
          flag = !flag;
        }
      }

      // ASS to MSS
      serializeAndSend(network, openedSecrets);
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      receiveAndReconstruct(network, resourcePool.getNoOfParties());

      OpenedValueStore<MdsonicASBoolBoolean<SecretP>, Boolean> openedBooleanValueStore = resourcePool.getOpenedBooleanValueStore();
      openedBooleanValueStore.pushOpenedValues(openedSecrets, openedList);

      // the first node
      boolean opened = openedList.get(0);
      MdsonicMSBoolBoolean<SecretP> g = new MdsonicMSBoolBoolean<>(maskedSecrets.get(0), opened);
      carried.add(new SBoolPair(null, g));

      for (int i = 0; i < openedSecrets.size() / 3; i++) {
        opened = openedList.get(3 * i + 1);
        MdsonicMSBoolBoolean<SecretP> p = new MdsonicMSBoolBoolean<>(maskedSecrets.get(3 * i + 1), opened);
        carried.add(new SBoolPair(p, gRemainValues.get(i)));
        opened = openedList.get(3 * i + 2);
        p = new MdsonicMSBoolBoolean<>(maskedSecrets.get(3 * i + 2), opened);
        opened = openedList.get(3 * i + 3);
        g = new MdsonicMSBoolBoolean<>(maskedSecrets.get(3 * i + 3), opened);
        carried.add(new SBoolPair(p, g));
      }
      if ((openedSecrets.size() - 1) % 3 != 0) {
        opened = openedList.get(openedList.size() - 1);
        MdsonicMSBoolBoolean<SecretP> p = new MdsonicMSBoolBoolean<>(maskedSecrets.get(openedList.size() - 1), opened);
        carried.add(new SBoolPair(p, gRemainValues.get(gRemainValues.size() - 1)));
      }
      // if we have an odd number of elements the last pair can just be taken directly from the input
      if (bits.size() % 2 != 0) {
        carried.add(bits.get(bits.size() - 1));
      }
      return EvaluationStatus.IS_DONE;
    }
  }


  /**
   * Retrieves shares for epsilons and deltas and reconstructs each.
   */
  private void receiveAndReconstruct(Network network, int noOfParties) {
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
  private void serializeAndSend(Network network, List<MdsonicASBoolBoolean<SecretP>> sendList) {
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
  public List<SBoolPair> out() {
    return carried;
  }

}
