package dk.alexandra.fresco.suite.mdsonic.datatypes;


import dk.alexandra.fresco.framework.value.SInt;

/**
 * Represents an authenticated, secret-share element.
 *
 * @param <PlainT> type of underlying plain value, i.e., the value type we use for arithmetic.
 */
public class MdsonicMSIntArithmetic<PlainT extends MdsonicCompUInt<?, ?, PlainT>> implements SInt {  // The class of MSS over Z2k: secret = opened + maskedSecret

  private final MdsonicASIntArithmetic<PlainT> maskedSecret;

  private final PlainT opened;

  /**
   * Creates a {@link MdsonicMSIntArithmetic}.
   */
  public MdsonicMSIntArithmetic(MdsonicASIntArithmetic<PlainT> maskedSecret, PlainT opened) {
    this.maskedSecret = maskedSecret;
    this.opened = opened;
  }

  /**
   * Creates a {@link MdsonicMSIntArithmetic} from a public value. <p>All parties compute the mac
   * share of the value but only party one (by convention) stores the public value as the share, the
   * others store 0.</p>
   */
  public MdsonicMSIntArithmetic(PlainT share, PlainT macKeyShare, PlainT zero, boolean isPartyOne) {
    this.opened = share;
    this.maskedSecret = new MdsonicASIntArithmetic<>(zero, macKeyShare, zero, isPartyOne);
  }

  /**
   * Return share.
   */
  public PlainT getOpened() {
    return opened;
  }

  /**
   * Return mac share.
   */
  public MdsonicASIntArithmetic<PlainT> getMaskedSecret() {
    return maskedSecret;
  }

  public SInt out() {
    return this;
  }

  /**
   * Compute sum of this and other.
   */
  public MdsonicMSIntArithmetic<PlainT> add(MdsonicMSIntArithmetic<PlainT> other) {
    return new MdsonicMSIntArithmetic<>(maskedSecret.add(other.getMaskedSecret()), opened.add(other.getOpened()));
  }

  /**
   * Compute difference of this and other.
   */
  public MdsonicMSIntArithmetic<PlainT> subtract(MdsonicMSIntArithmetic<PlainT> other) {
    return new MdsonicMSIntArithmetic<>(maskedSecret.subtract(other.getMaskedSecret()),
        opened.subtract(other.getOpened()));
  }

  /**
   * Compute product of this and constant (open) value.
   */
  public MdsonicMSIntArithmetic<PlainT> multiply(PlainT other) {
    return new MdsonicMSIntArithmetic<>(maskedSecret.multiply(other), opened.multiply(other));
  }

  /**
   * Compute sum of this and constant (open) value.
   */
  public MdsonicMSIntArithmetic<PlainT> addConstant(PlainT other) {
    return new MdsonicMSIntArithmetic<>(maskedSecret, opened.add(other));
  }

  @Override
  public String toString() {
    return "MdsonicMSIntArithmetic{" +
        "opened=" + opened +
        ", maskedShare=" + maskedSecret.getShare() + ", maskedMacShare=" + maskedSecret.getMacShare() +
        '}';
  }

}
