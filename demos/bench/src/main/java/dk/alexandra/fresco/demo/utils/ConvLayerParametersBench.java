package dk.alexandra.fresco.demo.utils;

import dk.alexandra.fresco.lib.collections.Matrix;

import java.math.BigDecimal;
import java.util.ArrayList;

public class ConvLayerParametersBench {
    private int InputFeatures;
    private int Filters;
    private int FilterSize;
    private int Stride;
    private int Padding;
    private int BatchSize;

    private int imageHeight;
    private int imageWidth;


    public ConvLayerParametersBench(int imageWidth, int imageHeight,
                                    int inputFeatures, int filters, int filterSize, int stride, int padding, int batchSize) {
        this.FilterSize = filterSize;
        this.Filters = filters;
        this.BatchSize = batchSize;
        this.InputFeatures = inputFeatures;
        this.Padding = padding;
        this.Stride = stride;
        this.imageHeight = imageHeight;
        this.imageWidth = imageWidth;
    }

    public int getInputFeatures(){return this.InputFeatures;}
    public int getFilterSize(){return this.FilterSize;}
    public int getPadding(){return this.Padding;}
    public int getStride(){return this.Stride;}
    public int getBatchSize(){return this.BatchSize;}
    public int getFilters(){return this.Filters;}

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public Matrix<BigDecimal> constructInput() {
        // size
        int size = imageHeight * imageWidth * InputFeatures;
        return paddingZero(size, 1);

        // padding non-zero value
//        return paddingNonZero(size, 1);
    }

    public Matrix<BigDecimal> constructWeight() {
        // size
        int height = Filters;
        int width = FilterSize * FilterSize * InputFeatures;
        return paddingZero(height, width);

        // padding non-zero value
//        return paddingNonZero(height, width);
    }

    public Matrix<BigDecimal> constructBias() {
        // size
        int ow = (((imageWidth-FilterSize+2*Padding)/Stride)+1);
        int oh = (((imageHeight-FilterSize+2*Padding)/Stride)+1);
        int height = Filters;
        int width = ow * oh;
        return paddingZero(height, width);

        // padding non-zero value
//        return paddingNonZero(height, width);
    }

    private Matrix<BigDecimal> paddingZero(int height, int width) {
        ArrayList<BigDecimal> row = new ArrayList<>(width);
        for (int i = 0; i < width; i++) {
            row.add(BigDecimal.valueOf(0));
        }
        return new Matrix<>(height, width, i -> row);
    }

    private Matrix<BigDecimal> paddingNonZero(int height, int width) {
        ArrayList<BigDecimal> row = new ArrayList<>(width);
        for (int i = 0; i < width; i++) {
            row.add(BigDecimal.valueOf(i+1));
        }
        return new Matrix<>(height, width, i -> row);
    }

}

