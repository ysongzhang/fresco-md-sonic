package dk.alexandra.fresco.demo.utils;

import dk.alexandra.fresco.lib.collections.Matrix;

import java.math.BigDecimal;
import java.util.ArrayList;

public class MaxpoolingLayerParametersBench {

    private int imageHeight;
    private int imageWidth;
    private int features;
    private int poolSize;		//Filter is of size (poolSize x poolSize)
    private int stride;
    private int batchSize;

    public MaxpoolingLayerParametersBench(int imageHeight, int imageWidth, int features, int poolSize, int stride, int batchSize) {
        this.imageHeight = imageHeight;
        this.imageWidth = imageWidth;
        this.features = features;
        this.poolSize = poolSize;
        this.stride = stride;
        this.batchSize = batchSize;
    }

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

    public int getFeatures() {
        return features;
    }

    public void setFeatures(int features) {
        this.features = features;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public int getStride() {
        return stride;
    }

    public void setStride(int stride) {
        this.stride = stride;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public Matrix<BigDecimal> constructInput() {
        // size
        int size = imageHeight * imageWidth * features;
        ArrayList<BigDecimal> row = new ArrayList<>(1);
        row.add(BigDecimal.valueOf(0));
        Matrix<BigDecimal> input = new Matrix<>(size, 1, i -> row);
        return input;

//        // padding non-zero
//        ArrayList<ArrayList<BigDecimal>> matrix = new ArrayList<>(size);
//        for (int i = 0; i < size; i++) {
//            ArrayList<BigDecimal> row = new ArrayList<>(1);
//            row.add(BigDecimal.valueOf(i+1));
//            matrix.add(row);
//        }
//        return new Matrix<>(size, 1, matrix);
    }

}
