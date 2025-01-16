package dk.alexandra.fresco.demo.nn;

public class AvgpoolingLayerParameters extends LayerParameters  {

    private int imageHeight;
    private int imageWidth;
    private int features;
    private int poolSize;		//Filter is of size (poolSize x poolSize)
    private int stride;
    private int batchSize;

    public AvgpoolingLayerParameters(int imageHeight, int imageWidth, int features, int poolSize, int stride, int batchSize) {
        super("AvgPool");
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

}


