package dk.alexandra.fresco.suite.mdml;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.DefaultLinearAlgebra;
import dk.alexandra.fresco.lib.real.RealLinearAlgebra;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.suite.mdml.protocols.natives.MdmlFixedMatrixMultiplyProtocol;

import java.math.BigDecimal;

public class MdmlFixedLinearAlgebra extends DefaultLinearAlgebra implements RealLinearAlgebra {

    private final int precision;

    protected MdmlFixedLinearAlgebra(ProtocolBuilderNumeric builder) {
        super(builder);
        precision = builder.getRealNumericContext().getPrecision();
    }

    @Override
    public DRes<Matrix<DRes<SReal>>> mult(Matrix<BigDecimal> a, DRes<Matrix<DRes<SReal>>> b) {
        throw new UnsupportedOperationException("Not supported to multiplication of Matrix<BigDecimal> and Matrix<SReal>");
    }

    @Override
    public DRes<Matrix<DRes<SReal>>> mult(DRes<Matrix<DRes<SReal>>> a, Matrix<BigDecimal> b) {
        throw new UnsupportedOperationException("Not supported to multiplication of Matrix<BigDecimal> and Matrix<SReal>");
    }

    @Override
    public DRes<Matrix<DRes<SReal>>> mult(DRes<Matrix<DRes<SReal>>> a, DRes<Matrix<DRes<SReal>>> b) {
        return builder.append(new MdmlFixedMatrixMultiplyProtocol<>(a, b, precision));
    }

    @Override
    public DRes<Matrix<DRes<SReal>>> mult(DRes<Matrix<DRes<SReal>>> a, DRes<Matrix<DRes<SReal>>> b, boolean useMatrixTriple) {
        return mult(a, b);
    }

}
