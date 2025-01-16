package dk.alexandra.fresco.suite.mdml.datatypes;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.OIntFactory;
import dk.alexandra.fresco.framework.value.SInt;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Factory for {@link CompT} instances.
 */
public interface MdmlCompUIntFactory<CompT extends MdmlCompUInt<?, ?, CompT>> extends OIntFactory {

  @Override
  default BigInteger toBigInteger(OInt value) {
    // TODO test
    return ((MdmlCompUInt) value).toBigInteger();
  }

  @Override
  default OInt fromBigInteger(BigInteger value) {
    return createFromBigInteger(value);
  }

  default CompT fromBit(int bit) {
    throw new UnsupportedOperationException();
  }

  default CompT fromBitAndHigh(long high, int bit) {throw new UnsupportedOperationException();}

  /**
   * Get result from deferred and downcast result to {@link CompT}.
   */
  default CompT fromOInt(DRes<OInt> value) {
    return Objects.requireNonNull((CompT) value.out());
  }

  /**
   * Get result from deferred and downcast result to {@link MdmlASIntArithmetic <CompT>}.
   */
  default MdmlASIntArithmetic<CompT> toMdmlASIntArithmetic(DRes<SInt> value) {
    return Objects.requireNonNull((MdmlASIntArithmetic<CompT>) value.out());
  }

  /**
   * Get result from deferred and downcast result to {@link MdmlMSIntArithmetic <CompT>}.
   */
  default MdmlMSIntArithmetic<CompT> toMdmlMSIntArithmetic(DRes<SInt> value) {
    return Objects.requireNonNull((MdmlMSIntArithmetic<CompT>) value.out());
  }

  /**
   * Get result from deferred and downcast result to {@link MdmlSIntBoolean <CompT>}.
   */
  default MdmlSIntBoolean<CompT> toMdmlSIntBoolean(DRes<SInt> value) {
    return Objects.requireNonNull((MdmlSIntBoolean<CompT>) value.out());
  }

  /**
   * Creates new {@link CompT} from a raw array of bytes.
   */
  CompT createFromBytes(byte[] bytes);

  /**
   * Creates random {@link CompT}.
   */
  CompT createRandom();

  /**
   * Creates serializer for {@link CompT} instances.
   */
  ByteSerializer<CompT> createSerializer();

  /**
   * Get length of most significant bits which represent the masking portion.
   */
  int getHighBitLength();

  /**
   * Get length of least significant bits which represent the data portion.
   */
  int getLowBitLength();

  @Override
  default int getMaxBitLength() {
    return getLowBitLength();
  }

  /**
   * Get total bit length.
   */
  default int getCompositeBitLength() {
    return getHighBitLength() + getLowBitLength();
  }

  /**
   * Creates new {@link CompT} from a {@link BigInteger}.
   */
  default CompT createFromBigInteger(BigInteger value) {
    return (value == null) ? null : createFromBytes(value.toByteArray());
  }

  /**
   * Creates new {@link CompT} from a {@link long}.
   */
  CompT fromLong(long value);

  /**
   * Creates element whose value is zero.
   */
  default CompT zero() {
    return createFromBigInteger(BigInteger.ZERO);
  }

  default CompT one() {
    return createFromBigInteger(BigInteger.ONE);
  }

  default CompT two() {
    return createFromBigInteger(BigInteger.valueOf(2));
  }

}
