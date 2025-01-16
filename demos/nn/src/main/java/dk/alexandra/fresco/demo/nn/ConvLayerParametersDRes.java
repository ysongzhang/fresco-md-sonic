package dk.alexandra.fresco.demo.nn;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.lib.collections.Matrix;

public class ConvLayerParametersDRes<T> extends LayerParameters{

    private DRes<Matrix<T>> weights;
    private DRes<Matrix<T>> bias;
    private int InputFeatures;
    private int Filters;
    private int FilterSize;
    private int Stride;
    private int Padding;
    private int BatchSize;

    private int imageHeight;
    private int imageWidth;


    public ConvLayerParametersDRes(DRes<Matrix<T>> weights, DRes<Matrix<T>> bias, int imageWidth, int imageHeight,
                                   int inputFeatures, int filters, int filterSize, int stride, int padding, int batchSize) {
        super("CNN");

        this.weights = weights;
        this.bias = bias;

        this.FilterSize = filterSize;
        this.Filters = filters;
        this.BatchSize = batchSize;
        this.InputFeatures = inputFeatures;
        this.Padding = padding;
        this.Stride = stride;
        this.imageHeight = imageHeight;
        this.imageWidth = imageWidth;
    }

    public ConvLayerParametersDRes(DRes<Matrix<T>> weights, DRes<Matrix<T>> bias, ConvLayerParameters convLayerParameters) {
        this(weights, bias, convLayerParameters.getImageWidth(), convLayerParameters.getImageHeight(),
                convLayerParameters.getInputFeatures(), convLayerParameters.getFilters(), convLayerParameters.getFilterSize(),
                convLayerParameters.getStride(), convLayerParameters.getPadding(), convLayerParameters.getBatchSize());
    }

    public DRes<Matrix<T>> getWeights() {
        return weights;
    }

    public DRes<Matrix<T>> getBias() {
        return bias;
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

}

