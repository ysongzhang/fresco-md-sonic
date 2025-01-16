package dk.alexandra.fresco.suite.mdsonic.resource.storage;

import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.util.OpenedValueStoreImpl;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicASBoolBoolean;
import dk.alexandra.fresco.suite.mdsonic.datatypes.MdsonicGF;

/**
 * MD-SONIC-specific instantiation of {@link OpenedValueStore} under boolean.
 */
public class MdsonicOpenedBooleanValueStoreImpl<SecretP extends MdsonicGF<SecretP>>
    extends OpenedValueStoreImpl<MdsonicASBoolBoolean<SecretP>, Boolean> {

}
