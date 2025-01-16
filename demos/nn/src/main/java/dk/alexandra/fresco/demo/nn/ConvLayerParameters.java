package dk.alexandra.fresco.demo.nn;

import dk.alexandra.fresco.lib.collections.Matrix;

public class ConvLayerParameters<T> extends LayerParameters{

    private Matrix<T> weights;
    private Matrix<T> bias;
    private int InputFeatures;
    private int Filters;
    private int FilterSize;
    private int Stride;
    private int Padding;
    private int BatchSize;

    private int imageHeight;
    private int imageWidth;


    public ConvLayerParameters(Matrix<T> weights, Matrix<T> bias, int imageWidth, int imageHeight,
                               int inputFeatures, int filters, int filterSize, int stride, int padding, int batchSize) {
        super("CNN");

//        if (bias.getWidth() != 1) {
//            throw new IllegalArgumentException(
//                    "Bias must be a column vector. Has width " + bias.getWidth() + " != 1.");
//        }
//
//        if (weights.getHeight() != bias.getHeight()) {
//            throw new IllegalArgumentException("Height of weight matrix (" + weights.getHeight()
//                    + ") must be equal to height of bias vector (" + bias.getHeight() + ")");
//        }


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

    public Matrix<T> getWeights() {
        return weights;
    }

    public Matrix<T> getBias() {
        return bias;
    }

    public int getInputs() {
        return weights.getWidth();
    }

    public int getOutputs() {
        return bias.getHeight();
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

