package dk.alexandra.fresco.suite.mdsonic.datatypes;

/**
 * Allows for conversion between {@link MdsonicUInt} instances and {@link MdsonicCompUInt} that are composed of
 * them.
 */
public interface MdsonicCompUIntConverter<
    HighT extends MdsonicUInt<HighT>,
    LowT extends MdsonicUInt<LowT>,
    CompT extends MdsonicCompUInt<HighT, LowT, CompT>> {

  /**
   * Creates new {@link CompT} from an instance of {@link HighT}.
   */
  CompT createFromHigh(HighT value);

  /**
   * Creates new {@link CompT} from an instance of {@link LowT}.
   */
  CompT createFromLow(LowT value);

}
