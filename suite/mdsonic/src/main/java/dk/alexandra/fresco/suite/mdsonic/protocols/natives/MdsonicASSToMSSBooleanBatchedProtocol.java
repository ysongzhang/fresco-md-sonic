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

public class MdsonicASSToMSSBooleanBatchedProtocol<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>> extends
        MdsonicNativeProtocol<List<DRes<SBool>>, PlainT, SecretP> {

    private DRes<List<DRes<SBool>>> inputs;
    private List<MdsonicASBoolBoolean<SecretP>> maskedSecrets;
    private List<MdsonicASBoolBoolean<SecretP>> openedSecrets;
    private List<Boolean> openedList;
    private List<DRes<SBool>> outputs;

    public MdsonicASSToMSSBooleanBatchedProtocol(DRes<List<DRes<SBool>>> inputs) {
        this.inputs = inputs;
        this.maskedSecrets = null;
    }

    public MdsonicASSToMSSBooleanBatchedProtocol(DRes<List<DRes<SBool>>> inputs, DRes<List<DRes<SBool>>> maskedSecrets) {
        this.inputs = inputs;
        List<DRes<SBool>> masked = maskedSecrets.out();
        this.maskedSecrets = new ArrayList<>(masked.size());
        for (DRes<SBool> anInner : masked) {
            this.maskedSecrets.add((MdsonicASBoolBoolean<SecretP>) anInner.out());
        }
    }

    public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                    Network network) {
        if (round == 0) {
            List<DRes<SBool>> inputsOut = inputs.out();
            // Init
            openedList = new ArrayList<>(inputsOut.size());
            openedSecrets = new ArrayList<>(inputsOut.size());
            outputs = new ArrayList<>(inputsOut.size());
            if (maskedSecrets == null) {
                maskedSecrets = new ArrayList<>(inputsOut.size());
                for (int i = 0; i < inputsOut.size(); i++) {
                    maskedSecrets.add(resourcePool.getDataSupplier().getNextBitShare());
                }
            } else {
                if (inputsOut.size() != maskedSecrets.size()) {
                    throw new IllegalArgumentException("Lists must be same size [masked secrets]");
                }
            }

            // Computation: MSS -> ASS
            for (int i = 0; i < inputsOut.size(); i++) {
                MdsonicASBoolBoolean<SecretP> input = (MdsonicASBoolBoolean<SecretP>) inputsOut.get(i).out();
                // ASS to MSS:
                openedSecrets.add(input.xor(maskedSecrets.get(i)));
            }
            serializeAndSendMSS(network, openedSecrets);
            return EvaluationStatus.HAS_MORE_ROUNDS;
        } else {
            receiveAndReconstructMSS(network, resourcePool.getNoOfParties());

            OpenedValueStore<MdsonicASBoolBoolean<SecretP>, Boolean> openedBooleanValueStore = resourcePool.getOpenedBooleanValueStore();
            openedBooleanValueStore.pushOpenedValues(openedSecrets, openedList);

            for (int i = 0; i < openedList.size(); i++) {
                boolean opened = openedList.get(i);
                MdsonicMSBoolBoolean<SecretP> output = new MdsonicMSBoolBoolean<>(maskedSecrets.get(i), opened);
                outputs.add(output);
            }
            return EvaluationStatus.IS_DONE;
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

    public List<DRes<SBool>> out() {
        return outputs;
    }
}
