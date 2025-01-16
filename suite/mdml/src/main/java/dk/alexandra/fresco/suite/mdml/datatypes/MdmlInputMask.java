package dk.alexandra.fresco.suite.mdml.datatypes;

public class MdmlInputMask<PlainT extends MdmlCompUInt<?, ?, PlainT>> {

  private final MdmlASIntArithmetic<PlainT> maskShare;
  private final PlainT openValue;

  public MdmlInputMask(MdmlASIntArithmetic<PlainT> maskShare) {
    this(maskShare, null);
  }

  public MdmlInputMask(MdmlASIntArithmetic<PlainT> maskShare, PlainT openValue) {
    this.maskShare = maskShare;
    this.openValue = openValue;
  }

  public MdmlASIntArithmetic<PlainT> getMaskShare() {
    return maskShare;
  }

  public PlainT getOpenValue() {
    return openValue;
  }

}
