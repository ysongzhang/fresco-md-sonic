package dk.alexandra.fresco.demo;

import dk.alexandra.fresco.demo.cli.CmdLineUtil;
import dk.alexandra.fresco.demo.nn.*;
import dk.alexandra.fresco.demo.utils.LinearAlgebraUtils;
import dk.alexandra.fresco.demo.utils.ModelLoader;
import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.collections.MatrixUtils;
import dk.alexandra.fresco.lib.real.SReal;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.math3.linear.RealVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class NNDemo implements Application<List<Matrix<BigDecimal>>, ProtocolBuilderNumeric> {

    private static Logger log = LoggerFactory.getLogger(NNDemo.class);
    private static int defaultPrecision = 13;

    public int tests;
    private final boolean useMdsonic;
    private final boolean useMatrix;
    private long now = 0;
    private long then = 0;
    private String testNet;

    // neural network
    private List<LayerParameters> layers;


    // Input
    private Matrix<BigDecimal> testVectors;

    // labels
    public Matrix<BigDecimal> plainOutput;
    public List<Integer> expected;

    public boolean acc = false;

    public LinearAlgebraUtils utils;

    public NNDemo(int num, String Testnet, boolean useMdsonic, boolean useMatrix, String pathClass, String DataSets, String batch) throws IOException {
        this.useMdsonic = useMdsonic;
        this.useMatrix = useMatrix;
        try {
            pathClass = URLDecoder.decode(pathClass, "UTF-8");
        } catch (IOException e) {
            throw e;
        }

        String pathTest = pathClass;
        String pathOutput = pathClass;
        String pathLabel = pathClass;
        ModelLoader loader = new ModelLoader(defaultPrecision);
        this.testNet = Testnet;
        if(Testnet.equals("mnist"))
        {
            if(DataSets.equals("mnist")) {
                log.info("Test mnist");

                String path0w = pathClass + "networks/mnist/2-layer/0W.csv";
                String path0b = pathClass + "networks/mnist/2-layer/0b.csv";
                String path1w = pathClass + "networks/mnist/2-layer/1W.csv";
                String path1b = pathClass + "networks/mnist/2-layer/1b.csv";
                String path2w = pathClass + "networks/mnist/2-layer/2W.csv";
                String path2b = pathClass + "networks/mnist/2-layer/2b.csv";

                this.layers = Arrays.asList(
                        loader.fullyConnectedLayerFromCsv(
                                new File(path0w),
                                new File(path0b)),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.fullyConnectedLayerFromCsv(
                                new File(path1w),
                                new File(path1b)),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.fullyConnectedLayerFromCsv(
                                new File(path2w),
                                new File(path2b))
                );
                pathTest = pathTest + "networks/DataSets/Mnist/input.csv";
            } else {
                throw new IllegalArgumentException("you can only choose mnist when testing mnist");
            }
        }
        else if(Testnet.equals("SecureML"))
        {
            if(DataSets.equals("mnist")) {
                log.info("Test SecureML");
                String path1w = pathClass + "networks/SecureML/csv/weight1.csv";
                String path1b = pathClass + "networks/SecureML/csv/bias1.csv";
                String path2w = pathClass + "networks/SecureML/csv/weight2.csv";
                String path2b = pathClass + "networks/SecureML/csv/bias2.csv";
                String path3w = pathClass + "networks/SecureML/csv/weight3.csv";
                String path3b = pathClass + "networks/SecureML/csv/bias3.csv";

                this.layers = Arrays.asList(
                        loader.fullyConnectedLayerFromCsv(// input: 784, output: 128
                                new File(path1w),
                                new File(path1b)),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.fullyConnectedLayerFromCsv(// input: 128, output: 128
                                new File(path2w),
                                new File(path2b)),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.fullyConnectedLayerFromCsv(// input: 128, output: 10
                                new File(path3w),
                                new File(path3b))
                );
                pathTest = pathTest + "networks/SecureML/csv/" + batch + "/input.csv";
                pathOutput = pathOutput + "networks/SecureML/csv/" + batch + "/output.csv";
                pathLabel = pathLabel + "networks/SecureML/csv/" + batch + "/label.csv";
                acc = true;
            } else {
                throw new IllegalArgumentException("you can only choose mnist when testing SecureML");
            }
        }
        else if(Testnet.equals("MiniONN"))
        {
            if(DataSets.equals("mnist")) {
                log.info("Test MiniONN");

                String path1w = pathClass + "networks/MiniONN/csv/weight1.csv";
                String path1b = pathClass + "networks/MiniONN/csv/bias1.csv";
                String path2w = pathClass + "networks/MiniONN/csv/weight2.csv";
                String path2b = pathClass + "networks/MiniONN/csv/bias2.csv";
                String path3w = pathClass + "networks/MiniONN/csv/weight3.csv";
                String path3b = pathClass + "networks/MiniONN/csv/bias3.csv";
                String path4w = pathClass + "networks/MiniONN/csv/weight4.csv";
                String path4b = pathClass + "networks/MiniONN/csv/bias4.csv";

                this.layers = Arrays.asList(
                        loader.ConvLayerFromCsv(
                                new File(path1w),
                                new File(path1b),
                                28, 28, 1, 16, 5, 1, 0, 1),
                        loader.Maxpooling(24, 24, 16, 2, 2, 1),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.ConvLayerFromCsv(
                                new File(path2w),
                                new File(path2b),
                                12, 12, 16, 16, 5, 1, 0, 1),
                        loader.Maxpooling(8, 8, 16, 2, 2, 1),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.fullyConnectedLayerFromCsv( // input: 256, output: 100
                                new File(path3w),
                                new File(path3b)),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.fullyConnectedLayerFromCsv( // input: 100, output: 10
                                new File(path4w),
                                new File(path4b))
                );
                pathTest = pathTest + "networks/MiniONN/csv/" + batch + "/input.csv";
                pathOutput = pathOutput + "networks/MiniONN/csv/" + batch + "/output.csv";
                pathLabel = pathLabel + "networks/MiniONN/csv/" + batch + "/label.csv";
                acc = true;
            } else {
                throw new IllegalArgumentException("you can only choose mnist when testing MiniONN");
            }
        }
        else if(Testnet.equals("Sarda"))
        {
            if(DataSets.equals("mnist")) {
                log.info("Test Sarda");

                String path1w = pathClass + "networks/Sarda/csv/weight1.csv";
                String path1b = pathClass + "networks/Sarda/csv/bias1.csv";
                String path2w = pathClass + "networks/Sarda/csv/weight2.csv";
                String path2b = pathClass + "networks/Sarda/csv/bias2.csv";
                String path3w = pathClass + "networks/Sarda/csv/weight3.csv";
                String path3b = pathClass + "networks/Sarda/csv/bias3.csv";

                this.layers = Arrays.asList(
                        loader.ConvLayerFromCsv(
                                new File(path1w),
                                new File(path1b),
                                28, 28, 1, 5, 2, 2, 0, 1),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.fullyConnectedLayerFromCsv( // input: 980, output: 100
                                new File(path2w),
                                new File(path2b)),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.fullyConnectedLayerFromCsv( // input: 100, output: 10
                                new File(path3w),
                                new File(path3b))
                );
                pathTest = pathTest + "networks/Sarda/csv/" + batch + "/input.csv";
                pathOutput = pathOutput + "networks/Sarda/csv/" + batch + "/output.csv";
                pathLabel = pathLabel + "networks/Sarda/csv/" + batch + "/label.csv";
                acc = true;
            } else {
                throw new IllegalArgumentException("you can only choose mnist when testing Sarda");
            }
        }
        else if(Testnet.equals("LeNet"))  // The network in Falcon, which could be used over Cifar-10 dataset.
        {
            if(DataSets.equals("mnist")) {
                log.info("Test LeNet");

                String path1w = pathClass + "networks/LeNet/csv/weight1.csv";
                String path1b = pathClass + "networks/LeNet/csv/bias1.csv";
                String path2w = pathClass + "networks/LeNet/csv/weight2.csv";
                String path2b = pathClass + "networks/LeNet/csv/bias2.csv";
                String path3w = pathClass + "networks/LeNet/csv/weight3.csv";
                String path3b = pathClass + "networks/LeNet/csv/bias3.csv";
                String path4w = pathClass + "networks/LeNet/csv/weight4.csv";
                String path4b = pathClass + "networks/LeNet/csv/bias4.csv";

                this.layers = Arrays.asList(
                        loader.ConvLayerFromCsv(
                                new File(path1w),
                                new File(path1b),
                                28,28,1,20,5,1,0,1),
                        loader.Maxpooling(24,24,20,2,2,1),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.ConvLayerFromCsv(
                                new File(path2w),
                                new File(path2b),
                                12,12,20,50,5,1,0,1),
                        loader.Maxpooling(8,8,50,2,2,1),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.fullyConnectedLayerFromCsv(
                                new File(path3w),
                                new File(path3b)),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.fullyConnectedLayerFromCsv(
                                new File(path4w),
                                new File(path4b))
                );
                pathTest = pathTest + "networks/LeNet/csv/input.csv";
            } else {
                throw new IllegalArgumentException("you can only choose mnist when testing LeNet");
            }
        }
        else if(Testnet.equals("LeNet-5"))
        {
            if(DataSets.equals("mnist")) {
                log.info("Test LeNet-5 MNIST");

                String path1w = pathClass + "networks/LeNet-5_mnist/csv/weight1.csv";
                String path1b = pathClass + "networks/LeNet-5_mnist/csv/bias1.csv";
                String path2w = pathClass + "networks/LeNet-5_mnist/csv/weight2.csv";
                String path2b = pathClass + "networks/LeNet-5_mnist/csv/bias2.csv";
                String path3w = pathClass + "networks/LeNet-5_mnist/csv/weight3.csv";
                String path3b = pathClass + "networks/LeNet-5_mnist/csv/bias3.csv";
                String path4w = pathClass + "networks/LeNet-5_mnist/csv/weight4.csv";
                String path4b = pathClass + "networks/LeNet-5_mnist/csv/bias4.csv";
                String path5w = pathClass + "networks/LeNet-5_mnist/csv/weight5.csv";
                String path5b = pathClass + "networks/LeNet-5_mnist/csv/bias5.csv";

                this.layers = Arrays.asList(
                        loader.ConvLayerFromCsv(
                                new File(path1w),
                                new File(path1b),
                                28,28,1,6,5,1,0,1),
                        loader.Maxpooling(24,24,6,2,2,1),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.ConvLayerFromCsv(
                                new File(path2w),
                                new File(path2b),
                                12,12,6,16,5,1,0,1),
                        loader.Maxpooling(8,8,16,2,2,1),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.fullyConnectedLayerFromCsv( // input: 256, output: 120
                                new File(path3w),
                                new File(path3b)),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.fullyConnectedLayerFromCsv( // input: 120, output: 84
                                new File(path4w),
                                new File(path4b)),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.fullyConnectedLayerFromCsv( // input: 84, output: 10
                                new File(path5w),
                                new File(path5b))
                );
                pathTest = pathTest + "networks/LeNet-5_mnist/csv/" + batch + "/input.csv";
                pathOutput = pathOutput + "networks/LeNet-5_mnist/csv/" + batch + "/output.csv";
                pathLabel = pathLabel + "networks/LeNet-5_mnist/csv/" + batch + "/label.csv";
                acc = true;
            } else if(DataSets.equals("CIFAR10")) {
                log.info("Test LeNet-5 CIFAR-10");

                String path1w = pathClass + "networks/LeNet-5_Cifar10/weight1.csv";
                String path1b = pathClass + "networks/LeNet-5_Cifar10/bias1.csv";
                String path2w = pathClass + "networks/LeNet-5_Cifar10/weight2.csv";
                String path2b = pathClass + "networks/LeNet-5_Cifar10/bias2.csv";
                String path3w = pathClass + "networks/LeNet-5_Cifar10/weight3.csv";
                String path3b = pathClass + "networks/LeNet-5_Cifar10/bias3.csv";
                String path4w = pathClass + "networks/LeNet-5_Cifar10/weight4.csv";
                String path4b = pathClass + "networks/LeNet-5_Cifar10/bias4.csv";
                String path5w = pathClass + "networks/LeNet-5_Cifar10/weight5.csv";
                String path5b = pathClass + "networks/LeNet-5_Cifar10/bias5.csv";

                this.layers = Arrays.asList(
                        loader.ConvLayerFromCsv(
                                new File(path1w),
                                new File(path1b),
                                32,32,3,6,5,1,0,1),
                        loader.Maxpooling(28,28,6,2,2,1),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.ConvLayerFromCsv(
                                new File(path2w),
                                new File(path2b),
                                14,14,6,16,5,1,0,1),
                        loader.Maxpooling(10,10,16,2,2,1),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.fullyConnectedLayerFromCsv( // input: 400, output: 120
                                new File(path3w),
                                new File(path3b)),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.fullyConnectedLayerFromCsv( // input: 120, output: 84
                                new File(path4w),
                                new File(path4b)),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.fullyConnectedLayerFromCsv( // input: 84, output: 10
                                new File(path5w),
                                new File(path5b))
                );
                pathTest = pathTest + "networks/DataSets/Cifar10/test.csv";
            } else if(DataSets.equals("TinyImageNet")) {
                log.info("Test LeNet-5 TinyImageNet");

                String path1w = pathClass + "networks/LeNet-5_TinyImageNet/weight1.csv";
                String path1b = pathClass + "networks/LeNet-5_TinyImageNet/bias1.csv";
                String path2w = pathClass + "networks/LeNet-5_TinyImageNet/weight2.csv";
                String path2b = pathClass + "networks/LeNet-5_TinyImageNet/bias2.csv";
                String path3w = pathClass + "networks/LeNet-5_TinyImageNet/weight3.csv";
                String path3b = pathClass + "networks/LeNet-5_TinyImageNet/bias3.csv";
                String path4w = pathClass + "networks/LeNet-5_TinyImageNet/weight4.csv";
                String path4b = pathClass + "networks/LeNet-5_TinyImageNet/bias4.csv";
                String path5w = pathClass + "networks/LeNet-5_TinyImageNet/weight5.csv";
                String path5b = pathClass + "networks/LeNet-5_TinyImageNet/bias5.csv";

                this.layers = Arrays.asList(
                        loader.ConvLayerFromCsv(
                                new File(path1w),
                                new File(path1b),
                                64,64,3,6,5,1,0,1),
                        loader.Maxpooling(60,60,6,2,2,1),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.ConvLayerFromCsv(
                                new File(path2w),
                                new File(path2b),
                                30,30,6,16,5,1,0,1),
                        loader.Maxpooling(26,26,16,2,2,1),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.fullyConnectedLayerFromCsv( // input: 2704, output: 120
                                new File(path3w),
                                new File(path3b)),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.fullyConnectedLayerFromCsv( // input: 120, output: 84
                                new File(path4w),
                                new File(path4b)),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.fullyConnectedLayerFromCsv( // input: 84, output: 200
                                new File(path5w),
                                new File(path5b))
                );
                pathTest = pathTest + "networks/DataSets/TinyImageNet/test.csv";
            } else {
                throw new IllegalArgumentException("you can only choose mnist & Cifar10 & TinyImageNet when testing LeNet-5");
            }
        }
        else if(Testnet.equals("AlexNet"))
        {
            if(DataSets.equals("mnist")) {
                throw new IllegalArgumentException("you can not choose mnist when testing AlexNet");
            }
            else if (DataSets.equals("CIFAR10"))
            {
                log.info("Test AlexNet CIFAR10");
                String path1w = pathClass + "networks/AlexNet_Cifar10/weight1.csv";
                String path1b = pathClass + "networks/AlexNet_Cifar10/bias1.csv";
                String path2w = pathClass + "networks/AlexNet_Cifar10/weight2.csv";
                String path2b = pathClass + "networks/AlexNet_Cifar10/bias2.csv";
                String path3w = pathClass + "networks/AlexNet_Cifar10/weight3.csv";
                String path3b = pathClass + "networks/AlexNet_Cifar10/bias3.csv";
                String path4w = pathClass + "networks/AlexNet_Cifar10/weight4.csv";
                String path4b = pathClass + "networks/AlexNet_Cifar10/bias4.csv";
                String path5w = pathClass + "networks/AlexNet_Cifar10/weight5.csv";
                String path5b = pathClass + "networks/AlexNet_Cifar10/bias5.csv";
                String path6w = pathClass + "networks/AlexNet_Cifar10/weight6.csv";
                String path6b = pathClass + "networks/AlexNet_Cifar10/bias6.csv";
                String path7w = pathClass + "networks/AlexNet_Cifar10/weight7.csv";
                String path7b = pathClass + "networks/AlexNet_Cifar10/bias7.csv";
                String path8w = pathClass + "networks/AlexNet_Cifar10/weight8.csv";
                String path8b = pathClass + "networks/AlexNet_Cifar10/bias8.csv";


                this.layers = Arrays.asList(
                        loader.ConvLayerFromCsv(
                                new File(path1w),
                                new File(path1b),
                                33, 33, 3, 96, 11, 4, 9, 1),
                        loader.Maxpooling(11, 11, 96, 3, 2, 1),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.ConvLayerFromCsv(
                                new File(path2w),
                                new File(path2b),
                                5, 5, 96, 256, 5, 1, 1, 1),
                        loader.Maxpooling(3, 3, 256, 3, 2, 1),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.ConvLayerFromCsv(
                                new File(path3w),
                                new File(path3b),
                                1, 1, 256, 384, 3, 1, 1, 1),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.ConvLayerFromCsv(
                                new File(path4w),
                                new File(path4b),
                                1, 1, 384, 384, 3, 1, 1, 1),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.ConvLayerFromCsv(
                                new File(path5w),
                                new File(path5b),
                                1, 1, 384, 256, 3, 1, 1, 1),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.fullyConnectedLayerFromCsv( // input: 256, output: 256
                                new File(path6w),
                                new File(path6b)),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.fullyConnectedLayerFromCsv( // input: 256, output: 256
                                new File(path7w),
                                new File(path7b)),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.fullyConnectedLayerFromCsv( // input: 256, output: 10
                                new File(path8w),
                                new File(path8b))
                        );
                pathTest = pathTest + "networks/DataSets/Cifar10/test33.csv";
            }
            else if (DataSets.equals("TinyImageNet"))
            {
                log.info("Test AlexNet TinyImageNet");
                String path1w = pathClass + "networks/AlexNet_TinyImageNet/weight1.csv";
                String path1b = pathClass + "networks/AlexNet_TinyImageNet/bias1.csv";
                String path2w = pathClass + "networks/AlexNet_TinyImageNet/weight2.csv";
                String path2b = pathClass + "networks/AlexNet_TinyImageNet/bias2.csv";
                String path3w = pathClass + "networks/AlexNet_TinyImageNet/weight3.csv";
                String path3b = pathClass + "networks/AlexNet_TinyImageNet/bias3.csv";
                String path4w = pathClass + "networks/AlexNet_TinyImageNet/weight4.csv";
                String path4b = pathClass + "networks/AlexNet_TinyImageNet/bias4.csv";
                String path5w = pathClass + "networks/AlexNet_TinyImageNet/weight5.csv";
                String path5b = pathClass + "networks/AlexNet_TinyImageNet/bias5.csv";
                String path6w = pathClass + "networks/AlexNet_TinyImageNet/weight6.csv";
                String path6b = pathClass + "networks/AlexNet_TinyImageNet/bias6.csv";
                String path7w = pathClass + "networks/AlexNet_TinyImageNet/weight7.csv";
                String path7b = pathClass + "networks/AlexNet_TinyImageNet/bias7.csv";
                String path8w = pathClass + "networks/AlexNet_TinyImageNet/weight8.csv";
                String path8b = pathClass + "networks/AlexNet_TinyImageNet/bias8.csv";

                this.layers = Arrays.asList(
                        loader.ConvLayerFromCsv(
                                new File(path1w),
                                new File(path1b),
                                56, 56, 3, 64, 7, 1, 3, 1),
                        loader.ConvLayerFromCsv(
                                new File(path2w),
                                new File(path2b),
                                56, 56, 64, 64, 5, 1, 2, 1),
                        loader.Maxpooling(56, 56, 64, 2, 2, 1),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.ConvLayerFromCsv(
                                new File(path3w),
                                new File(path3b),
                                28, 28, 64, 128, 5, 1, 2, 1),
                        loader.Maxpooling(28, 28, 128, 2, 2, 1),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.ConvLayerFromCsv(
                                new File(path4w),
                                new File(path4b),
                                14,14,128,256,3,1,1, 1),
                        loader.ConvLayerFromCsv(
                                new File(path5w),
                                new File(path5b),
                                14,14,256,256,3,1,1,1),
                        loader.Maxpooling(14,14,256,2,2,1),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.fullyConnectedLayerFromCsv( // input: 12544, output: 1024
                                new File(path6w),
                                new File(path6b)),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.fullyConnectedLayerFromCsv( // input: 1024, output: 1024
                                new File(path7w),
                                new File(path7b)),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.fullyConnectedLayerFromCsv( // input: 1024, output: 200
                                new File(path8w),
                                new File(path8b))
                );
                pathTest = pathTest + "networks/DataSets/TinyImageNet/test56.csv";
            }
            else if (DataSets.equals("ImageNet"))
            {
                log.info("Test AlexNet ImageNet");
                String path1w = pathClass + "networks/AlexNet_ImageNet/weight1.csv";
                String path1b = pathClass + "networks/AlexNet_ImageNet/bias1.csv";
                String path2w = pathClass + "networks/AlexNet_ImageNet/weight2.csv";
                String path2b = pathClass + "networks/AlexNet_ImageNet/bias2.csv";
                String path3w = pathClass + "networks/AlexNet_ImageNet/weight3.csv";
                String path3b = pathClass + "networks/AlexNet_ImageNet/bias3.csv";
                String path4w = pathClass + "networks/AlexNet_ImageNet/weight4.csv";
                String path4b = pathClass + "networks/AlexNet_ImageNet/bias4.csv";
                String path5w = pathClass + "networks/AlexNet_ImageNet/weight5.csv";
                String path5b = pathClass + "networks/AlexNet_ImageNet/bias5.csv";
                String path6w = pathClass + "networks/AlexNet_ImageNet/weight6.csv";
                String path6b = pathClass + "networks/AlexNet_ImageNet/bias6.csv";
                String path7w = pathClass + "networks/AlexNet_ImageNet/weight7.csv";
                String path7b = pathClass + "networks/AlexNet_ImageNet/bias7.csv";
                String path8w = pathClass + "networks/AlexNet_ImageNet/weight8.csv";
                String path8b = pathClass + "networks/AlexNet_ImageNet/bias8.csv";

                this.layers = Arrays.asList(
                        loader.ConvLayerFromCsv(
                                new File(path1w),
                                new File(path1b),
                                224, 224, 3, 96, 11, 4, 2, 1),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.Avgpooling(55, 55, 96, 3, 2, 1),
                        loader.ConvLayerFromCsv(
                            new File(path2w),
                            new File(path2b),
                            27, 27, 96, 256, 5, 1, 2, 1),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.Avgpooling(27, 27, 256, 3, 2, 1),
                        loader.ConvLayerFromCsv(
                                new File(path3w),
                                new File(path3b),
                                13, 13, 256, 384, 3, 1, 1, 1),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.ConvLayerFromCsv(
                                new File(path4w),
                                new File(path4b),
                                13, 13, 384, 384, 3, 1, 1, 1),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.ConvLayerFromCsv(
                                new File(path5w),
                                new File(path5b),
                                13, 13, 384, 256, 3, 1, 1, 1),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.Avgpooling(13, 13, 256, 3, 2, 1),
                        loader.fullyConnectedLayerFromCsv( // input: 9216, output: 4096
                                new File(path6w),
                                new File(path6b)),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.fullyConnectedLayerFromCsv( // input: 4096, output: 4096
                                new File(path7w),
                                new File(path7b)),
                        new ActiveLayerParameters(ActivationFunctions.Type.RELU),
                        loader.fullyConnectedLayerFromCsv( // input: 4096, output: 1000
                                new File(path8w),
                                new File(path8b))
                );

                pathTest = pathTest + "networks/DataSets/ImageNet/test.csv";
            } else {
                throw new IllegalArgumentException("you can only choose CIFAR-10, TinyImageNet and ImageNet when testing AlexNet");
            }
        } else if(Testnet.equals("ResNet-18"))
        {
            if(DataSets.equals("CIFAR10")) {
                log.info("Test ResNet-18 over CIFAR-10");
                // Without BN

                String path1w = pathClass + "networks/ResNet_Cifar10/weight1.csv";
                String path1b = pathClass + "networks/ResNet_Cifar10/bias1.csv";
                String path2w = pathClass + "networks/ResNet_Cifar10/weight2.csv";
                String path2b = pathClass + "networks/ResNet_Cifar10/bias2.csv";
                String path3w = pathClass + "networks/ResNet_Cifar10/weight3.csv";
                String path3b = pathClass + "networks/ResNet_Cifar10/bias3.csv";
                String path4w = pathClass + "networks/ResNet_Cifar10/weight4.csv";
                String path4b = pathClass + "networks/ResNet_Cifar10/bias4.csv";
                String path5w = pathClass + "networks/ResNet_Cifar10/weight5.csv";
                String path5b = pathClass + "networks/ResNet_Cifar10/bias5.csv";
                String path6w = pathClass + "networks/ResNet_Cifar10/weight6.csv";
                String path6b = pathClass + "networks/ResNet_Cifar10/bias6.csv";
                String path7w = pathClass + "networks/ResNet_Cifar10/weight7.csv";
                String path7b = pathClass + "networks/ResNet_Cifar10/bias7.csv";
                String path8w = pathClass + "networks/ResNet_Cifar10/weight8.csv";
                String path8b = pathClass + "networks/ResNet_Cifar10/bias8.csv";
                String path9w = pathClass + "networks/ResNet_Cifar10/weight9.csv";
                String path9b = pathClass + "networks/ResNet_Cifar10/bias9.csv";
                String path10w = pathClass + "networks/ResNet_Cifar10/weight10.csv";
                String path10b = pathClass + "networks/ResNet_Cifar10/bias10.csv";
                String path11w = pathClass + "networks/ResNet_Cifar10/weight11.csv";
                String path11b = pathClass + "networks/ResNet_Cifar10/bias11.csv";
                String path12w = pathClass + "networks/ResNet_Cifar10/weight12.csv";
                String path12b = pathClass + "networks/ResNet_Cifar10/bias12.csv";
                String path13w = pathClass + "networks/ResNet_Cifar10/weight13.csv";
                String path13b = pathClass + "networks/ResNet_Cifar10/bias13.csv";
                String path14w = pathClass + "networks/ResNet_Cifar10/weight14.csv";
                String path14b = pathClass + "networks/ResNet_Cifar10/bias14.csv";
                String path15w = pathClass + "networks/ResNet_Cifar10/weight15.csv";
                String path15b = pathClass + "networks/ResNet_Cifar10/bias15.csv";
                String path16w = pathClass + "networks/ResNet_Cifar10/weight16.csv";
                String path16b = pathClass + "networks/ResNet_Cifar10/bias16.csv";
                String path17w = pathClass + "networks/ResNet_Cifar10/weight17.csv";
                String path17b = pathClass + "networks/ResNet_Cifar10/bias17.csv";
                String path18w = pathClass + "networks/ResNet_Cifar10/weight18.csv";
                String path18b = pathClass + "networks/ResNet_Cifar10/bias18.csv";
                String path19w = pathClass + "networks/ResNet_Cifar10/weight19.csv";
                String path19b = pathClass + "networks/ResNet_Cifar10/bias19.csv";
                String path20w = pathClass + "networks/ResNet_Cifar10/weight20.csv";
                String path20b = pathClass + "networks/ResNet_Cifar10/bias20.csv";
                String path21w = pathClass + "networks/ResNet_Cifar10/weight21.csv";
                String path21b = pathClass + "networks/ResNet_Cifar10/bias21.csv";

                this.layers = Arrays.asList(
                        // Input
                        loader.ConvLayerFromCsv(
                                new File(path1w),
                                new File(path1b),
                                32, 32, 3, 64, 3, 1, 1, 1),

                        // Stage 1
                        // The first basic block
                        loader.ConvLayerFromCsv(
                                new File(path2w),
                                new File(path2b),
                                32, 32, 64, 64, 3, 1, 1, 1),
                        loader.ConvLayerFromCsv(
                                new File(path3w),
                                new File(path3b),
                                32, 32, 64, 64, 3, 1, 1, 1),
                        // The second basic block
                        loader.ConvLayerFromCsv(
                                new File(path4w),
                                new File(path4b),
                                32, 32, 64, 64, 3, 1, 1, 1),
                        loader.ConvLayerFromCsv(
                                new File(path5w),
                                new File(path5b),
                                32, 32, 64, 64, 3, 1, 1, 1),

                        // Stage 2
                        // The first basic block
                        loader.ConvLayerFromCsv(
                                new File(path6w),
                                new File(path6b),
                                32, 32, 64, 128, 3, 2, 1, 1),
                        loader.ConvLayerFromCsv(
                                new File(path7w),
                                new File(path7b),
                                16, 16, 128, 128, 3, 1, 1, 1),
                        loader.ConvLayerFromCsv(  // down sample
                                new File(path8w),
                                new File(path8b),
                                32, 32, 64, 128, 1, 2, 0, 1),
                        // The second basic block
                        loader.ConvLayerFromCsv(
                                new File(path9w),
                                new File(path9b),
                                16, 16, 128, 128, 3, 1, 1, 1),
                        loader.ConvLayerFromCsv(
                                new File(path10w),
                                new File(path10b),
                                16, 16, 128, 128, 3, 1, 1, 1),

                        // Stage 3
                        // The first basic block
                        loader.ConvLayerFromCsv(
                                new File(path11w),
                                new File(path11b),
                                16, 16, 128, 256, 3, 2, 1, 1),
                        loader.ConvLayerFromCsv(
                                new File(path12w),
                                new File(path12b),
                                8, 8, 256, 256, 3, 1, 1, 1),
                        loader.ConvLayerFromCsv(  // down sample
                                new File(path13w),
                                new File(path13b),
                                16, 16, 128, 256, 1, 2, 0, 1),
                        // The second basic block
                        loader.ConvLayerFromCsv(
                                new File(path14w),
                                new File(path14b),
                                8, 8, 256, 256, 3, 1, 1, 1),
                        loader.ConvLayerFromCsv(
                                new File(path15w),
                                new File(path15b),
                                8, 8, 256, 256, 3, 1, 1, 1),

                        // Stage 4
                        // The first basic block
                        loader.ConvLayerFromCsv(
                                new File(path16w),
                                new File(path16b),
                                8, 8, 256, 512, 3, 2, 1, 1),
                        loader.ConvLayerFromCsv(
                                new File(path17w),
                                new File(path17b),
                                4, 4, 512, 512, 3, 1, 1, 1),
                        loader.ConvLayerFromCsv(  // down sample
                                new File(path18w),
                                new File(path18b),
                                8, 8, 256, 512, 1, 2, 0, 1),
                        // The second basic block
                        loader.ConvLayerFromCsv(
                                new File(path19w),
                                new File(path19b),
                                4, 4, 512, 512, 3, 1, 1, 1),
                        loader.ConvLayerFromCsv(
                                new File(path20w),
                                new File(path20b),
                                4, 4, 512, 512, 3, 1, 1, 1),

                        // Output
                        loader.Avgpooling(4, 4, 512, 4, 1, 1),
                        loader.fullyConnectedLayerFromCsv( // input: 512, output: 10
                                new File(path21w),
                                new File(path21b))
                );
                pathTest = pathTest + "networks/DataSets/Cifar10/test.csv";
            } else if(DataSets.equals("TinyImageNet")) {
                log.info("Test ResNet-18 over Tiny ImageNet");
                // Without BN

                String path1w = pathClass + "networks/ResNet_TinyImageNet/weight1.csv";
                String path1b = pathClass + "networks/ResNet_TinyImageNet/bias1.csv";
                String path2w = pathClass + "networks/ResNet_TinyImageNet/weight2.csv";
                String path2b = pathClass + "networks/ResNet_TinyImageNet/bias2.csv";
                String path3w = pathClass + "networks/ResNet_TinyImageNet/weight3.csv";
                String path3b = pathClass + "networks/ResNet_TinyImageNet/bias3.csv";
                String path4w = pathClass + "networks/ResNet_TinyImageNet/weight4.csv";
                String path4b = pathClass + "networks/ResNet_TinyImageNet/bias4.csv";
                String path5w = pathClass + "networks/ResNet_TinyImageNet/weight5.csv";
                String path5b = pathClass + "networks/ResNet_TinyImageNet/bias5.csv";
                String path6w = pathClass + "networks/ResNet_TinyImageNet/weight6.csv";
                String path6b = pathClass + "networks/ResNet_TinyImageNet/bias6.csv";
                String path7w = pathClass + "networks/ResNet_TinyImageNet/weight7.csv";
                String path7b = pathClass + "networks/ResNet_TinyImageNet/bias7.csv";
                String path8w = pathClass + "networks/ResNet_TinyImageNet/weight8.csv";
                String path8b = pathClass + "networks/ResNet_TinyImageNet/bias8.csv";
                String path9w = pathClass + "networks/ResNet_TinyImageNet/weight9.csv";
                String path9b = pathClass + "networks/ResNet_TinyImageNet/bias9.csv";
                String path10w = pathClass + "networks/ResNet_TinyImageNet/weight10.csv";
                String path10b = pathClass + "networks/ResNet_TinyImageNet/bias10.csv";
                String path11w = pathClass + "networks/ResNet_TinyImageNet/weight11.csv";
                String path11b = pathClass + "networks/ResNet_TinyImageNet/bias11.csv";
                String path12w = pathClass + "networks/ResNet_TinyImageNet/weight12.csv";
                String path12b = pathClass + "networks/ResNet_TinyImageNet/bias12.csv";
                String path13w = pathClass + "networks/ResNet_TinyImageNet/weight13.csv";
                String path13b = pathClass + "networks/ResNet_TinyImageNet/bias13.csv";
                String path14w = pathClass + "networks/ResNet_TinyImageNet/weight14.csv";
                String path14b = pathClass + "networks/ResNet_TinyImageNet/bias14.csv";
                String path15w = pathClass + "networks/ResNet_TinyImageNet/weight15.csv";
                String path15b = pathClass + "networks/ResNet_TinyImageNet/bias15.csv";
                String path16w = pathClass + "networks/ResNet_TinyImageNet/weight16.csv";
                String path16b = pathClass + "networks/ResNet_TinyImageNet/bias16.csv";
                String path17w = pathClass + "networks/ResNet_TinyImageNet/weight17.csv";
                String path17b = pathClass + "networks/ResNet_TinyImageNet/bias17.csv";
                String path18w = pathClass + "networks/ResNet_TinyImageNet/weight18.csv";
                String path18b = pathClass + "networks/ResNet_TinyImageNet/bias18.csv";
                String path19w = pathClass + "networks/ResNet_TinyImageNet/weight19.csv";
                String path19b = pathClass + "networks/ResNet_TinyImageNet/bias19.csv";
                String path20w = pathClass + "networks/ResNet_TinyImageNet/weight20.csv";
                String path20b = pathClass + "networks/ResNet_TinyImageNet/bias20.csv";
                String path21w = pathClass + "networks/ResNet_TinyImageNet/weight21.csv";
                String path21b = pathClass + "networks/ResNet_TinyImageNet/bias21.csv";

                this.layers = Arrays.asList(
                    // Input
                    loader.ConvLayerFromCsv(
                            new File(path1w),
                            new File(path1b),
                            64, 64, 3, 64, 7, 2, 3, 1),

                    // Stage 1
                    // The first basic block
                    loader.ConvLayerFromCsv(
                            new File(path2w),
                            new File(path2b),
                            32, 32, 64, 64, 3, 1, 1, 1),
                    loader.ConvLayerFromCsv(
                            new File(path3w),
                            new File(path3b),
                            32, 32, 64, 64, 3, 1, 1, 1),
                    // The second basic block
                    loader.ConvLayerFromCsv(
                            new File(path4w),
                            new File(path4b),
                            32, 32, 64, 64, 3, 1, 1, 1),
                    loader.ConvLayerFromCsv(
                            new File(path5w),
                            new File(path5b),
                            32, 32, 64, 64, 3, 1, 1, 1),

                    // Stage 2
                    // The first basic block
                    loader.ConvLayerFromCsv(
                            new File(path6w),
                            new File(path6b),
                            32, 32, 64, 128, 3, 2, 1, 1),
                    loader.ConvLayerFromCsv(
                            new File(path7w),
                            new File(path7b),
                            16, 16, 128, 128, 3, 1, 1, 1),
                    loader.ConvLayerFromCsv(  // down sample
                            new File(path8w),
                            new File(path8b),
                            32, 32, 64, 128, 1, 2, 0, 1),
                    // The second basic block
                    loader.ConvLayerFromCsv(
                            new File(path9w),
                            new File(path9b),
                            16, 16, 128, 128, 3, 1, 1, 1),
                    loader.ConvLayerFromCsv(
                            new File(path10w),
                            new File(path10b),
                            16, 16, 128, 128, 3, 1, 1, 1),

                    // Stage 3
                    // The first basic block
                    loader.ConvLayerFromCsv(
                            new File(path11w),
                            new File(path11b),
                            16, 16, 128, 256, 3, 2, 1, 1),
                    loader.ConvLayerFromCsv(
                            new File(path12w),
                            new File(path12b),
                            8, 8, 256, 256, 3, 1, 1, 1),
                    loader.ConvLayerFromCsv(  // down sample
                            new File(path13w),
                            new File(path13b),
                            16, 16, 128, 256, 1, 2, 0, 1),
                    // The second basic block
                    loader.ConvLayerFromCsv(
                            new File(path14w),
                            new File(path14b),
                            8, 8, 256, 256, 3, 1, 1, 1),
                    loader.ConvLayerFromCsv(
                            new File(path15w),
                            new File(path15b),
                            8, 8, 256, 256, 3, 1, 1, 1),

                    // Stage 4
                    // The first basic block
                    loader.ConvLayerFromCsv(
                            new File(path16w),
                            new File(path16b),
                            8, 8, 256, 512, 3, 2, 1, 1),
                    loader.ConvLayerFromCsv(
                            new File(path17w),
                            new File(path17b),
                            4, 4, 512, 512, 3, 1, 1, 1),
                    loader.ConvLayerFromCsv(  // down sample
                            new File(path18w),
                            new File(path18b),
                            8, 8, 256, 512, 1, 2, 0, 1),
                    // The second basic block
                    loader.ConvLayerFromCsv(
                            new File(path19w),
                            new File(path19b),
                            4, 4, 512, 512, 3, 1, 1, 1),
                    loader.ConvLayerFromCsv(
                            new File(path20w),
                            new File(path20b),
                            4, 4, 512, 512, 3, 1, 1, 1),

                    // Output
                    loader.Avgpooling(4, 4, 512, 4, 1, 1),
                    loader.fullyConnectedLayerFromCsv( // input: 512, output: 200
                            new File(path21w),
                            new File(path21b))
                );
                pathTest = pathTest + "networks/DataSets/TinyImageNet/test.csv";
            } else {
                throw new IllegalArgumentException("you can only choose CIFAR-10 and TinyImageNet when testing ResNet-18");
            }
        }
        else
        {
            throw new IllegalArgumentException("you can only choose: mnist & SecureML & MiniONN & Sarda & LeNet & " +
                    "AlexNet & ResNet-18");
        }

        this.testVectors = loader.matrixFromCsv(new File(pathTest));

        if(this.acc) {
            this.plainOutput = loader.matrixFromCsv(new File(pathOutput));
            Stream<String> expectedLines = Files.lines(Paths.get(pathLabel));
            this.expected = expectedLines.map(s -> Integer.parseInt(s)).collect(Collectors.toList());
            expectedLines.close();
        }

        this.utils = new LinearAlgebraUtils();

        if (num > 128) {
            throw new UnsupportedOperationException("The test number can not large than 128.");
        } else {
            this.tests = num;
        }

        if (!acc) {
            if (num > 1) {
                throw new UnsupportedOperationException("The test number can not large than 1 when testing this neural network");
            }
        }
    }

    // public NN
//    public DRes<List<Matrix<BigDecimal>>> buildComputation(ProtocolBuilderNumeric producer) {
//        List<DRes<Matrix<DRes<BigDecimal>>>> opened = new ArrayList<>();
//        return producer.seq(par -> {
//            for (int j = 0; j < this.flag; j++) {
//                for (int i = 0; i < this.tests; i++) {
//                    DRes<Matrix<DRes<SReal>>> testVector =
//                            par.realLinAlg().input(this.utils.createColumnVector(testVectors.getRow(i)), 1);
//                    DRes<Matrix<DRes<SReal>>> out = par.seq(new NeuralNetwork(this.layers, testVector, false, useMdsonic, useMatrix));
//                    opened.add(par.realLinAlg().openMatrix(out));
//                }
//            }
//            return () -> opened.stream().map(l -> new MatrixUtils().unwrapMatrix(l))
//                    .collect(Collectors.toList());
//        });
//    }

    // privacy NN
    public DRes<List<Matrix<BigDecimal>>> buildComputation(ProtocolBuilderNumeric producer) {
        List<DRes<Matrix<DRes<BigDecimal>>>> opened = new ArrayList<>();
        return producer.seq(seq -> {
            List<LayerParameters> layersPrivacy = new ArrayList<>(this.layers.size());
            for (LayerParameters parameters : this.layers) {
                if (parameters.LayerType.equals("CNN")) {
                    ConvLayerParameters<BigDecimal> convLayerParameters = (ConvLayerParameters<BigDecimal>) parameters;
                    DRes<Matrix<DRes<SReal>>> tempWeight = seq.realLinAlg().input(convLayerParameters.getWeights(), 2);
                    DRes<Matrix<DRes<SReal>>> tempBias = seq.realLinAlg().input(convLayerParameters.getBias(), 2);
                    layersPrivacy.add(new ConvLayerParametersDRes<>(tempWeight, tempBias, convLayerParameters));
                } else if (parameters.LayerType.equals("FULL")) {
                    FullyConnectedLayerParameters<BigDecimal> fullyConnectedLayerParameters = (FullyConnectedLayerParameters<BigDecimal>) parameters;
                    DRes<Matrix<DRes<SReal>>> tempWeight = seq.realLinAlg().input(fullyConnectedLayerParameters.getWeights(), 2);
                    DRes<Matrix<DRes<SReal>>> tempBias = seq.realLinAlg().input(fullyConnectedLayerParameters.getBias(), 2);
                    layersPrivacy.add(new FullyConnectedLayerParametersDRes<>(tempWeight, tempBias));
                } else if (parameters.LayerType.equals("MaxPool")) {
                    layersPrivacy.add(parameters);
                }else if (parameters.LayerType.equals("AvgPool")) {
                    layersPrivacy.add(parameters);
                }
                else if (parameters.LayerType.equals("ACT")) {
                    layersPrivacy.add(parameters);
                }
            }
            return () -> layersPrivacy;
        }).seq((par, layersPrivacy) -> {
            now = System.currentTimeMillis();
            System.out.println("********************** Start Time: " + now + " ms.");
            for (int i = 0; i < this.tests; i++) {
                DRes<Matrix<DRes<SReal>>> testVector = par.realLinAlg().input(this.utils.createColumnVector(testVectors.getRow(i)), 2);
                DRes<Matrix<DRes<SReal>>> out;
                if (this.testNet.equals("ResNet-18")) {
                    out = par.seq(new Resnet(layersPrivacy, testVector, useMdsonic, useMatrix));
                } else {
                    out = par.seq(new NeuralNetwork(layersPrivacy, testVector, true, useMdsonic, useMatrix));
                }
                opened.add(par.realLinAlg().openMatrix(out));
            }
            return () -> opened.stream().map(l -> new MatrixUtils().unwrapMatrix(l))
                    .collect(Collectors.toList());
        });
    }

    /**
     * Main method for NNDemo.
     * @param args Arguments for the application
     * @throws IOException In case of network problems
     */
    public static <ResourcePoolT extends ResourcePool> void main(String[] args) throws IOException {
        CmdLineUtil<ResourcePoolT, ProtocolBuilderNumeric> cmdUtil = new CmdLineUtil<>();
        cmdUtil.addOption(Option.builder("n").desc("The number of tests.").hasArg().build());
        cmdUtil.addOption(Option.builder("Net").desc("The type of network.").hasArg().build());
        cmdUtil.addOption(Option.builder("path").desc("The path of resources.").hasArg().build());
        cmdUtil.addOption(Option.builder("dataset").desc("The dataset.").hasArg().build());
        cmdUtil.addOption(Option.builder("sonic").desc("Use the MD-SONIC protocol.").required(false).hasArg(false).build());
        cmdUtil.addOption(Option.builder("matrix").desc("Use matrix triples.").required(false).hasArg(false).build());
        cmdUtil.addOption(Option.builder("batch").desc("The number of batch.").required(false).hasArg().build());
        CommandLine cmd = cmdUtil.parse(args);

        boolean useMdsonic = false;
        if (cmd.hasOption("sonic")) {
            useMdsonic = true;
        }

        boolean useMatrix = false;
        if (cmd.hasOption("matrix")) {
            useMatrix = true;
        }

        String batch = "batch1";
        if (cmd.hasOption("batch")) {
            batch = cmd.getOptionValue("batch");
        }

        int testNum = Integer.parseInt(cmd.getOptionValue("n"));

        NNDemo nnDemo;
        try {
            nnDemo = new NNDemo(testNum, cmd.getOptionValue("Net"),
                    useMdsonic, useMatrix, cmd.getOptionValue("path"), cmd.getOptionValue("dataset"), batch);
        } catch (Exception e) {
            throw e;
        }

        SecureComputationEngine<ResourcePoolT, ProtocolBuilderNumeric> sce = cmdUtil.getSce();
        cmdUtil.startNetwork();
        ResourcePoolT resourcePool = cmdUtil.getResourcePool();
        List<Matrix<BigDecimal>> outputs = sce.runApplication(nnDemo, resourcePool, cmdUtil.getNetwork());
        nnDemo.then = System.currentTimeMillis();
        System.out.println("********************** End Time: " + nnDemo.then + " ms.");
        System.out.println("********************** Duration: " + (nnDemo.then - nnDemo.now) + "ms.");
        System.out.println("********************** Duration per input: " + ((nnDemo.then - nnDemo.now) / testNum) + "ms.");

        // Test accuracy; Only for Mnist
        if(nnDemo.acc) {
            int correctNum = 0;
            double relativeErrorSum = 0.0;
            for (int i = 0; i < nnDemo.tests; i++) {
                RealVector a = nnDemo.utils.convert(outputs.get(i)).getColumnVector(0);
                RealVector plain = nnDemo.utils.convert(nnDemo.plainOutput).getRowVector(i);
                int predict = a.getMaxIndex();
                int expect = nnDemo.expected.get(i);
                if (predict == expect) {
                    correctNum++;
                }
                for (int j = 0; j < a.toArray().length; j++) {
                    double plainValue = plain.getEntry(j);
                    double predictValue = a.getEntry(j);
                    double tmp = Math.abs(predictValue - plainValue);
                    relativeErrorSum += tmp / Math.abs(plainValue);
                }
            }
            log.info("Accuracy: " + 100 * ((double) correctNum / nnDemo.tests));
            log.info("Average relative error: " + 100 * (relativeErrorSum / (nnDemo.tests * nnDemo.plainOutput.getWidth())));
        }

        cmdUtil.closeNetwork();
        sce.shutdownSCE();
    }
}
