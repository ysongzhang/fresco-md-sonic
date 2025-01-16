package dk.alexandra.fresco.suite.mdsonic.datatypes;

import dk.alexandra.fresco.framework.value.SInt;

public abstract class MdsonicASInt<PlainT extends MdsonicCompUInt<?, ?, PlainT>> implements SInt {  // The base class of ASS for arithmetic circuits

  protected final PlainT share;
  protected final PlainT macShare;

  public MdsonicASInt(PlainT share, PlainT macShare) {
    this.share = share;
    this.macShare = macShare;
  }

  /**
   * Return share.
   */
  public PlainT getShare() {
    return share;
  }

  /**
   * Return mac share.
   */
  public PlainT getMacShare() {
    return macShare;
  }

  public byte[] serializeShareLow() {
    return share.serializeLeastSignificant();
  }

  public byte[] serializeShare() {
    return share.serializeAll();
  }

  @Override
  public SInt out() {
    return this;
  }

}
