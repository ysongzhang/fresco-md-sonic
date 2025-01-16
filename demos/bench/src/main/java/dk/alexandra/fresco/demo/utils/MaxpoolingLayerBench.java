package dk.alexandra.fresco.demo.utils;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.SReal;

import java.util.ArrayList;
import java.util.List;

public class MaxpoolingLayerBench implements LayerBench {  // Output Column Vector

    private final boolean useMdsonic;
    private DRes<Matrix<DRes<SReal>>> v;  // Column Vector
    private MaxpoolingLayerParametersBench parameters;

    public MaxpoolingLayerBench(MaxpoolingLayerParametersBench parameters,
                                DRes<Matrix<DRes<SReal>>> v) {

        this.parameters = parameters;
        this.v = v;
        this.useMdsonic = false;
    }

    public MaxpoolingLayerBench(MaxpoolingLayerParametersBench parameters,
                                DRes<Matrix<DRes<SReal>>> v, boolean useMdsonic) {

        this.parameters = parameters;
        this.v = v;
        this.useMdsonic = useMdsonic;
    }

    @Override
    public DRes<Matrix<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric builder) {


        int B = parameters.getBatchSize();
        int iw = parameters.getImageWidth();
        int ih = parameters.getImageHeight();
        int f = parameters.getPoolSize();
        int S = parameters.getStride();
        int Din = parameters.getFeatures();
        int ow 	= (((iw-f)/S)+1);
        int oh	= (((ih-f)/S)+1);

        int Columns = f*f;
        int Rows = ow*oh*Din*B;

        return builder.seq(r -> {
            ArrayList<DRes<SReal>> Inputv = v.out().getColumnArray(0);
            return () -> Inputv;
        }).seq((r0, input) -> {
            ArrayList<DRes<SReal>>t1 = new ArrayList<>(ow*oh*Din*B*f*f);

            int sizeBeta = iw;
            int sizeD 	= sizeBeta*ih;
            int sizeB 	= sizeD*Din;
            for (int b = 0; b < B; ++b)
                for (int r = 0; r < Din; ++r)
                    for (int beta = 0; beta < ih-f+1; beta+=S)
                        for (int alpha = 0; alpha < iw-f+1; alpha+=S)
                            for (int q = 0; q < f; ++q)
                                for (int p = 0; p < f; ++p)
                                {
                                    t1.add(input.get(b*sizeB + r*sizeD + (beta + q)*sizeBeta + (alpha + p)));
                                }
            return () -> t1;
        }).par((r1, t1) -> {
            List<DRes<Matrix<DRes<SReal>>>> input_transpose = new ArrayList<>(Columns);
            for (int i = 0; i < Columns; i++) {
                ArrayList<DRes<SReal>> temp = new ArrayList<>(Rows);
                for (int j = 0; j < Rows; j++) {
                    temp.add(t1.get(j * Columns + i));
                }
                Matrix<DRes<SReal>> tempM = new Matrix<>(1, Rows, ii -> temp);
                input_transpose.add(() -> tempM);
            }
            return () -> input_transpose;
        }).whileLoop((list) -> list.size() > 1,
                (r2, list) -> r2.seq(new ParallelMaxPoolingBench(list, useMdsonic)))
                .seq((seq, out) -> out.get(0))
                .seq((r3, o) -> r3.realLinAlg().transpose(() -> o));
    }
}
