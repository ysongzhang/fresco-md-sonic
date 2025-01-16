package dk.alexandra.fresco.demo.utils;


import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.SReal;

import java.util.ArrayList;


public class PrivacyConvLayerBench implements LayerBench {  // Output Column Vector

    private DRes<Matrix<DRes<SReal>>> v;  // Column Vector
    private DRes<Matrix<DRes<SReal>>> weight;  // Column Vector
    private DRes<Matrix<DRes<SReal>>> bias;  // Column Vector
    private ConvLayerParametersBench parameters;
    final private boolean useMatrix;

    public PrivacyConvLayerBench(ConvLayerParametersBench parameters,
                                 DRes<Matrix<DRes<SReal>>> v,
                                 DRes<Matrix<DRes<SReal>>> weight,
                                 DRes<Matrix<DRes<SReal>>> bias,
                                 boolean useMatrix) {
        this.parameters = parameters;
        this.v = v;
        this.weight = weight;
        this.bias = bias;
        this.useMatrix = useMatrix;
    }

    @Override
    public DRes<Matrix<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric builder) {
        return builder.seq(r0->{
            int InputFeatures = parameters.getInputFeatures();
            int Filters = parameters.getFilters();
            int FilterSize = parameters.getFilterSize();
            int Stride = parameters.getStride();
            int Padding = parameters.getPadding();
            int BatchSize = parameters.getBatchSize();
            int ih = parameters.getImageHeight();
            int iw = parameters.getImageWidth();
            int ow = (((iw-FilterSize+2*Padding)/Stride)+1);
            int oh = (((ih-FilterSize+2*Padding)/Stride)+1);

            ArrayList<DRes<SReal>> Inputv = v.out().getColumnArray(0);
            DRes<SReal>[] Inputv2 = new DRes[(FilterSize * FilterSize * InputFeatures) * (ow * oh * 1)];

            int loc_input, loc_output;
            for (int i = 0; i < 1; ++i)
                for (int j = 0; j < oh; j++)
                    for (int k = 0; k < ow; k++)
                    {
                        loc_output = (i*ow*oh + j*ow + k);
                        for (int l = 0; l < InputFeatures; ++l)
                        {
                            loc_input = i * (iw+2*Padding)*(ih+2*Padding)*InputFeatures + l*(iw+2*Padding)*(ih+2*Padding) + j*Stride*(iw+2*Padding) + k*Stride;
                            for (int a = 0; a < FilterSize; ++a)			//filter height
                                for (int b = 0; b < FilterSize; ++b){		//filter width
                                    Inputv2[(l * FilterSize * FilterSize + a * FilterSize + b) * ow * oh + loc_output] = Inputv.get(loc_input + a*(iw+2*Padding) + b);
                                }
                        }
                    }
            Matrix<DRes<SReal>> input_matrix = new Matrix<>(FilterSize * FilterSize * InputFeatures,ow*oh, i -> {
                ArrayList<DRes<SReal>> T = new ArrayList<>();
                for(int j = i * ow * oh; j < (i + 1) * ow * oh; j++)
                {
                    T.add(Inputv2[j]);
                }
                return T;
            });

            return () -> input_matrix;
        }).seq((r1, x) -> {
            return r1.realLinAlg().add(bias,
                    r1.realLinAlg().mult(weight, ()->x, useMatrix));
        }).par((r2, w) -> {
            Matrix<DRes<SReal>> wColumn =  new Matrix<>(w.getHeight() * w.getWidth(), 1, i -> {
                ArrayList<DRes<SReal>> w1 = new ArrayList<>();
                w1.add(w.getRow(i / w.getWidth()).get(i % w.getWidth()));
                return w1;
            });
            return () -> wColumn;
        });
    }


}
