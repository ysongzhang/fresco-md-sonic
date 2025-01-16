package dk.alexandra.fresco.suite.mdsonic.datatypes;

/**
 * Represents an authenticated, secret-share element.
 *
 * @param <PlainT> type of underlying plain value, i.e., the value type we use for arithmetic.
 */
public class MdsonicASIntArithmetic<PlainT extends MdsonicCompUInt<?, ?, PlainT>> extends
        MdsonicASInt<PlainT> {  // The class of ASS for arithmetic circuits

  /**
   * Creates a {@link MdsonicASIntArithmetic}.
   */
  public MdsonicASIntArithmetic(PlainT share, PlainT macShare) {
    super(share, macShare);
  }

  /**
   * Creates a {@link MdsonicASIntArithmetic} from a public value. <p>All parties compute the mac
   * share of the value but only party one (by convention) stores the public value as the share, the
   * others store 0.</p>
   */
  public MdsonicASIntArithmetic(PlainT share, PlainT macKeyShare, PlainT zero, boolean isPartyOne) {
    this(isPartyOne ? share : zero, share.multiply(macKeyShare));
  }

  /**
   * Compute sum of this and other.
   */
  public MdsonicASIntArithmetic<PlainT> add(MdsonicASIntArithmetic<PlainT> other) {
    return new MdsonicASIntArithmetic<>(share.add(other.share), macShare.add(other.macShare));
  }

  /**
   * Compute difference of this and other.
   */
  public MdsonicASIntArithmetic<PlainT> subtract(MdsonicASIntArithmetic<PlainT> other) {
    return new MdsonicASIntArithmetic<>(share.subtract(other.share),
        macShare.subtract(other.macShare));
  }

  /**
   * Compute product of this and constant (open) value.
   */
  public MdsonicASIntArithmetic<PlainT> multiply(PlainT other) {
    return new MdsonicASIntArithmetic<>(share.multiply(other), macShare.multiply(other));
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
  public MdsonicASIntArithmetic<PlainT> addConstant(
      PlainT other, PlainT macKeyShare, PlainT zero, boolean isPartyOne) {
    MdsonicASIntArithmetic<PlainT> wrapped = new MdsonicASIntArithmetic<>(other, macKeyShare, zero,
        isPartyOne);
    return add(wrapped);
  }

  @Override
  public String toString() {
    return "DomainASIntArithmetic{" +
        "share=" + share +
        ", macShare=" + macShare +
        '}';
  }

}
