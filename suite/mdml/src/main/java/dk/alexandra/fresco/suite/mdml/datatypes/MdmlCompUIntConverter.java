package dk.alexandra.fresco.suite.mdml.datatypes;

/**
 * Allows for conversion between {@link MdmlUInt} instances and {@link MdmlCompUInt} that are composed of
 * them.
 */
public interface MdmlCompUIntConverter<
    HighT extends MdmlUInt<HighT>,
    LowT extends MdmlUInt<LowT>,
    CompT extends MdmlCompUInt<HighT, LowT, CompT>> {

  /**
   * Creates new {@link CompT} from an instance of {@link HighT}.
   */
  CompT createFromHigh(HighT value);

  /**
   * Creates new {@link CompT} from an instance of {@link LowT}.
   */
  CompT createFromLow(LowT value);

}
