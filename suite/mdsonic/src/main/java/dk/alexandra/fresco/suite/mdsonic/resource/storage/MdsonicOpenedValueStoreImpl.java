package dk.alexandra.fresco.suite.mdsonic.resource.storage;

import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.util.OpenedValueStoreImpl;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicASIntArithmetic;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicCompUInt;

/**
 * MD-SONIC-specific instantiation of {@link OpenedValueStore} under arithmetic.
 */
public class MdsonicOpenedValueStoreImpl<PlainT extends MdsonicCompUInt<?, ?, PlainT>>
    extends OpenedValueStoreImpl<MdsonicASIntArithmetic<PlainT>, PlainT> {

}
