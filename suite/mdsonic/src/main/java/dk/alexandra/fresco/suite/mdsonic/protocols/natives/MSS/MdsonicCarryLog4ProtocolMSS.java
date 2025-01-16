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
public class MdsonicCarryLog4ProtocolMSS<PlainT extends MdsonicCompUInt<?, ?, PlainT>, SecretP extends MdsonicGF<SecretP>> extends
        MdsonicNativeProtocol<List<SBoolPair>, PlainT, SecretP> {
  private final List<SBoolPair> bits;  // little-endian
  private final int bitLength;
  private MdsonicASBoolBoolean<SecretP> innerProduct;
  private boolean crossOpen;
  private List<MdsonicASBoolBoolean<SecretP>> gRemainValues;

  // ASS to MSS:
  private List<MdsonicASBoolBoolean<SecretP>> openedSecrets;
  private List<MdsonicASBoolBoolean<SecretP>> maskedSecrets;
  private List<Boolean> openedList;

  // result
  private List<SBoolPair> carried;

  public MdsonicCarryLog4ProtocolMSS(List<SBoolPair> bits, int bitLength) {
    this.bits = bits;
    this.bitLength = bitLength;
  }

  @Override
  public EvaluationStatus evaluate(int round, MdsonicResourcePool<PlainT, SecretP> resourcePool,
                                                  Network network) {
    SecretP macKeyShareBoolean = resourcePool.getDataSupplier().getSecretSharedKeyBoolean();
    MdsonicGFFactory<SecretP> factoryBoolean = resourcePool.getBooleanFactory();
    innerProduct = resourcePool.getDataSupplier().getNextBitTripleProductShare();
    if (bitLength == 32) {
      if (bits.size() == 8) {
        if (round == 0) {
          carried = new ArrayList<>(bits.size() / 2);
          gRemainValues = new ArrayList<>(bits.size() / 2);
          maskedSecrets = new ArrayList<>(3 * bits.size() / 4);
          openedSecrets = new ArrayList<>(3 * bits.size() / 4);
          openedList = new ArrayList<>(3 * bits.size() / 4);

          for (int i = 0; i < 4; i++) {
            if (i == 0) {
              SBoolPair left = bits.get(0);  // (p1, g1), p1 == null
              SBoolPair right = bits.get(1);  // (p2, g2)

              MdsonicMSBoolBoolean<SecretP> g1 = (MdsonicMSBoolBoolean<SecretP>) left.getSecond().out();
              MdsonicMSBoolBoolean<SecretP> p2 = (MdsonicMSBoolBoolean<SecretP>) right.getFirst().out();
              MdsonicASBoolBoolean<SecretP> g2 = (MdsonicASBoolBoolean<SecretP>) right.getSecond().out();

              // p2 * g1
//              innerProduct = resourcePool.getDataSupplier().getNextBitTripleProductShare();
              crossOpen = (p2.getOpened() & g1.getOpened());
              MdsonicASBoolBoolean<SecretP> p2g1Prod = innerProduct.xorOpen(crossOpen, macKeyShareBoolean, factoryBoolean.zero(), resourcePool.getMyId() == 1)
                      .xor(p2.getMaskedSecret().and(g1.getOpened()))
                      .xor(g1.getMaskedSecret().and(p2.getOpened()));
              MdsonicASBoolBoolean<SecretP> g = g2.xor(p2g1Prod);

              // ASS to MSS
              MdsonicASBoolBoolean<SecretP> masked = resourcePool.getDataSupplier().getNextBitShare();
              maskedSecrets.add(masked);
              openedSecrets.add(g.xor(masked));
            } else {
              // little-endian
              SBoolPair left = bits.get(2 * i);  // (p1, g1)
              SBoolPair right = bits.get(2 * i + 1);  // (p2, g2)

              MdsonicMSBoolBoolean<SecretP> p1 = (MdsonicMSBoolBoolean<SecretP>) left.getFirst().out();
              MdsonicMSBoolBoolean<SecretP> g1 = (MdsonicMSBoolBoolean<SecretP>) left.getSecond().out();
              MdsonicMSBoolBoolean<SecretP> p2 = (MdsonicMSBoolBoolean<SecretP>) right.getFirst().out();
              MdsonicASBoolBoolean<SecretP> g2 = (MdsonicASBoolBoolean<SecretP>) right.getSecond().out();

              // p2 * g1
//              innerProduct = resourcePool.getDataSupplier().getNextBitTripleProductShare();
              crossOpen = (p2.getOpened() & g1.getOpened());
              MdsonicASBoolBoolean<SecretP> p2g1Prod = innerProduct.xorOpen(crossOpen, macKeyShareBoolean, factoryBoolean.zero(), resourcePool.getMyId() == 1)
                      .xor(p2.getMaskedSecret().and(g1.getOpened()))
                      .xor(g1.getMaskedSecret().and(p2.getOpened()));
              MdsonicASBoolBoolean<SecretP> g = g2.xor(p2g1Prod);

              // p1 * p2 = p
//              innerProduct = resourcePool.getDataSupplier().getNextBitTripleProductShare();
              crossOpen = (p1.getOpened() & p2.getOpened());
              MdsonicASBoolBoolean<SecretP> p = innerProduct.xorOpen(crossOpen, macKeyShareBoolean, factoryBoolean.zero(), resourcePool.getMyId() == 1)
                      .xor(p1.getMaskedSecret().and(p2.getOpened()))
                      .xor(p2.getMaskedSecret().and(p1.getOpened()));

              MdsonicASBoolBoolean<SecretP> masked = resourcePool.getDataSupplier().getNextBitShare();
              maskedSecrets.add(masked);
              openedSecrets.add(p.xor(masked));

              if (i != 3) {  // ASS to MSS
                masked = resourcePool.getDataSupplier().getNextBitShare();
                maskedSecrets.add(masked);
                openedSecrets.add(g.xor(masked));
              } else {  // remain ASS
                gRemainValues.add(g);
              }
            }
          }

          // ASS to MSS
          serializeAndSend(network, openedSecrets);
          return EvaluationStatus.HAS_MORE_ROUNDS;
        } else {
          receiveAndReconstruct(network, resourcePool.getNoOfParties());

          OpenedValueStore<MdsonicASBoolBoolean<SecretP>, Boolean> openedBooleanValueStore = resourcePool.getOpenedBooleanValueStore();
          openedBooleanValueStore.pushOpenedValues(openedSecrets, openedList);

          boolean opened = openedList.get(0);
          MdsonicMSBoolBoolean<SecretP> g = new MdsonicMSBoolBoolean<>(maskedSecrets.get(0), opened);
          carried.add(new SBoolPair(null, g));

          for (int i = 0; i < (openedSecrets.size() - 1) / 2; i++) {
            opened = openedList.get(2 * i + 1);
            MdsonicMSBoolBoolean<SecretP> p = new MdsonicMSBoolBoolean<>(maskedSecrets.get(2 * i + 1), opened);
            opened = openedList.get(2 * i + 2);
            g = new MdsonicMSBoolBoolean<>(maskedSecrets.get(2 * i + 2), opened);
            carried.add(new SBoolPair(p, g));
          }
          opened = openedList.get(openedList.size() - 1);
          MdsonicMSBoolBoolean<SecretP> p = new MdsonicMSBoolBoolean<>(maskedSecrets.get(openedList.size() - 1), opened);
          carried.add(new SBoolPair(p, gRemainValues.get(gRemainValues.size() - 1)));
          return EvaluationStatus.IS_DONE;
        }
      } else {  // len == 4
        // return ASS directly
        carried = new ArrayList<>(1);
        SBoolPair pair1 = bits.get(0);  // (p1, g1), p1 == null
        SBoolPair pair2 = bits.get(1);  // (p2, g2)
        SBoolPair pair3 = bits.get(2);  // (p3, g3)
        SBoolPair pair4 = bits.get(3);  // (p4, g4)
        // g4 \xor p4g3 \xor p4p3g2 \xor p4p3p2g1
        MdsonicMSBoolBoolean<SecretP> g1 = (MdsonicMSBoolBoolean<SecretP>) pair1.getSecond().out();
        MdsonicMSBoolBoolean<SecretP> p2 = (MdsonicMSBoolBoolean<SecretP>) pair2.getFirst().out();
        MdsonicMSBoolBoolean<SecretP> g2 = (MdsonicMSBoolBoolean<SecretP>) pair2.getSecond().out();
        MdsonicMSBoolBoolean<SecretP> p3 = (MdsonicMSBoolBoolean<SecretP>) pair3.getFirst().out();
        MdsonicMSBoolBoolean<SecretP> g3 = (MdsonicMSBoolBoolean<SecretP>) pair3.getSecond().out();
        MdsonicMSBoolBoolean<SecretP> p4 = (MdsonicMSBoolBoolean<SecretP>) pair4.getFirst().out();
        MdsonicASBoolBoolean<SecretP> g4 = (MdsonicASBoolBoolean<SecretP>) pair4.getSecond().out();

        // p4 * g3
//        innerProduct = resourcePool.getDataSupplier().getNextBitTripleProductShare();
        crossOpen = (p4.getOpened() & g3.getOpened());
        MdsonicASBoolBoolean<SecretP> p4g3 = innerProduct.xorOpen(crossOpen, macKeyShareBoolean, factoryBoolean.zero(), resourcePool.getMyId() == 1)
                .xor(p4.getMaskedSecret().and(g3.getOpened()))
                .xor(g3.getMaskedSecret().and(p4.getOpened()));

        // p4 * p3 * g2
//        MdsonicASBoolBoolean<SecretP> crossProduct1 = resourcePool.getDataSupplier().getNextBitTripleProductShare();
//        MdsonicASBoolBoolean<SecretP> crossProduct2 = resourcePool.getDataSupplier().getNextBitTripleProductShare();
//        MdsonicASBoolBoolean<SecretP> crossProduct3 = resourcePool.getDataSupplier().getNextBitTripleProductShare();
//        MdsonicASBoolBoolean<SecretP> crossProduct4 = resourcePool.getDataSupplier().getNextBitTripleProductShare();
        boolean temp1 = (p4.getOpened() & p3.getOpened() & g2.getOpened());
        MdsonicASBoolBoolean<SecretP> temp2 = p4.getMaskedSecret().and((p3.getOpened() & g2.getOpened()));
        MdsonicASBoolBoolean<SecretP> temp3 = p3.getMaskedSecret().and((p4.getOpened() & g2.getOpened()));
        MdsonicASBoolBoolean<SecretP> temp4 = g2.getMaskedSecret().and((p4.getOpened() & p3.getOpened()));
        MdsonicASBoolBoolean<SecretP> temp5 = innerProduct.and(g2.getOpened());
        MdsonicASBoolBoolean<SecretP> temp6 = innerProduct.and(p3.getOpened());
        MdsonicASBoolBoolean<SecretP> temp7 = innerProduct.and(p4.getOpened());
        MdsonicASBoolBoolean<SecretP> p4p3g2 = innerProduct
                .xor(temp2)
                .xor(temp3)
                .xor(temp4)
                .xor(temp5)
                .xor(temp6)
                .xor(temp7)
                .xorOpen(temp1, macKeyShareBoolean, factoryBoolean.zero(), resourcePool.getMyId() == 1);

        // p4 * p3 * p2 * g1
//        List<MdsonicASBoolBoolean<SecretP>> crossProducts = new ArrayList<>(11);
//        for (int i = 0; i < 11; i++) {
//          crossProducts.add(resourcePool.getDataSupplier().getNextBitTripleProductShare());
//        }
        temp1 = (p4.getOpened() & p3.getOpened() & p2.getOpened() & g1.getOpened());
        temp2 = p4.getMaskedSecret().and((p3.getOpened() & p2.getOpened() & g1.getOpened()));
        temp3 = p3.getMaskedSecret().and((p4.getOpened() & p2.getOpened() & g1.getOpened()));
        temp4 = p2.getMaskedSecret().and((p4.getOpened() & p3.getOpened() & g1.getOpened()));
        temp5 = g1.getMaskedSecret().and((p4.getOpened() & p3.getOpened() & p2.getOpened()));
        temp6 = innerProduct.and(p2.getOpened() & g1.getOpened());
        temp7 = innerProduct.and(p3.getOpened() & g1.getOpened());
        MdsonicASBoolBoolean<SecretP> temp8 = innerProduct.and(p3.getOpened() & p2.getOpened());
        MdsonicASBoolBoolean<SecretP> temp9 = innerProduct.and(p4.getOpened() & g1.getOpened());
        MdsonicASBoolBoolean<SecretP> temp10 = innerProduct.and(p4.getOpened() & p2.getOpened());
        MdsonicASBoolBoolean<SecretP> temp11 = innerProduct.and(p4.getOpened() & p3.getOpened());
        MdsonicASBoolBoolean<SecretP> temp12 = innerProduct.and(g1.getOpened());
        MdsonicASBoolBoolean<SecretP> temp13 = innerProduct.and(p2.getOpened());
        MdsonicASBoolBoolean<SecretP> temp14 = innerProduct.and(p3.getOpened());
        MdsonicASBoolBoolean<SecretP> temp15 = innerProduct.and(p4.getOpened());
        MdsonicASBoolBoolean<SecretP> p4p3p2g1 = innerProduct
                .xor(temp2).xor(temp3).xor(temp4)
                .xor(temp5).xor(temp6).xor(temp7)
                .xor(temp8).xor(temp9).xor(temp10)
                .xor(temp11).xor(temp12).xor(temp13)
                .xor(temp14).xor(temp15)
                .xorOpen(temp1, macKeyShareBoolean, factoryBoolean.zero(), resourcePool.getMyId() == 1);

        MdsonicASBoolBoolean<SecretP> g = g4.xor(p4g3).xor(p4p3g2).xor(p4p3p2g1);
        carried.add(new SBoolPair(null, g));  // ASS
        return EvaluationStatus.IS_DONE;
      }
    } else {  // bitLength == 64
      if (round == 0) {
        carried = new ArrayList<>(bits.size() / 4);
        gRemainValues = new ArrayList<>(bits.size() / 16);
        maskedSecrets = new ArrayList<>(7 * bits.size() / 16);
        openedSecrets = new ArrayList<>(7 * bits.size() / 16);
        openedList = new ArrayList<>(7 * bits.size() / 16);

        for (int i = 0; i < bits.size() / 4; i++) {

          if (i == 0) {
            SBoolPair pair1 = bits.get(0);  // (p1, g1), p1 == null
            SBoolPair pair2 = bits.get(1);  // (p2, g2)
            SBoolPair pair3 = bits.get(2);  // (p3, g3)
            SBoolPair pair4 = bits.get(3);  // (p4, g4)
            // g = g4 \xor p4g3 \xor p4p3g2 \xor p4p3p2g1
            MdsonicMSBoolBoolean<SecretP> g1 = (MdsonicMSBoolBoolean<SecretP>) pair1.getSecond().out();
            MdsonicMSBoolBoolean<SecretP> p2 = (MdsonicMSBoolBoolean<SecretP>) pair2.getFirst().out();
            MdsonicMSBoolBoolean<SecretP> g2 = (MdsonicMSBoolBoolean<SecretP>) pair2.getSecond().out();
            MdsonicMSBoolBoolean<SecretP> p3 = (MdsonicMSBoolBoolean<SecretP>) pair3.getFirst().out();
            MdsonicMSBoolBoolean<SecretP> g3 = (MdsonicMSBoolBoolean<SecretP>) pair3.getSecond().out();
            MdsonicMSBoolBoolean<SecretP> p4 = (MdsonicMSBoolBoolean<SecretP>) pair4.getFirst().out();
            MdsonicASBoolBoolean<SecretP> g4 = (MdsonicASBoolBoolean<SecretP>) pair4.getSecond().out();

            // p4 * g3
//            innerProduct = resourcePool.getDataSupplier().getNextBitTripleProductShare();
            crossOpen = (p4.getOpened() & g3.getOpened());
            MdsonicASBoolBoolean<SecretP> p4g3 = innerProduct.xorOpen(crossOpen, macKeyShareBoolean, factoryBoolean.zero(), resourcePool.getMyId() == 1)
                    .xor(p4.getMaskedSecret().and(g3.getOpened()))
                    .xor(g3.getMaskedSecret().and(p4.getOpened()));

            // p4 * p3 * g2
//            MdsonicASBoolBoolean<SecretP> crossProduct1 = resourcePool.getDataSupplier().getNextBitTripleProductShare();
//            MdsonicASBoolBoolean<SecretP> crossProduct2 = resourcePool.getDataSupplier().getNextBitTripleProductShare();
//            MdsonicASBoolBoolean<SecretP> crossProduct3 = resourcePool.getDataSupplier().getNextBitTripleProductShare();
//            MdsonicASBoolBoolean<SecretP> crossProduct4 = resourcePool.getDataSupplier().getNextBitTripleProductShare();
            boolean temp1 = (p4.getOpened() & p3.getOpened() & g2.getOpened());
            MdsonicASBoolBoolean<SecretP> temp2 = p4.getMaskedSecret().and((p3.getOpened() & g2.getOpened()));
            MdsonicASBoolBoolean<SecretP> temp3 = p3.getMaskedSecret().and((p4.getOpened() & g2.getOpened()));
            MdsonicASBoolBoolean<SecretP> temp4 = g2.getMaskedSecret().and((p4.getOpened() & p3.getOpened()));
            MdsonicASBoolBoolean<SecretP> temp5 = innerProduct.and(g2.getOpened());
            MdsonicASBoolBoolean<SecretP> temp6 = innerProduct.and(p3.getOpened());
            MdsonicASBoolBoolean<SecretP> temp7 = innerProduct.and(p4.getOpened());
            MdsonicASBoolBoolean<SecretP> p4p3g2 = innerProduct
                    .xor(temp2)
                    .xor(temp3)
                    .xor(temp4)
                    .xor(temp5)
                    .xor(temp6)
                    .xor(temp7)
                    .xorOpen(temp1, macKeyShareBoolean, factoryBoolean.zero(), resourcePool.getMyId() == 1);

            // p4 * p3 * p2 * g1
//            List<MdsonicASBoolBoolean<SecretP>> crossProducts = new ArrayList<>(11);
//            for (int j = 0; j < 11; j++) {
//              crossProducts.add(resourcePool.getDataSupplier().getNextBitTripleProductShare());
//            }
            temp1 = (p4.getOpened() & p3.getOpened() & p2.getOpened() & g1.getOpened());
            temp2 = p4.getMaskedSecret().and((p3.getOpened() & p2.getOpened() & g1.getOpened()));
            temp3 = p3.getMaskedSecret().and((p4.getOpened() & p2.getOpened() & g1.getOpened()));
            temp4 = p2.getMaskedSecret().and((p4.getOpened() & p3.getOpened() & g1.getOpened()));
            temp5 = g1.getMaskedSecret().and((p4.getOpened() & p3.getOpened() & p2.getOpened()));
            temp6 = innerProduct.and(p2.getOpened() & g1.getOpened());
            temp7 = innerProduct.and(p3.getOpened() & g1.getOpened());
            MdsonicASBoolBoolean<SecretP> temp8 = innerProduct.and(p3.getOpened() & p2.getOpened());
            MdsonicASBoolBoolean<SecretP> temp9 = innerProduct.and(p4.getOpened() & g1.getOpened());
            MdsonicASBoolBoolean<SecretP> temp10 = innerProduct.and(p4.getOpened() & p2.getOpened());
            MdsonicASBoolBoolean<SecretP> temp11 = innerProduct.and(p4.getOpened() & p3.getOpened());
            MdsonicASBoolBoolean<SecretP> temp12 = innerProduct.and(g1.getOpened());
            MdsonicASBoolBoolean<SecretP> temp13 = innerProduct.and(p2.getOpened());
            MdsonicASBoolBoolean<SecretP> temp14 = innerProduct.and(p3.getOpened());
            MdsonicASBoolBoolean<SecretP> temp15 = innerProduct.and(p4.getOpened());
            MdsonicASBoolBoolean<SecretP> p4p3p2g1 = innerProduct
                    .xor(temp2).xor(temp3).xor(temp4)
                    .xor(temp5).xor(temp6).xor(temp7)
                    .xor(temp8).xor(temp9).xor(temp10)
                    .xor(temp11).xor(temp12).xor(temp13)
                    .xor(temp14).xor(temp15)
                    .xorOpen(temp1, macKeyShareBoolean, factoryBoolean.zero(), resourcePool.getMyId() == 1);

            MdsonicASBoolBoolean<SecretP> g = g4.xor(p4g3).xor(p4p3g2).xor(p4p3p2g1);

            if (bits.size() == 4) {
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
            SBoolPair pair1 = bits.get(4 * i);  // (p1, g1)
            SBoolPair pair2 = bits.get(4 * i + 1);  // (p2, g2)
            SBoolPair pair3 = bits.get(4 * i + 2);  // (p3, g3)
            SBoolPair pair4 = bits.get(4 * i + 3);  // (p4, g4)

            MdsonicMSBoolBoolean<SecretP> p1 = (MdsonicMSBoolBoolean<SecretP>) pair1.getFirst().out();
            MdsonicMSBoolBoolean<SecretP> g1 = (MdsonicMSBoolBoolean<SecretP>) pair1.getSecond().out();
            MdsonicMSBoolBoolean<SecretP> p2 = (MdsonicMSBoolBoolean<SecretP>) pair2.getFirst().out();
            MdsonicMSBoolBoolean<SecretP> g2 = (MdsonicMSBoolBoolean<SecretP>) pair2.getSecond().out();
            MdsonicMSBoolBoolean<SecretP> p3 = (MdsonicMSBoolBoolean<SecretP>) pair3.getFirst().out();
            MdsonicMSBoolBoolean<SecretP> g3 = (MdsonicMSBoolBoolean<SecretP>) pair3.getSecond().out();
            MdsonicMSBoolBoolean<SecretP> p4 = (MdsonicMSBoolBoolean<SecretP>) pair4.getFirst().out();
            MdsonicASBoolBoolean<SecretP> g4 = (MdsonicASBoolBoolean<SecretP>) pair4.getSecond().out();

            // p4 * g3
//            innerProduct = resourcePool.getDataSupplier().getNextBitTripleProductShare();
            crossOpen = (p4.getOpened() & g3.getOpened());
            MdsonicASBoolBoolean<SecretP> p4g3 = innerProduct.xorOpen(crossOpen, macKeyShareBoolean, factoryBoolean.zero(), resourcePool.getMyId() == 1)
                    .xor(p4.getMaskedSecret().and(g3.getOpened()))
                    .xor(g3.getMaskedSecret().and(p4.getOpened()));

            // p4 * p3 * g2
//            MdsonicASBoolBoolean<SecretP> crossProduct1 = resourcePool.getDataSupplier().getNextBitTripleProductShare();
//            MdsonicASBoolBoolean<SecretP> crossProduct2 = resourcePool.getDataSupplier().getNextBitTripleProductShare();
//            MdsonicASBoolBoolean<SecretP> crossProduct3 = resourcePool.getDataSupplier().getNextBitTripleProductShare();
//            MdsonicASBoolBoolean<SecretP> crossProduct4 = resourcePool.getDataSupplier().getNextBitTripleProductShare();
            boolean temp1 = (p4.getOpened() & p3.getOpened() & g2.getOpened());
            MdsonicASBoolBoolean<SecretP> temp2 = p4.getMaskedSecret().and((p3.getOpened() & g2.getOpened()));
            MdsonicASBoolBoolean<SecretP> temp3 = p3.getMaskedSecret().and((p4.getOpened() & g2.getOpened()));
            MdsonicASBoolBoolean<SecretP> temp4 = g2.getMaskedSecret().and((p4.getOpened() & p3.getOpened()));
            MdsonicASBoolBoolean<SecretP> temp5 = innerProduct.and(g2.getOpened());
            MdsonicASBoolBoolean<SecretP> temp6 = innerProduct.and(p3.getOpened());
            MdsonicASBoolBoolean<SecretP> temp7 = innerProduct.and(p4.getOpened());
            MdsonicASBoolBoolean<SecretP> p4p3g2 = innerProduct
                    .xor(temp2)
                    .xor(temp3)
                    .xor(temp4)
                    .xor(temp5)
                    .xor(temp6)
                    .xor(temp7)
                    .xorOpen(temp1, macKeyShareBoolean, factoryBoolean.zero(), resourcePool.getMyId() == 1);

            // p4 * p3 * p2 * g1
//            List<MdsonicASBoolBoolean<SecretP>> crossProducts = new ArrayList<>(11);
//            for (int j = 0; j < 11; j++) {
//              crossProducts.add(resourcePool.getDataSupplier().getNextBitTripleProductShare());
//            }
            temp1 = (p4.getOpened() & p3.getOpened() & p2.getOpened() & g1.getOpened());
            temp2 = p4.getMaskedSecret().and((p3.getOpened() & p2.getOpened() & g1.getOpened()));
            temp3 = p3.getMaskedSecret().and((p4.getOpened() & p2.getOpened() & g1.getOpened()));
            temp4 = p2.getMaskedSecret().and((p4.getOpened() & p3.getOpened() & g1.getOpened()));
            temp5 = g1.getMaskedSecret().and((p4.getOpened() & p3.getOpened() & p2.getOpened()));
            temp6 = innerProduct.and(p2.getOpened() & g1.getOpened());
            temp7 = innerProduct.and(p3.getOpened() & g1.getOpened());
            MdsonicASBoolBoolean<SecretP> temp8 = innerProduct.and(p3.getOpened() & p2.getOpened());
            MdsonicASBoolBoolean<SecretP> temp9 = innerProduct.and(p4.getOpened() & g1.getOpened());
            MdsonicASBoolBoolean<SecretP> temp10 = innerProduct.and(p4.getOpened() & p2.getOpened());
            MdsonicASBoolBoolean<SecretP> temp11 = innerProduct.and(p4.getOpened() & p3.getOpened());
            MdsonicASBoolBoolean<SecretP> temp12 = innerProduct.and(g1.getOpened());
            MdsonicASBoolBoolean<SecretP> temp13 = innerProduct.and(p2.getOpened());
            MdsonicASBoolBoolean<SecretP> temp14 = innerProduct.and(p3.getOpened());
            MdsonicASBoolBoolean<SecretP> temp15 = innerProduct.and(p4.getOpened());
            MdsonicASBoolBoolean<SecretP> p4p3p2g1 = innerProduct
                    .xor(temp2).xor(temp3).xor(temp4)
                    .xor(temp5).xor(temp6).xor(temp7)
                    .xor(temp8).xor(temp9).xor(temp10)
                    .xor(temp11).xor(temp12).xor(temp13)
                    .xor(temp14).xor(temp15)
                    .xorOpen(temp1, macKeyShareBoolean, factoryBoolean.zero(), resourcePool.getMyId() == 1);

            // g = g4 \xor p4g3 \xor p4p3g2 \xor p4p3p2g1
            MdsonicASBoolBoolean<SecretP> g = g4.xor(p4g3).xor(p4p3g2).xor(p4p3p2g1);

            // p = p4 * p3 * p2 * p1
//            List<MdsonicASBoolBoolean<SecretP>> crossProducts_new = new ArrayList<>(11);
//            for (int j = 0; j < 11; j++) {
//              crossProducts_new.add(resourcePool.getDataSupplier().getNextBitTripleProductShare());
//            }
            temp1 = (p4.getOpened() & p3.getOpened() & p2.getOpened() & p1.getOpened());
            temp2 = p4.getMaskedSecret().and((p3.getOpened() & p2.getOpened() & p1.getOpened()));
            temp3 = p3.getMaskedSecret().and((p4.getOpened() & p2.getOpened() & p1.getOpened()));
            temp4 = p2.getMaskedSecret().and((p4.getOpened() & p3.getOpened() & p1.getOpened()));
            temp5 = p1.getMaskedSecret().and((p4.getOpened() & p3.getOpened() & p2.getOpened()));
            temp6 = innerProduct.and(p2.getOpened() & p1.getOpened());
            temp7 = innerProduct.and(p3.getOpened() & p1.getOpened());
            temp8 = innerProduct.and(p3.getOpened() & p2.getOpened());
            temp9 = innerProduct.and(p4.getOpened() & p1.getOpened());
            temp10 = innerProduct.and(p4.getOpened() & p2.getOpened());
            temp11 = innerProduct.and(p4.getOpened() & p3.getOpened());
            temp12 = innerProduct.and(p1.getOpened());
            temp13 = innerProduct.and(p2.getOpened());
            temp14 = innerProduct.and(p3.getOpened());
            temp15 = innerProduct.and(p4.getOpened());
            MdsonicASBoolBoolean<SecretP> p = innerProduct
                    .xor(temp2).xor(temp3).xor(temp4)
                    .xor(temp5).xor(temp6).xor(temp7)
                    .xor(temp8).xor(temp9).xor(temp10)
                    .xor(temp11).xor(temp12).xor(temp13)
                    .xor(temp14).xor(temp15)
                    .xorOpen(temp1, macKeyShareBoolean, factoryBoolean.zero(), resourcePool.getMyId() == 1);

            MdsonicASBoolBoolean<SecretP> masked = resourcePool.getDataSupplier().getNextBitShare();
            maskedSecrets.add(masked);
            openedSecrets.add(p.xor(masked));

            if (i != 3) {  // ASS to MSS
              masked = resourcePool.getDataSupplier().getNextBitShare();
              maskedSecrets.add(masked);
              openedSecrets.add(g.xor(masked));
            } else {  // remain ASS
              gRemainValues.add(g);
            }
          }
        }

        // ASS to MSS
        serializeAndSend(network, openedSecrets);
        return EvaluationStatus.HAS_MORE_ROUNDS;
      } else {
        receiveAndReconstruct(network, resourcePool.getNoOfParties());

        OpenedValueStore<MdsonicASBoolBoolean<SecretP>, Boolean> openedBooleanValueStore = resourcePool.getOpenedBooleanValueStore();
        openedBooleanValueStore.pushOpenedValues(openedSecrets, openedList);

        boolean opened = openedList.get(0);
        MdsonicMSBoolBoolean<SecretP> g = new MdsonicMSBoolBoolean<>(maskedSecrets.get(0), opened);
        carried.add(new SBoolPair(null, g));

        for (int i = 0; i < (openedSecrets.size() - 1) / 2; i++) {
          opened = openedList.get(2 * i + 1);
          MdsonicMSBoolBoolean<SecretP> p = new MdsonicMSBoolBoolean<>(maskedSecrets.get(2 * i + 1), opened);
          opened = openedList.get(2 * i + 2);
          g = new MdsonicMSBoolBoolean<>(maskedSecrets.get(2 * i + 2), opened);
          carried.add(new SBoolPair(p, g));
        }
        opened = openedList.get(openedList.size() - 1);
        MdsonicMSBoolBoolean<SecretP> p = new MdsonicMSBoolBoolean<>(maskedSecrets.get(openedList.size() - 1), opened);
        carried.add(new SBoolPair(p, gRemainValues.get(gRemainValues.size() - 1)));
        return EvaluationStatus.IS_DONE;
      }
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
