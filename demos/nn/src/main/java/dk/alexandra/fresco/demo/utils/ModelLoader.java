package dk.alexandra.fresco.demo.utils;

import dk.alexandra.fresco.demo.nn.AvgpoolingLayerParameters;
import dk.alexandra.fresco.demo.nn.ConvLayerParameters;
import dk.alexandra.fresco.demo.nn.MaxpoolingLayerParameters;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.demo.nn.FullyConnectedLayerParameters;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class ModelLoader {

  private final int precision;

  public ModelLoader(int precision) {
    this.precision = precision;
  }

  public Matrix<BigDecimal> matrixFromCsv(File file) throws IOException {
    ArrayList<ArrayList<BigDecimal>> rows = new ArrayList<>();
    CSVParser parser = CSVParser.parse(file, Charset.defaultCharset(), CSVFormat.EXCEL);
    for (CSVRecord record : parser) {
      ArrayList<BigDecimal> row = new ArrayList<>();
      for (String entry : record) {
        row.add(new BigDecimal(entry).setScale(precision, RoundingMode.DOWN));
      }
      rows.add(row);
    }
    return new Matrix<>(rows.size(), rows.get(0).size(), rows);
  }

  public FullyConnectedLayerParameters<BigDecimal> fullyConnectedLayerFromCsv(File weights, File bias) throws IOException {
    Matrix<BigDecimal> weightsMatrix = matrixFromCsv(weights);
    //System.out.println("weight_H:"+weightsMatrix.getHeight());
    //System.out.println("weight_W:"+weightsMatrix.getWidth());
    Matrix<BigDecimal> biasVector = matrixFromCsv(bias);
    //System.out.println("bias_H:"+biasVector.getHeight());
    //System.out.println("bias_W:"+biasVector.getWidth());
    return new FullyConnectedLayerParameters<>(weightsMatrix, biasVector);
  }

  public ConvLayerParameters<BigDecimal> ConvLayerFromCsv(File weights, File bias, int imageHeight, int imageWidth,
                                                          int inputFeatures, int filters, int filterSize, int stride, int padding, int batchSize
                                                          ) throws IOException {
    Matrix<BigDecimal> weightsMatrix = matrixFromCsv(weights);

    Matrix<BigDecimal> biasVector = matrixFromCsv(bias);

    return new ConvLayerParameters<>(weightsMatrix, biasVector, imageHeight, imageWidth, inputFeatures, filters, filterSize, stride, padding, batchSize);
  }

  public MaxpoolingLayerParameters Maxpooling(int imageHeights, int imageWidths, int inputFeatures,
                                                          int poolsize, int stride, int batchSize) throws IOException {

    return new MaxpoolingLayerParameters(imageHeights, imageWidths, inputFeatures, poolsize, stride, batchSize);
  }

  public AvgpoolingLayerParameters Avgpooling(int imageHeights, int imageWidths, int inputFeatures,
                                              int poolsize, int stride, int batchSize) throws IOException {

    return new AvgpoolingLayerParameters(imageHeights, imageWidths, inputFeatures, poolsize, stride, batchSize);
  }


}
