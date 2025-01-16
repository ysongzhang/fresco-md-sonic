package dk.alexandra.fresco.suite.mdml.resource.storage;

import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.util.OpenedValueStoreImpl;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlASIntArithmetic;

/**
 * Mdml-specific instantiation of {@link OpenedValueStore}.
 */
public class MdmlOpenedValueStoreImpl<PlainT extends MdmlCompUInt<?, ?, PlainT>>
    extends OpenedValueStoreImpl<MdmlASIntArithmetic<PlainT>, PlainT> {

}
