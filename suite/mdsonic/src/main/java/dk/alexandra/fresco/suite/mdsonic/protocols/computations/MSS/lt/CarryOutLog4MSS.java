//package dk.alexandra.fresco.suite.mdsonic.protocols.computations.MSS.lt;
//
//import dk.alexandra.fresco.framework.DRes;
//import dk.alexandra.fresco.framework.builder.Computation;
//import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
//import dk.alexandra.fresco.framework.builder.numeric.domain.ConversionDomain;
//import dk.alexandra.fresco.framework.builder.numeric.domain.LogicalBoolean;
//import dk.alexandra.fresco.framework.util.Pair;
//import dk.alexandra.fresco.framework.util.SBoolPair;
//import dk.alexandra.fresco.framework.value.SBool;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Given values a and b represented as bits, computes if a + b overflows, i.e., if the addition
// * results in a carry.
// */
//public class CarryOutLog4MSS implements Computation<SBool, ProtocolBuilderNumeric> {  // little-endian
//
//  private final DRes<List<Boolean>> openBitsDef;
//  private final DRes<List<DRes<SBool>>> secretBitsDef;
//  private final DRes<List<DRes<SBool>>> productBitsDef;
//  private final int bitLength;
//
//  /**
//   * Constructs new {@link CarryOutLog4MSS}.
//   *
//   * @param clearBits clear bits
//   * @param secretBits secret bits
//   */
//  public CarryOutLog4MSS(DRes<List<Boolean>> clearBits, DRes<List<DRes<SBool>>> secretBits, DRes<List<DRes<SBool>>> productBitsDef, int k) {
//    this.secretBitsDef = secretBits;
//    this.openBitsDef = clearBits;
//    this.productBitsDef = productBitsDef;
//    this.bitLength = k;  // 32 or 64
//  }
//
//  @Override
//  public DRes<SBool> buildComputation(ProtocolBuilderNumeric builder) {
//    List<DRes<SBool>> secretBits = secretBitsDef.out();
//    List<DRes<SBool>> productBits = productBitsDef.out();
//    List<Boolean> openBits = openBitsDef.out();
//    if (secretBits.size() != openBits.size()) {
//      throw new IllegalArgumentException("Number of bits must be the same");
//    }
//    return builder.par(par -> {
//      List<DRes<SBool>> pValues = new ArrayList<>(secretBits.size() / 4);
//      List<DRes<SBool>> gValues = new ArrayList<>(secretBits.size() / 4);
//      LogicalBoolean logicalBoolean = par.logicalBoolean(false);
//      for (int i = 0; i < secretBits.size() / 4; i++) {
//        // parse inputs
//        List<DRes<SBool>> prodList = productBits.subList(11 * i, 11 * i + 11);  // 11项
//        Boolean a1 = openBits.get(4 * i);
//        Boolean a2 = openBits.get(4 * i + 1);
//        Boolean a3 = openBits.get(4 * i + 2);
//        Boolean a4 = openBits.get(4 * i + 3);
//        Boolean a1a2 = a1 & a2;
//        Boolean a1a3 = a1 & a3;
//        Boolean a1a4 = a1 & a4;
//        Boolean a2a3 = a2 & a3;
//        Boolean a2a4 = a2 & a4;
//        Boolean a3a4 = a3 & a4;
//        Boolean a1a2a3 = a1 & a2a3;
//        Boolean a1a2a4 = a1 & a2a4;
//        Boolean a1a3a4 = a1 & a3a4;
//        Boolean a2a3a4 = a2 & a3a4;
//        Boolean a1a2a3a4 = a1a2 & a3a4;
//        DRes<SBool> b1 = secretBits.get(4 * i);
//        DRes<SBool> b2 = secretBits.get(4 * i + 1);
//        DRes<SBool> b3 = secretBits.get(4 * i + 2);
//        DRes<SBool> b4 = secretBits.get(4 * i + 3);
//        DRes<SBool> b1b2 = prodList.get(0);
//        DRes<SBool> b1b3 = prodList.get(1);
//        DRes<SBool> b1b4 = prodList.get(2);
//        DRes<SBool> b2b3 = prodList.get(3);
//        DRes<SBool> b2b4 = prodList.get(4);
//        DRes<SBool> b3b4 = prodList.get(5);
//        DRes<SBool> b1b2b3 = prodList.get(6);
//        DRes<SBool> b1b2b4 = prodList.get(7);
//        DRes<SBool> b1b3b4 = prodList.get(8);
//        DRes<SBool> b2b3b4 = prodList.get(9);
//        DRes<SBool> b1b2b3b4 = prodList.get(10);
//        // computations for p4p3p2p1
//        List<DRes<SBool>> comP1P2P3P4 = new ArrayList<>(15);
//        comP1P2P3P4.add(b1b2b3b4);
//        comP1P2P3P4.add(logicalBoolean.andKnown(() -> a1a2a3, b4));
//        comP1P2P3P4.add(logicalBoolean.andKnown(() -> a1a2a4, b3));
//        comP1P2P3P4.add(logicalBoolean.andKnown(() -> a1a2, b3b4));
//        comP1P2P3P4.add(logicalBoolean.andKnown(() -> a1a3a4, b2));
//        comP1P2P3P4.add(logicalBoolean.andKnown(() -> a1a3, b2b4));
//        comP1P2P3P4.add(logicalBoolean.andKnown(() -> a1a4, b2b3));
//        comP1P2P3P4.add(logicalBoolean.andKnown(() -> a1, b2b3b4));
//        comP1P2P3P4.add(logicalBoolean.andKnown(() -> a2a3a4, b1));
//        comP1P2P3P4.add(logicalBoolean.andKnown(() -> a2a3, b1b4));
//        comP1P2P3P4.add(logicalBoolean.andKnown(() -> a2a4, b1b3));
//        comP1P2P3P4.add(logicalBoolean.andKnown(() -> a2, b1b3b4));
//        comP1P2P3P4.add(logicalBoolean.andKnown(() -> a3a4, b1b2));
//        comP1P2P3P4.add(logicalBoolean.andKnown(() -> a3, b1b2b4));
//        comP1P2P3P4.add(logicalBoolean.andKnown(() -> a4, b1b2b3));
//        DRes<SBool> p = logicalBoolean.xorKnown(() -> a1a2a3a4, logicalBoolean.xorOfList(() -> comP1P2P3P4));
//        pValues.add(p);
//        // computations for g4 \xor p4g3 \xor p4p3g2 \xor p4p3p2g1
//        List<DRes<SBool>> comG = new ArrayList<>(15);
//        comG.add(logicalBoolean.andKnown(() -> a4, b4));
//        comG.add(logicalBoolean.andKnown(() -> a3a4, b3));
//        comG.add(logicalBoolean.andKnown(() -> a3, b3b4));
//        comG.add(logicalBoolean.andKnown(() -> a2a3a4, b2));
//        comG.add(logicalBoolean.andKnown(() -> a2a4, b2b3));
//        comG.add(logicalBoolean.andKnown(() -> a2a3, b2b4));
//        comG.add(logicalBoolean.andKnown(() -> a2, b2b3b4));
//        comG.add(logicalBoolean.andKnown(() -> a1a2a3a4, b1));
//        comG.add(logicalBoolean.andKnown(() -> a1a2a4, b1b3));
//        comG.add(logicalBoolean.andKnown(() -> a1a2a3, b1b4));
//        comG.add(logicalBoolean.andKnown(() -> a1a2, b1b3b4));
//        comG.add(logicalBoolean.andKnown(() -> a1a3a4, b1b2));
//        comG.add(logicalBoolean.andKnown(() -> a1a4, b1b2b3));
//        comG.add(logicalBoolean.andKnown(() -> a1a3, b1b2b4));
//        comG.add(logicalBoolean.andKnown(() -> a1, b1b2b3b4));
//        DRes<SBool> g = logicalBoolean.xorOfList(() -> comG);
//        gValues.add(g);
//      }
//      if (secretBits.size() % 4 != 0) {
//        if (secretBits.size() % 4 != 3) {
//          throw new IllegalArgumentException("Illegal size in CarryOutLog4 Protocol");
//        }
//        // remain 3 items
//        // parse inputs
//        List<DRes<SBool>> prodList = productBits.subList(productBits.size() - 4, productBits.size());  // 4项
//        Boolean a1 = openBits.get(secretBits.size() - 3);
//        Boolean a2 = openBits.get(secretBits.size() - 2);
//        Boolean a3 = openBits.get(secretBits.size() - 1);
//        Boolean a1a2 = a1 & a2;
//        Boolean a1a3 = a1 & a3;
//        Boolean a2a3 = a2 & a3;
//        Boolean a1a2a3 = a1 & a2a3;
//        DRes<SBool> b1 = secretBits.get(secretBits.size() - 3);
//        DRes<SBool> b2 = secretBits.get(secretBits.size() - 2);
//        DRes<SBool> b3 = secretBits.get(secretBits.size() - 1);
//        DRes<SBool> b1b2 = prodList.get(0);
//        DRes<SBool> b1b3 = prodList.get(1);
//        DRes<SBool> b2b3 = prodList.get(2);
//        DRes<SBool> b1b2b3 = prodList.get(3);
//        // computations for p3p2p1
//        List<DRes<SBool>> comP1P2P3 = new ArrayList<>(7);
//        comP1P2P3.add(b1b2b3);
//        comP1P2P3.add(logicalBoolean.andKnown(() -> a1a3, b2));
//        comP1P2P3.add(logicalBoolean.andKnown(() -> a2a3, b1));
//        comP1P2P3.add(logicalBoolean.andKnown(() -> a3, b1b2));
//        comP1P2P3.add(logicalBoolean.andKnown(() -> a1a2, b3));
//        comP1P2P3.add(logicalBoolean.andKnown(() -> a1, b2b3));
//        comP1P2P3.add(logicalBoolean.andKnown(() -> a2, b1b3));
//        DRes<SBool> p = logicalBoolean.xorKnown(() -> a1a2a3, logicalBoolean.xorOfList(() -> comP1P2P3));
//        pValues.add(p);
//        // computations for g3 \xor p3g2 \xor p3p2g1
//        List<DRes<SBool>> comG = new ArrayList<>(7);
//        comG.add(logicalBoolean.andKnown(() -> a3, b3));
//        comG.add(logicalBoolean.andKnown(() -> a2a3, b2));
//        comG.add(logicalBoolean.andKnown(() -> a2, b2b3));
//        comG.add(logicalBoolean.andKnown(() -> a1a2a3, b1));
//        comG.add(logicalBoolean.andKnown(() -> a1a3, b1b2));
//        comG.add(logicalBoolean.andKnown(() -> a1a2, b1b3));
//        comG.add(logicalBoolean.andKnown(() -> a1, b1b2b3));
//        DRes<SBool> g = logicalBoolean.xorOfList(() -> comG);
//        gValues.add(g);
//      }
//      final Pair<DRes<List<DRes<SBool>>>, DRes<List<DRes<SBool>>>> pair = new Pair<>(() -> pValues,
//              () -> gValues);
//      return () -> pair;
//    }).par((par, pair) -> {  // ASS to MSS
//      ConversionDomain conversionDomain = par.conversionDomain();
//      List<DRes<SBool>> gValues = pair.getSecond().out();
//      if (bitLength == 64) {
//        List<DRes<SBool>> gValuesMSS = new ArrayList<>(gValues.size());
//        for (int i = 0; i < gValues.size() / 4; i++) {
//          gValuesMSS.add(gValues.get(4 * i));
//          gValuesMSS.add(gValues.get(4 * i + 1));
//          gValuesMSS.add(gValues.get(4 * i + 2));
//        }
//        DRes<List<DRes<SBool>>> pValuesDRes = conversionDomain.toMSSBooleanBatch(pair.getFirst());
//        DRes<List<DRes<SBool>>> gValueMSSDRes = conversionDomain.toMSSBooleanBatch(() -> gValuesMSS);
//        final Pair<DRes<List<DRes<SBool>>>, Pair<DRes<List<DRes<SBool>>>, DRes<List<DRes<SBool>>>>> pairRes = new Pair<>(pValuesDRes,
//                new Pair<>(() -> gValues, gValueMSSDRes));
//        return () -> pairRes;
//      } else if (bitLength == 32) {
//        List<DRes<SBool>> gValuesMSS = new ArrayList<>(gValues.size());
//        for (int i = 0; i < gValues.size() / 2; i++) {
//          gValuesMSS.add(gValues.get(2 * i));
//        }
//        DRes<List<DRes<SBool>>> pValuesDRes = conversionDomain.toMSSBooleanBatch(pair.getFirst());
//        DRes<List<DRes<SBool>>> gValueMSSDRes = conversionDomain.toMSSBooleanBatch(() -> gValuesMSS);
//        final Pair<DRes<List<DRes<SBool>>>, Pair<DRes<List<DRes<SBool>>>, DRes<List<DRes<SBool>>>>> pairRes = new Pair<>(pValuesDRes,
//                new Pair<>(() -> gValues, gValueMSSDRes));
//        return () -> pairRes;
//      } else {
//        throw new IllegalArgumentException("Illegal bitLength in CarryOutLog4 Protocol: " + bitLength + ". Only support to 32 or 64.");
//      }
//    }).par((par, pair) -> {
//      List<DRes<SBool>> pValues = pair.getFirst().out();  // MSS
//      List<DRes<SBool>> gValuesASS = pair.getSecond().getFirst().out();
//      List<DRes<SBool>> gValuesMSS = pair.getSecond().getSecond().out();
//      List<SBoolPair> pairs = new ArrayList<>(pValues.size());
//      if (bitLength == 64) {
//        for (int i = 0; i < pValues.size() / 4; i++) {
//          pairs.add(new SBoolPair(pValues.get(4 * i), gValuesMSS.get(3 * i)));
//          pairs.add(new SBoolPair(pValues.get(4 * i + 1), gValuesMSS.get(3 * i + 1)));
//          pairs.add(new SBoolPair(pValues.get(4 * i + 2), gValuesMSS.get(3 * i + 2)));
//          pairs.add(new SBoolPair(pValues.get(4 * i + 3), gValuesASS.get(4 * i + 3)));
//        }
//      } else {
//        for (int i = 0; i < pValues.size() / 2; i++) {
//          pairs.add(new SBoolPair(pValues.get(2 * i), gValuesMSS.get(i)));
//          pairs.add(new SBoolPair(pValues.get(2 * i + 1), gValuesASS.get(2 * i + 1)));
//        }
//      }
//      return par.seq(new PreCarryBitsMSS(pairs, true, bitLength));
//    });
//  }
//}
