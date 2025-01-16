package dk.alexandra.fresco.suite.mdml.datatypes;


import dk.alexandra.fresco.framework.value.SInt;

/**
 * Represents an authenticated, secret-share element.
 *
 * @param <PlainT> type of underlying plain value, i.e., the value type we use for arithmetic.
 */
public class MdmlMSIntArithmetic<PlainT extends MdmlCompUInt<?, ?, PlainT>> implements SInt {  // secret = opened - maskedSecret

  private final MdmlASIntArithmetic<PlainT> maskedSecret;

  private final PlainT opened;

  /**
   * Creates a {@link MdmlMSIntArithmetic}.
   */
  public MdmlMSIntArithmetic(MdmlASIntArithmetic<PlainT> maskedSecret, PlainT opened) {
    this.maskedSecret = maskedSecret;
    this.opened = opened;
  }

  /**
   * Creates a {@link MdmlMSIntArithmetic} from a public value. <p>All parties compute the mac
   * share of the value but only party one (by convention) stores the public value as the share, the
   * others store 0.</p>
   */
  public MdmlMSIntArithmetic(PlainT share, PlainT macKeyShare, PlainT zero, boolean isPartyOne) {
    this.opened = share;
    this.maskedSecret = new MdmlASIntArithmetic<>(zero, macKeyShare, zero, isPartyOne);
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
  public MdmlASIntArithmetic<PlainT> getMaskedSecret() {
    return maskedSecret;
  }

  public SInt out() {
    return this;
  }

  /**
   * Compute sum of this and other.
   */
  public MdmlMSIntArithmetic<PlainT> add(MdmlMSIntArithmetic<PlainT> other) {
    return new MdmlMSIntArithmetic<>(maskedSecret.add(other.getMaskedSecret()), opened.add(other.getOpened()));
  }

  /**
   * Compute difference of this and other.
   */
  public MdmlMSIntArithmetic<PlainT> subtract(MdmlMSIntArithmetic<PlainT> other) {
    return new MdmlMSIntArithmetic<>(maskedSecret.subtract(other.getMaskedSecret()),
        opened.subtract(other.getOpened()));
  }

  /**
   * Compute product of this and constant (open) value.
   */
  public MdmlMSIntArithmetic<PlainT> multiply(PlainT other) {
    return new MdmlMSIntArithmetic<>(maskedSecret.multiply(other), opened.multiply(other));
  }

  /**
   * Compute sum of this and constant (open) value.
   */
  public MdmlMSIntArithmetic<PlainT> addConstant(PlainT other) {
    return new MdmlMSIntArithmetic<>(maskedSecret, opened.add(other));
  }

  @Override
  public String toString() {
    return "MdmlMSIntArithmetic{" +
        "opened=" + opened +
        ", maskedShare=" + maskedSecret.getShare() + ", maskedMacShare=" + maskedSecret.getMacShare() +
        '}';
  }

}
