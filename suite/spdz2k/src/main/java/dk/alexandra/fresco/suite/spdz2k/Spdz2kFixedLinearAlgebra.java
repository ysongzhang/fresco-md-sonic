package dk.alexandra.fresco.suite.spdz2k;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.DefaultLinearAlgebra;
import dk.alexandra.fresco.lib.real.RealLinearAlgebra;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.lib.real.fixed.SFixed;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kFixedMatrixProductProtocol;

import java.util.ArrayList;

public class Spdz2kFixedLinearAlgebra extends DefaultLinearAlgebra implements RealLinearAlgebra {

    private final int precision;

    protected Spdz2kFixedLinearAlgebra(ProtocolBuilderNumeric builder) {
        super(builder);
        precision = builder.getRealNumericContext().getPrecision();
    }

    @Override
    public DRes<Matrix<DRes<SReal>>> mult(DRes<Matrix<DRes<SReal>>> a, DRes<Matrix<DRes<SReal>>> b, boolean useMatrixTriple) {
        if (useMatrixTriple) {
            return builder.seq(seq -> {
                return seq.append(new Spdz2kFixedMatrixProductProtocol<>(a, b));
            }).seq((seq, matrix) -> {
                // truncation
                return truncate(seq, matrix);
            });
        } else {
            return builder.seq(seq -> {
                return mult(seq, a.out(), b.out(),
                        (builder, x) -> builder.realAdvanced().innerProduct(x.getFirst(), x.getSecond()));
            });
        }
    }

    private DRes<Matrix<DRes<SReal>>> truncate(ProtocolBuilderNumeric builder, Matrix<DRes<SInt>> input) {
        return builder.par(par -> {
            Matrix<DRes<SReal>> result = new Matrix<>(input.getHeight(), input.getWidth(), i -> {
                ArrayList<DRes<SReal>> row = new ArrayList<>(input.getWidth());
                ArrayList<DRes<SInt>> rowInput = input.getRow(i);
                for (int j = 0; j < input.getWidth(); j++) {
                    DRes<SInt> truncated = par.advancedNumeric().truncate(rowInput.get(j), precision);
                    row.add(new SFixed(truncated, precision));
                }
                return row;
            });
            return () -> result;
        });
    }

}
