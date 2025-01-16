package dk.alexandra.fresco.suite.mdml.protocols.natives;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUInt;
import dk.alexandra.fresco.suite.mdml.resource.MdmlResourcePool;

/**
 * Generic native MDML protocol.
 */
public abstract class MdmlNativeProtocol<
    OutputT,
    PlainT extends MdmlCompUInt<?, ?, PlainT>> implements
    NativeProtocol<OutputT, MdmlResourcePool<PlainT>> {

}
