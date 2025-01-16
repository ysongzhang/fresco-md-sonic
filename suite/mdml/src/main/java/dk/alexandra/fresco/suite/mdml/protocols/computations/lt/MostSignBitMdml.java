package dk.alexandra.fresco.suite.mdml.protocols.computations.lt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUInt;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUIntFactory;
import dk.alexandra.fresco.suite.mdml.protocols.natives.MdmlASSToMSSProtocol;
import dk.alexandra.fresco.suite.mdml.protocols.natives.MdmlGetOpenProtocol;

import java.util.ArrayList;
import java.util.List;

/**
 * Extract the value of the most significant bit of value.
 */
public class MostSignBitMdml<PlainT extends MdmlCompUInt<?, ?, PlainT>> implements
    Computation<SInt, ProtocolBuilderNumeric> {

  private final DRes<SInt> value;
  private final MdmlCompUIntFactory<PlainT> factory;
  private final int k;

  public MostSignBitMdml(DRes<SInt> value, MdmlCompUIntFactory<PlainT> factory) {
    this.value = value;
    this.factory = factory;
    this.k = factory.getLowBitLength();
  }

  @Override  // TODO: Note that there is a correctness bug that is due to the design of their LTZ protocol. We will identify it and check it with the authors.
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    DRes<OInt> Delta_delta_x = builder.append(new MdmlGetOpenProtocol<>(value));
    return builder.par(par -> {
        List<DRes<SInt>> innerRandomBits = new ArrayList<>(k);
        for (int i = 0; i < k; i++) {
            innerRandomBits.add(par.numeric().randomBit());
        }
        return () -> innerRandomBits;
    }).seq((seq, list) -> {
        DRes<SInt> z = seq.comparison().compareLTBits(Delta_delta_x, () -> list);
        return seq.conversion().toArithmetic(z);
    }).seq((seq, zInt) -> {
        return seq.append(new MdmlASSToMSSProtocol<>(zInt));
    });
  }
}
