package dk.alexandra.fresco.suite.mdml;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.math.integer.binary.RandomBitMask;
import dk.alexandra.fresco.suite.mdml.protocols.natives.MdmlTruncateFromPairs;
import dk.alexandra.fresco.suite.mdml.protocols.natives.MdmlTruncationPairProtocol;

import java.math.BigInteger;
import java.util.List;

/**
 * MDML-specific advanced numeric functionality.
 */
public class MdmlAdvancedNumeric implements AdvancedNumeric {
  private final ProtocolBuilderNumeric builder;

  MdmlAdvancedNumeric(ProtocolBuilderNumeric builder) {
    this.builder = builder;
  }

  @Override
  public DRes<SInt> truncate(DRes<SInt> input, int shifts) {
    return builder.append(new MdmlTruncateFromPairs<>(input, shifts));
  }

  @Override
  public DRes<SInt> truncate(DRes<SInt> input, int shifts, boolean useTruncationPairs) {
    return truncate(input, shifts);
  }

  @Override
  public DRes<TruncationPair> generateTruncationPair(int d) {
    return builder.seq(seq -> seq.append(new MdmlTruncationPairProtocol<>(d)));
  }


  @Override
  public DRes<SInt> sum(List<DRes<SInt>> elements){
    throw new UnsupportedOperationException("Not supported to sum in AdvancedNumeric");
  }

  @Override
  public DRes<SInt> sum(DRes<List<DRes<SInt>>> elements) {
    throw new UnsupportedOperationException("Not supported to sum in AdvancedNumeric");
  }

  @Override
  public DRes<SInt> product(List<DRes<SInt>> elements){
    throw new UnsupportedOperationException("Not supported to product in AdvancedNumeric");
  }

  @Override
  public DRes<SInt> div(DRes<SInt> dividend, BigInteger divisor){
    throw new UnsupportedOperationException("Not supported to div in AdvancedNumeric");
  }

  @Override
  public DRes<SInt> div(DRes<SInt> dividend, DRes<SInt> divisor) {
    throw new UnsupportedOperationException("Not supported to div in AdvancedNumeric");
  }

  @Override
  public DRes<SInt> mod(DRes<SInt> dividend, BigInteger divisor) {
    throw new UnsupportedOperationException("Not supported to mod in AdvancedNumeric");
  }

  @Override
  public DRes<List<SInt>> toBits(DRes<SInt> in, int maxInputLength){
    throw new UnsupportedOperationException("Not supported to toBits in AdvancedNumeric");
  }

  @Override
  public DRes<SInt> exp(DRes<SInt> x, DRes<SInt> e, int maxExponentLength){
    throw new UnsupportedOperationException("Not supported to exp in AdvancedNumeric");
  }

  @Override
  public DRes<SInt> exp(BigInteger x, DRes<SInt> e, int maxExponentLength){
    throw new UnsupportedOperationException("Not supported to exp in AdvancedNumeric");
  }

  @Override
  public DRes<SInt> exp(DRes<SInt> x, BigInteger e){
    throw new UnsupportedOperationException("Not supported to exp in AdvancedNumeric");
  }

  @Override
  public DRes<SInt> sqrt(DRes<SInt> input, int maxInputLength) {
    throw new UnsupportedOperationException("Not supported to sqrt in AdvancedNumeric");
  }

  @Override
  public DRes<SInt> log(DRes<SInt> input, int maxInputLength) {
    throw new UnsupportedOperationException("Not supported to log in AdvancedNumeric");
  }

  @Override
  public DRes<SInt> innerProduct(List<DRes<SInt>> vectorA, List<DRes<SInt>> vectorB){
    throw new UnsupportedOperationException("Not supported to innerProduct in AdvancedNumeric");
  }

  @Override
  public DRes<SInt> innerProductWithPublicPart(List<BigInteger> vectorA, List<DRes<SInt>> vectorB){
    throw new UnsupportedOperationException("Not supported to innerProductWithPublicPart in AdvancedNumeric");
  }

  @Override
  public DRes<SInt> innerProductWithPublicPart(DRes<List<OInt>> vectorA,
                                               DRes<List<DRes<SInt>>> vectorB) {
    throw new UnsupportedOperationException("Not supported to innerProductWithPublicPart in AdvancedNumeric");
  }

  @Override
  public DRes<RandomAdditiveMask> additiveMask(int noOfBits){
    throw new UnsupportedOperationException("Not supported to additiveMask in AdvancedNumeric");
  }

  @Override
  public DRes<RandomBitMask> randomBitMask(int noOfBits){
    throw new UnsupportedOperationException("Not supported to randomBitMask in AdvancedNumeric");
  }

  @Override
  public DRes<RandomBitMask> randomBitMask(DRes<List<DRes<SInt>>> randomBits){
    throw new UnsupportedOperationException("Not supported to randomBitMask in AdvancedNumeric");
  }

  @Override
  public DRes<SInt> rightShift(DRes<SInt> input){
    throw new UnsupportedOperationException("Not supported to rightShift in AdvancedNumeric");
  }

  @Override
  public DRes<SInt> rightShift(DRes<SInt> input, int shifts) {
    throw new UnsupportedOperationException("Not supported to rightShift in AdvancedNumeric");
  }

  @Override
  public DRes<RightShiftResult> rightShiftWithRemainder(DRes<SInt> input){
    throw new UnsupportedOperationException("Not supported to rightShiftWithRemainder in AdvancedNumeric");
  }

  @Override
  public DRes<RightShiftResult> rightShiftWithRemainder(DRes<SInt> input, int shifts){
    throw new UnsupportedOperationException("Not supported to rightShiftWithRemainder in AdvancedNumeric");
  }

  @Override
  public DRes<SInt> bitLength(DRes<SInt> input, int maxBitLength){
    throw new UnsupportedOperationException("Not supported to bitLength in AdvancedNumeric");
  }

  @Override
  public DRes<SInt> invert(DRes<SInt> x){
    throw new UnsupportedOperationException("Not supported to invert in AdvancedNumeric");
  }

  @Override
  public DRes<SInt> condSelect(DRes<SInt> condition, DRes<SInt> left, DRes<SInt> right){
    throw new UnsupportedOperationException("Not supported to condSelect in AdvancedNumeric");
  }

  @Override
  public DRes<Pair<DRes<SInt>, DRes<SInt>>> swapIf(DRes<SInt> condition, DRes<SInt> left,
                                            DRes<SInt> right){
    throw new UnsupportedOperationException("Not supported to swapIf in AdvancedNumeric");
  }
}
