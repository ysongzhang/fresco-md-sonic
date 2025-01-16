package dk.alexandra.fresco.framework.builder.numeric.mdsonic;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.framework.value.SBool;

import java.util.List;

/**
 * Logical operators on secret arithmetic representations of boolean values. <p>NOTE: all inputs are
 * assumed to represent 0 or 1 values only. The result is undefined if other values are passed
 * in.</p>
 */
public interface LogicalBoolean extends ComputationDirectory {
  // TODO: this is starting to look a lot like the Binary computation directory...

  /**
   * Computes logical AND of inputs. <p>NOTE: Inputs must represent 0 or 1 values only.</p>
   */
  DRes<SBool> and(DRes<SBool> bitA, DRes<SBool> bitB);

  DRes<SBool> and(DRes<SBool> bitA, DRes<SBool> bitB, DRes<SBool> maskedSecret);

  /**
   * Computes logical OR of inputs. <p>NOTE: Inputs must represent 0 or 1 values only.</p>
   */
  DRes<SBool> or(DRes<SBool> bitA, DRes<SBool> bitB);

  DRes<SBool> or(DRes<SBool> bitA, DRes<SBool> bitB, DRes<SBool> maskedSecret);

  /**
   * Computes logical OR of inputs but is only safe to use when at least one of the bits is 0.
   * <p>This allows to express and or as a linear operation and therefore far more efficient.</p>
   */
  DRes<SBool> halfOr(DRes<SBool> bitA, DRes<SBool> bitB);

  /**
   * Computes logical XOR of inputs. <p>NOTE: Inputs must represent 0 or 1 values only.</p>
   */
  DRes<SBool> xor(DRes<SBool> bitA, DRes<SBool> bitB);

  default DRes<SBool> xor(DRes<SBool> bitA, DRes<SBool> bitB, boolean useMaskedEvaluation) {
    return xor(bitA, bitB);
  }

  /**
   * Computes logical AND of inputs. <p>NOTE: Inputs must represent 0 or 1 values only.</p>
   */
  DRes<SBool> andKnown(DRes<Boolean> knownBit, DRes<SBool> secretBit);

  default DRes<SBool> andKnown(DRes<Boolean> knownBit, DRes<SBool> secretBit, boolean useMaskedEvaluation) {
    return andKnown(knownBit, secretBit);
  }

  /**
   * Computes logical XOR of inputs. <p>NOTE: Inputs must represent 0 or 1 values only.</p>
   */
  DRes<SBool> xorKnown(DRes<Boolean> knownBit, DRes<SBool> secretBit);

  default DRes<SBool> xorKnown(DRes<Boolean> knownBit, DRes<SBool> secretBit, boolean useMaskedEvaluation) {
    return xorKnown(knownBit, secretBit);
  }

  /**
   * Computes logical NOT of input. <p>NOTE: Input must represent 0 or 1 values only.</p>
   */
  DRes<SBool> not(DRes<SBool> secretBit);

  /**
   * Opens secret bits, possibly performing conversion before producing final open value. <p>NOTE:
   * Input must represent 0 or 1 values only.</p>
   */
  DRes<Boolean> openAsBit(DRes<SBool> secretBit);

  /**
   * Batch opening of bits.
   */
  DRes<List<DRes<Boolean>>> openAsBits(DRes<List<DRes<SBool>>> secretBits);

  /**
   * Negates all given bits.
   */
  DRes<List<DRes<SBool>>> batchedNot(DRes<List<DRes<SBool>>> bits);

  /**
   * Computes pairwise logical AND of input bits. <p>NOTE: Inputs must represent 0 or 1 values
   * only.</p>
   */
  DRes<List<DRes<SBool>>> pairWiseAndKnown(DRes<List<Boolean>> knownBits,
      DRes<List<DRes<SBool>>> secretBits);

  /**
   * Computes pairwise logical AND of input bits. <p>NOTE: Inputs must represent 0 or 1 values
   * only.</p>
   */
  DRes<List<DRes<SBool>>> pairWiseAnd(DRes<List<DRes<SBool>>> bitsA,
      DRes<List<DRes<SBool>>> bitsB);

  DRes<List<DRes<SBool>>> pairWiseAnd(DRes<List<DRes<SBool>>> bitsA,
                                      DRes<List<DRes<SBool>>> bitsB, List<SBool> maskedSecrets);

  /**
   * Computes pairwise logical OR of input bits. <p>NOTE: Inputs must represent 0 or 1 values
   * only.</p>
   */
  DRes<List<DRes<SBool>>> pairWiseOr(DRes<List<DRes<SBool>>> bitsA,
      DRes<List<DRes<SBool>>> bitsB);

  DRes<List<DRes<SBool>>> pairWiseOr(DRes<List<DRes<SBool>>> bitsA,
                                     DRes<List<DRes<SBool>>> bitsB, List<SBool> maskedSecrets);

  /**
   * Computes pairwise logical XOR of input bits. <p>NOTE: Inputs must represent 0 or 1 values
   * only.</p>
   */
  DRes<List<DRes<SBool>>> pairWiseXor(DRes<List<DRes<SBool>>> bitsA,
      DRes<List<DRes<SBool>>> bitsB);

  DRes<SBool> xorOfList(DRes<List<DRes<SBool>>> bits);

  /**
   * Computes pairwise logical XOR of input bits. <p>NOTE: Inputs must represent 0 or 1 values
   * only.</p>
   */
  DRes<List<DRes<SBool>>> pairWiseXorKnown(DRes<List<Boolean>> knownBits,
      DRes<List<DRes<SBool>>> secretBits);

  /**
   * Computes logical OR of all input bits. <p> NOTE: Inputs must represent 0 or 1 values only.
   * </p>
   */
  DRes<SBool> orOfList(DRes<List<DRes<SBool>>> bits);

  /**
   * Given a list of bits, computes or of each neighbor pair of bits, i.e., given b1, b2, b3, b4,
   * will output b1 OR b2, b3 OR b4. <p>Also handles uneven number of elements.</p>
   */
  DRes<List<DRes<SBool>>> orNeighbors(DRes<List<DRes<SBool>>> bits);

  // MSS专有计算
  DRes<SBool> andMSS(DRes<SBool> bitA, DRes<SBool> bitB);

  DRes<List<DRes<SBool>>> pairWiseAndMSS(DRes<List<DRes<SBool>>> bitsA,
                                         DRes<List<DRes<SBool>>> bitsB);

  DRes<SBool> orMSS(DRes<SBool> bitA, DRes<SBool> bitB);

  DRes<List<DRes<SBool>>> pairWiseOrMSS(DRes<List<DRes<SBool>>> bitsA,
                                        DRes<List<DRes<SBool>>> bitsB);

  DRes<Boolean> getOpened(DRes<SBool> secretShare);

  DRes<Boolean> maskOpen(DRes<SBool> input, DRes<SBool> mask);

}
