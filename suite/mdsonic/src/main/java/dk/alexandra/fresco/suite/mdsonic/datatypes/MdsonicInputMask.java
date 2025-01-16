package dk.alexandra.fresco.suite.mdsonic.datatypes;

public class MdsonicInputMask<PlainT extends MdsonicCompUInt<?, ?, PlainT>> {

  private final MdsonicASIntArithmetic<PlainT> maskShare;
  private final PlainT openValue;

  public MdsonicInputMask(MdsonicASIntArithmetic<PlainT> maskShare) {
    this(maskShare, null);
  }

  public MdsonicInputMask(MdsonicASIntArithmetic<PlainT> maskShare, PlainT openValue) {
    this.maskShare = maskShare;
    this.openValue = openValue;
  }

  public MdsonicASIntArithmetic<PlainT> getMaskShare() {
    return maskShare;
  }

  public PlainT getOpenValue() {
    return openValue;
  }

}
