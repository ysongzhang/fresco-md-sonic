package dk.alexandra.fresco.suite.mdml.datatypes;

/**
 * Represents an authenticated, secret-share element.
 *
 * @param <PlainT> type of underlying plain value, i.e., the value type we use for arithmetic.
 */
public class MdmlASIntArithmetic<PlainT extends MdmlCompUInt<?, ?, PlainT>> extends
        MdmlSInt<PlainT> {

  /**
   * Creates a {@link MdmlASIntArithmetic}.
   */
  public MdmlASIntArithmetic(PlainT share, PlainT macShare) {
    super(share, macShare);
  }

  /**
   * Creates a {@link MdmlASIntArithmetic} from a public value. <p>All parties compute the mac
   * share of the value but only party one (by convention) stores the public value as the share, the
   * others store 0.</p>
   */
  public MdmlASIntArithmetic(PlainT share, PlainT macKeyShare, PlainT zero, boolean isPartyOne) {
    this(isPartyOne ? share : zero, share.multiply(macKeyShare));
  }

  /**
   * Compute sum of this and other.
   */
  public MdmlASIntArithmetic<PlainT> add(MdmlASIntArithmetic<PlainT> other) {
    return new MdmlASIntArithmetic<>(share.add(other.share), macShare.add(other.macShare));
  }

  /**
   * Compute difference of this and other.
   */
  public MdmlASIntArithmetic<PlainT> subtract(MdmlASIntArithmetic<PlainT> other) {
    return new MdmlASIntArithmetic<>(share.subtract(other.share),
        macShare.subtract(other.macShare));
  }

  /**
   * Compute product of this and constant (open) value.
   */
  public MdmlASIntArithmetic<PlainT> multiply(PlainT other) {
    return new MdmlASIntArithmetic<>(share.multiply(other), macShare.multiply(other));
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
  public MdmlASIntArithmetic<PlainT> addConstant(
      PlainT other, PlainT macKeyShare, PlainT zero, boolean isPartyOne) {
    MdmlASIntArithmetic<PlainT> wrapped = new MdmlASIntArithmetic<>(other, macKeyShare, zero,
        isPartyOne);
    return add(wrapped);
  }

  /**
   * Converts this to boolean representation ({@link MdmlSIntBoolean}).
   */
  public MdmlSIntBoolean<PlainT> toBoolean() {
    return new MdmlSIntBoolean<>(
        share.toBitRep(),
        macShare.shiftLeftSmall(macShare.getLowBitLength() - 1)
    );
  }

  @Override
  public String toString() {
    return "Spdz2kSIntArithmetic{" +
        "share=" + share +
        ", macShare=" + macShare +
        '}';
  }

}
