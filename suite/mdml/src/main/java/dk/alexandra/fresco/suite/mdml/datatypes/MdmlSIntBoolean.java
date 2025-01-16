package dk.alexandra.fresco.suite.mdml.datatypes;

/**
 * Represents an authenticated, secret-share element.
 *
 * @param <PlainT> type of underlying plain value, i.e., the value type we use for arithmetic.
 *
 */
public class MdmlSIntBoolean<PlainT extends MdmlCompUInt<?, ?, PlainT>> extends
        MdmlSInt<PlainT> {

  /**
   * Creates a {@link MdmlSIntBoolean}.
   */
  public MdmlSIntBoolean(PlainT share, PlainT macShare) {
    super(share, macShare);
  }

  /**
   * Creates a {@link MdmlSIntBoolean} from a public value. <p>All parties compute the mac share
   * of the value but only party one (by convention) stores the public value as the share, the
   * others store 0.</p>
   */
  public MdmlSIntBoolean(PlainT share, PlainT macKeyShare, PlainT zero, boolean isPartyOne) {
    this(isPartyOne ? share : zero, share.toArithmeticRep().multiply(macKeyShare));
  }

  /**
   * Compute sum of this and other.
   */
  public MdmlSIntBoolean<PlainT> xor(MdmlSIntBoolean<PlainT> other) {
    return new MdmlSIntBoolean<>(
        share.add(other.share),
        macShare.add(other.macShare)
    );
  }

  /**
   * Compute product of this and constant (open) value.
   */
  public MdmlSIntBoolean<PlainT> and(int otherBit) {
    return new MdmlSIntBoolean<>(
        share.multiplyByBit(otherBit),
        macShare.multiplyByBit(otherBit)
    );
  }

  /**
   * Compute sum of this and constant (open) value. <p>All parties compute their mac share of the
   * public value and add it to the mac share of the authenticated value, however only party 1 adds
   * the public value to is value share.</p>
   *
   * @param other constant, open value
   * @param macKeyShare mac key share for maccing open value
   * @param zero zero value
   * @param isPartyOne used to ensure that only one party adds value to share
   * @return result of sum
   */
  public MdmlSIntBoolean<PlainT> xorOpen(
      PlainT other, PlainT macKeyShare, PlainT zero, boolean isPartyOne) {
    MdmlSIntBoolean<PlainT> wrapped = new MdmlSIntBoolean<>(other, macKeyShare, zero,
        isPartyOne);
    return xor(wrapped);
  }

  public MdmlASIntArithmetic<PlainT> asArithmetic() {
    return new MdmlASIntArithmetic<>(share.toArithmeticRep(), macShare);
  }

  @Override
  public String toString() {
    return "Spdz2kSIntBoolean{" +
        "share=" + share +
        ", macShare=" + macShare +
        '}';
  }

}
