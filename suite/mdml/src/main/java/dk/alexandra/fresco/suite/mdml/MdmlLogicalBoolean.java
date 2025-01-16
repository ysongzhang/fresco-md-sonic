package dk.alexandra.fresco.suite.mdml;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.DefaultLogical;
import dk.alexandra.fresco.framework.builder.numeric.Logical;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUIntFactory;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlSIntBoolean;
import dk.alexandra.fresco.suite.mdml.protocols.computations.OrNeighborsComputationMdml;
import dk.alexandra.fresco.suite.mdml.protocols.natives.*;

import java.util.List;

/**
 * Logical operators for Mdml on boolean shares. <p>NOTE: requires that inputs have previously
 * been converted to boolean shares!</p>
 */
public class MdmlLogicalBoolean<PlainT extends MdmlCompUInt<?, ?, PlainT>> extends
    DefaultLogical implements Logical {

  private final MdmlCompUIntFactory<PlainT> factory;

  protected MdmlLogicalBoolean(
      ProtocolBuilderNumeric builder, MdmlCompUIntFactory<PlainT> factory) {
    super(builder);
    this.factory = factory;
  }

  @Override
  public DRes<SInt> and(DRes<SInt> bitA, DRes<SInt> bitB) {
    return builder.append(new MdmlAndProtocol<>(bitA, bitB));
  }

  @Override
  public DRes<SInt> or(DRes<SInt> bitA, DRes<SInt> bitB) {
    return builder.append(new MdmlOrProtocol<>(bitA, bitB));
  }

  @Override
  public DRes<SInt> xor(DRes<SInt> bitA, DRes<SInt> bitB) {
    return builder.append(new MdmlXorProtocol<>(bitA, bitB));
  }

  @Override
  public DRes<SInt> halfOr(DRes<SInt> bitA, DRes<SInt> bitB) {
    return xor(bitA, bitB);
  }

  @Override
  public DRes<SInt> andKnown(DRes<OInt> knownBit, DRes<SInt> secretBit) {
    return builder.append(new MdmlAndKnownProtocol<>(knownBit, secretBit));
  }

  @Override
  public DRes<List<DRes<SInt>>> pairWiseAndKnown(DRes<List<OInt>> knownBits,
      DRes<List<DRes<SInt>>> secretBits) {
    return builder.append(new MdmlAndKnownBatchedProtocol<>(knownBits, secretBits));
  }

  @Override
  public DRes<List<DRes<SInt>>> pairWiseAnd(DRes<List<DRes<SInt>>> bitsA,
      DRes<List<DRes<SInt>>> bitsB) {
    return builder.append(new MdmlAndBatchedProtocol<>(bitsA, bitsB));
  }

  @Override
  public DRes<List<DRes<SInt>>> batchedNot(DRes<List<DRes<SInt>>> bits) {
    return builder.append(new MdmlNotBatchedProtocol<>(bits));
  }

  @Override
  public DRes<List<DRes<SInt>>> pairWiseXorKnown(DRes<List<OInt>> knownBits,
      DRes<List<DRes<SInt>>> secretBits) {
    return builder.append(new MdmlXorKnownBatchedProtocol<>(knownBits, secretBits));
  }

  @Override
  public DRes<SInt> xorKnown(DRes<OInt> knownBit, DRes<SInt> secretBit) {
    return builder.append(new MdmlXorKnownProtocol<>(knownBit, secretBit));
  }

  @Override
  public DRes<SInt> not(DRes<SInt> secretBit) {
    return xorKnown(builder.getOIntFactory().one(), secretBit);
  }

  @Override
  public DRes<List<DRes<SInt>>> pairWiseOr(DRes<List<DRes<SInt>>> bitsA,
      DRes<List<DRes<SInt>>> bitsB) {
    return builder.append(new MdmlOrBatchedProtocol<>(bitsA, bitsB));
  }

  @Override
  public DRes<SInt> orOfList(DRes<List<DRes<SInt>>> bits) {
    return builder.append(new MdmlOrOfListProtocol<>(bits));
  }

  @Override
  public DRes<List<DRes<SInt>>> orNeighbors(List<DRes<SInt>> bits) {
    return builder.seq(new OrNeighborsComputationMdml(bits));
  }

  @Override
  public DRes<OInt> openAsBit(DRes<SInt> secretBit) {
    // quite heavy machinery...
    return builder.seq(seq -> {
      MdmlSIntBoolean<PlainT> bit = factory.toMdmlSIntBoolean(secretBit);
      return seq.append(new MdmlOutputToAll<>(bit.asArithmetic()));
    }).seq((seq, opened) -> {
      PlainT openBit = factory.fromOInt(opened);
      // TODO clean up
      return openBit.testBit(factory.getLowBitLength() - 1) ? factory.one() : factory.zero();
    });
  }

}
