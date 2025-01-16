package dk.alexandra.fresco.suite.mdsonic;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.mdsonic.LogicalBoolean;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicCompUInt;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicCompUIntFactory;
import dk.alexandra.fresco.suite.mdsonic.protocols.computations.ASS.OrNeighborsComputationASS;
import dk.alexandra.fresco.suite.mdsonic.protocols.natives.*;
import dk.alexandra.fresco.suite.mdsonic.protocols.natives.ASS.MdsonicOrOfListProtocolASS;
import dk.alexandra.fresco.suite.mdsonic.protocols.natives.MSS.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Logical operators for Mdsonic on boolean shares. <p>NOTE: requires that inputs have previously
 * been converted to boolean shares!</p>
 *
 */
public class MdsonicLogicalBoolean<PlainT extends MdsonicCompUInt<?, ?, PlainT>> implements LogicalBoolean {

  private final MdsonicCompUIntFactory<PlainT> factory;

  private final ProtocolBuilderNumeric builder;

  private final boolean useMaskedEvaluation;

  protected MdsonicLogicalBoolean(
          ProtocolBuilderNumeric builder, MdsonicCompUIntFactory<PlainT> factory, boolean useMaskedEvaluation) {
    this.builder = builder;
    this.factory = factory;
    this.useMaskedEvaluation = useMaskedEvaluation;
  }

  @Override
  public DRes<SBool> and(DRes<SBool> bitA, DRes<SBool> bitB) {
    return builder.append(new MdsonicAndProtocol<>(bitA, bitB, useMaskedEvaluation));
  }

  @Override
  public DRes<SBool> and(DRes<SBool> bitA, DRes<SBool> bitB, DRes<SBool> maskedSecret) {
    return builder.append(new MdsonicAndProtocol<>(bitA, bitB, useMaskedEvaluation, maskedSecret));
  }

  @Override
  public DRes<SBool> or(DRes<SBool> bitA, DRes<SBool> bitB) {
    return builder.append(new MdsonicOrProtocol<>(bitA, bitB, useMaskedEvaluation));
  }

  @Override
  public DRes<SBool> or(DRes<SBool> bitA, DRes<SBool> bitB, DRes<SBool> maskedSecret) {
    return builder.append(new MdsonicOrProtocol<>(bitA, bitB, useMaskedEvaluation, maskedSecret));
  }

  @Override
  public DRes<SBool> xor(DRes<SBool> bitA, DRes<SBool> bitB) {
    return builder.append(new MdsonicXorProtocol<>(bitA, bitB, useMaskedEvaluation));
  }

  @Override
  public DRes<SBool> xor(DRes<SBool> bitA, DRes<SBool> bitB, boolean useMaskedEvaluation) {
    return builder.append(new MdsonicXorProtocol<>(bitA, bitB, useMaskedEvaluation));
  }

  @Override
  public DRes<SBool> halfOr(DRes<SBool> bitA, DRes<SBool> bitB) {
    return xor(bitA, bitB);
  }

  @Override
  public DRes<SBool> andKnown(DRes<Boolean> knownBit, DRes<SBool> secretBit) {
    return builder.append(new MdsonicAndKnownProtocol<>(knownBit, secretBit, useMaskedEvaluation));
  }

  @Override
  public DRes<SBool> andKnown(DRes<Boolean> knownBit, DRes<SBool> secretBit, boolean useMaskedEvaluation) {
    return builder.append(new MdsonicAndKnownProtocol<>(knownBit, secretBit, useMaskedEvaluation));
  }

  @Override
  public DRes<List<DRes<SBool>>> pairWiseAndKnown(DRes<List<Boolean>> knownBits,
      DRes<List<DRes<SBool>>> secretBits) {
    return builder.append(new MdsonicAndKnownBatchedProtocol<>(knownBits, secretBits, useMaskedEvaluation));
  }

  @Override
  public DRes<List<DRes<SBool>>> pairWiseAnd(DRes<List<DRes<SBool>>> bitsA,
      DRes<List<DRes<SBool>>> bitsB) {
    return builder.append(new MdsonicAndBatchedProtocol<>(bitsA, bitsB, useMaskedEvaluation));
  }

  @Override
  public DRes<List<DRes<SBool>>> pairWiseAnd(DRes<List<DRes<SBool>>> bitsA,
                                             DRes<List<DRes<SBool>>> bitsB, List<SBool> maskedSecrets) {
    return builder.append(new MdsonicAndBatchedProtocol<>(bitsA, bitsB, useMaskedEvaluation, maskedSecrets));
  }

  @Override
  public DRes<List<DRes<SBool>>> batchedNot(DRes<List<DRes<SBool>>> bits) {
    return builder.append(new MdsonicNotBatchedProtocol<>(bits, useMaskedEvaluation));
  }

  @Override
  public DRes<List<DRes<SBool>>> pairWiseXorKnown(DRes<List<Boolean>> knownBits,
      DRes<List<DRes<SBool>>> secretBits) {
    return builder.append(new MdsonicXorKnownBatchedProtocol<>(knownBits, secretBits, useMaskedEvaluation));
  }

  @Override
  public DRes<SBool> xorKnown(DRes<Boolean> knownBit, DRes<SBool> secretBit) {
    return builder.append(new MdsonicXorKnownProtocol<>(knownBit, secretBit, useMaskedEvaluation));
  }

  @Override
  public DRes<SBool> xorKnown(DRes<Boolean> knownBit, DRes<SBool> secretBit, boolean useMaskedEvaluation) {
    return builder.append(new MdsonicXorKnownProtocol<>(knownBit, secretBit, useMaskedEvaluation));
  }

  @Override
  public DRes<SBool> not(DRes<SBool> secretBit) {
    return xorKnown(() -> true, secretBit);
  }

  @Override
  public DRes<List<DRes<SBool>>> pairWiseOr(DRes<List<DRes<SBool>>> bitsA,
      DRes<List<DRes<SBool>>> bitsB) {
    return builder.append(new MdsonicOrBatchedProtocol<>(bitsA, bitsB, useMaskedEvaluation));
  }

  @Override
  public DRes<List<DRes<SBool>>> pairWiseOr(DRes<List<DRes<SBool>>> bitsA,
                                            DRes<List<DRes<SBool>>> bitsB, List<SBool> maskedSecrets) {
    return builder.append(new MdsonicOrBatchedProtocol<>(bitsA, bitsB, useMaskedEvaluation, maskedSecrets));
  }

  @Override
  public DRes<List<DRes<SBool>>> pairWiseXor(DRes<List<DRes<SBool>>> bitsA,
                                      DRes<List<DRes<SBool>>> bitsB) {
    return builder.append(new MdsonicXorBatchedProtocol<>(bitsA, bitsB, useMaskedEvaluation));
  }

  @Override
  public DRes<SBool> xorOfList(DRes<List<DRes<SBool>>> bits) {
    return builder.append(new MdsonicXorListProtocol<>(bits, useMaskedEvaluation));
  }

  @Override
  public DRes<Boolean> openAsBit(DRes<SBool> secretBit) {
    return builder.append(new MdsonicOutputToAllAsBit<>(secretBit, useMaskedEvaluation));
  }

  @Override
  public DRes<List<DRes<Boolean>>> openAsBits(DRes<List<DRes<SBool>>> secretBits) {
    return builder.par(par -> {
      List<DRes<Boolean>> openList =
              secretBits.out().stream().map(closed -> par.logicalBoolean().openAsBit(closed))
                      .collect(Collectors.toList());
      return () -> openList;
    });
  }

  // ASS专有计算
  @Override
  public DRes<SBool> orOfList(DRes<List<DRes<SBool>>> bits) {
    if (useMaskedEvaluation) {
      throw new UnsupportedOperationException("Masked: don't support to or of list");
    } else {
      return builder.append(new MdsonicOrOfListProtocolASS<>(bits));
    }
  }

  @Override
  public DRes<List<DRes<SBool>>> orNeighbors(DRes<List<DRes<SBool>>> bits) {
    if (useMaskedEvaluation) {
      throw new UnsupportedOperationException("Masked: don't support to or neighbors");
    } else {
      return builder.seq(new OrNeighborsComputationASS(bits));
    }
  }


  // MSS专有计算
  @Override
  public DRes<SBool> andMSS(DRes<SBool> bitA, DRes<SBool> bitB) {  // input: MSS, output: ASS
    // 错误判断
    if (!useMaskedEvaluation) {
      throw new UnsupportedOperationException("ASS: don't support to andMSS");
    }
    return builder.append(new MdsonicAndProtocolMSS<>(bitA, bitB));
  }

  @Override
  public DRes<List<DRes<SBool>>> pairWiseAndMSS(DRes<List<DRes<SBool>>> bitsA,
                                             DRes<List<DRes<SBool>>> bitsB) {  // input: MSS, output: ASS
    if (!useMaskedEvaluation) {
      throw new UnsupportedOperationException("ASS: don't support to pairWiseAndMSS");
    }
    return builder.append(new MdsonicAndBatchedProtocolMSS<>(bitsA, bitsB));
  }

  @Override
  public DRes<SBool> orMSS(DRes<SBool> bitA, DRes<SBool> bitB) {  // input: MSS, output: ASS
    if (!useMaskedEvaluation) {
      throw new UnsupportedOperationException("ASS: don't support to orMSS");
    }
    return builder.append(new MdsonicOrProtocolMSS<>(bitA, bitB));
  }

  @Override
  public DRes<List<DRes<SBool>>> pairWiseOrMSS(DRes<List<DRes<SBool>>> bitsA,
                                            DRes<List<DRes<SBool>>> bitsB) {  // input: MSS, output: ASS
    if (!useMaskedEvaluation) {
      throw new UnsupportedOperationException("ASS: don't support to pairWiseOrMSS");
    }
    return builder.append(new MdsonicOrBatchedProtocolMSS<>(bitsA, bitsB));
  }

  @Override
  public DRes<Boolean> getOpened(DRes<SBool> secretShare) {
    if (useMaskedEvaluation) {
      return builder.append(new MdsonicGetOpenedBitProtocolMSS<>(secretShare));
    } else {
      throw new UnsupportedOperationException("ASS: Don't support obtain Opened value of an MSS secret bit");
    }
  }

  @Override
  public DRes<Boolean> maskOpen(DRes<SBool> input, DRes<SBool> mask) {
    return builder.append(new MdsonicMaskOpenProtocol<>(input, mask));
  }

}
