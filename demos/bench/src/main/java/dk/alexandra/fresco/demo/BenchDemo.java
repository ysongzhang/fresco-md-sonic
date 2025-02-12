package dk.alexandra.fresco.demo;

import dk.alexandra.fresco.demo.cli.CmdLineUtil;
import dk.alexandra.fresco.demo.utils.*;
import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.mdsonic.NumericMdsonic;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.RealLinearAlgebra;
import dk.alexandra.fresco.lib.real.SReal;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

public class BenchDemo implements Application<Matrix<BigDecimal>, ProtocolBuilderNumeric> {
    public enum TestType {
        MULT,
        CMP,
        MATMUL,
        RELU,
        CONV,
        MP
    }
    private static Logger log = LoggerFactory.getLogger(BenchDemo.class);
    private int myId;
    private boolean useMdsonic;
    private TestType testType;

    // matmult and relu
    private Matrix<BigDecimal> xMatrix;
    private Matrix<BigDecimal> yMatrix;

    // mult and cmp
    private int number;
    private long input1;
    private long input2;

    // conv
    private ConvLayerParametersBench convLayerParameters;
    private Matrix<BigDecimal> inputMatrix;
    private Matrix<BigDecimal> weightMatrix;
    private Matrix<BigDecimal> biasMatrix;

    // max pool
    private MaxpoolingLayerParametersBench maxpoolingLayerParameters;

    // time
    private long then = 0;
    private long now = 0;


    // mult and cmp
    public BenchDemo(TestType testType, int id, boolean useMdsonic, int n) {
        this.testType = testType;
        this.myId = id;
        this.useMdsonic = useMdsonic;
        this.number = n;
        this.input1 = 0;
        this.input2 = 0;
    }

    // matmult
    public BenchDemo(TestType testType, int id, Matrix<BigDecimal> a, Matrix<BigDecimal> b, boolean useMdsonic) {
        this.testType = testType;
        this.myId = id;
        this.xMatrix = a;
        this.yMatrix = b;
        this.useMdsonic = useMdsonic;
    }

    // relu
    public BenchDemo(TestType testType, int id, Matrix<BigDecimal> a, boolean useMdsonic) {
        this.testType = testType;
        this.myId = id;
        this.xMatrix = a;
        this.useMdsonic = useMdsonic;
    }

    // conv
    public BenchDemo(TestType testType, int id, ConvLayerParametersBench parameters, Matrix<BigDecimal> a, Matrix<BigDecimal> b, Matrix<BigDecimal> c, boolean useMdsonic) {
        this.testType = testType;
        this.myId = id;
        this.convLayerParameters = parameters;
        this.inputMatrix = a;
        this.weightMatrix = b;
        this.biasMatrix = c;
        this.useMdsonic = useMdsonic;
    }

    // conv
    public BenchDemo(TestType testType, int id, MaxpoolingLayerParametersBench parameters, Matrix<BigDecimal> a, boolean useMdsonic) {
        this.testType = testType;
        this.myId = id;
        this.maxpoolingLayerParameters = parameters;
        this.inputMatrix = a;
        this.useMdsonic = useMdsonic;
    }

    @Override // test matrix product
    public DRes<Matrix<BigDecimal>> buildComputation(ProtocolBuilderNumeric producer) {
        if (useMdsonic) {
            if (testType == TestType.MULT) {
                return producer.par(par -> {
                            // Input element
                            NumericMdsonic numericIo = par.numericMdsonic();
                            DRes<SInt> x1 = (myId == 2)
                                    ? numericIo.input(BigInteger.valueOf(input1), 2) : numericIo.input(null, 2);
                            DRes<SInt> x2 = (myId == 2)
                                    ? numericIo.input(BigInteger.valueOf(input2), 2) : numericIo.input(null, 2);
                            Pair<DRes<SInt>, DRes<SInt>> input = new Pair<>(x1, x2);
                            return () -> input;
                        }).par((par, input) -> {
                            for (int i = 0; i < number; i++) {
                                par.numericMdsonic().mult(input.getFirst(), input.getSecond());
                            }
                            return () -> new Matrix<>(1, 1, new ArrayList<>(new ArrayList<>()));
                        });
            } else if (testType == TestType.CMP) {
                return producer.par(par -> {
                    // Input element
                    NumericMdsonic numericIo = par.numericMdsonic();
                    DRes<SInt> x1 = (myId == 2)
                            ? numericIo.input(BigInteger.valueOf(input1), 2) : numericIo.input(null, 2);
                    DRes<SInt> x2 = (myId == 2)
                            ? numericIo.input(BigInteger.valueOf(input2), 2) : numericIo.input(null, 2);
                    Pair<DRes<SInt>, DRes<SInt>> input = new Pair<>(x1, x2);
                    return () -> input;
                }).par((par, input) -> {
                    for (int i = 0; i < number; i++) {
                        par.comparisonMdsonic().compareLT(input.getFirst(), input.getSecond());
                    }
                    return () -> new Matrix<>(1, 1, new ArrayList<>(new ArrayList<>()));
                });
            } else if (testType == TestType.MATMUL) {
                return producer.par(par -> {
                    // Input element
                    RealLinearAlgebra  realLinearAlgebra = par.realLinAlg();
                    Matrix<BigDecimal> nullMatrix = new Matrix<>(xMatrix.getHeight(), xMatrix.getWidth());
                    DRes<Matrix<DRes<SReal>>> x = (myId == 2)
                            ? realLinearAlgebra.input(xMatrix, 2) : realLinearAlgebra.input(nullMatrix, 2);
                    nullMatrix = new Matrix<>(yMatrix.getHeight(), yMatrix.getWidth());
                    DRes<Matrix<DRes<SReal>>> y = (myId == 2)
                            ? realLinearAlgebra.input(yMatrix, 2) : realLinearAlgebra.input(nullMatrix, 2);
                    Pair<DRes<Matrix<DRes<SReal>>>, DRes<Matrix<DRes<SReal>>>> inputs = new Pair<>(x, y);
                    return () -> inputs;
                }).seq((seq, input) -> {
                    now = System.currentTimeMillis();
                    System.out.println("********************** Start Time: " + now + " ms.");
                    seq.realLinAlg().mult(input.getFirst(), input.getSecond(), true);
                    return () -> new Matrix<>(1, 1, new ArrayList<>(new ArrayList<>()));
                });
            } else if (testType == TestType.RELU) {  // ReLU
                return producer.par(par -> {
                    // Input element
                    RealLinearAlgebra  realLinearAlgebra = par.realLinAlg();
                    Matrix<BigDecimal> nullMatrix = new Matrix<>(xMatrix.getHeight(), xMatrix.getWidth());
                    DRes<Matrix<DRes<SReal>>> x = (myId == 2)
                            ? realLinearAlgebra.input(xMatrix, 2) : realLinearAlgebra.input(nullMatrix, 2);
                    return () -> x;
                }).seq((seq, input) -> {
                    now = System.currentTimeMillis();
                    System.out.println("********************** Start Time: " + now + " ms.");
                    ActivationFunctionsBench activation = new MdsonicActivationFunctionsBench(seq);
                    activation.activation(ActivationFunctionsBench.Type.RELU, input);
                    return () -> new Matrix<>(1, 1, new ArrayList<>(new ArrayList<>()));
                });
            } else if (testType == TestType.CONV) {
                return producer.par(par -> {
                    // Input element
                    RealLinearAlgebra realLinearAlgebra = par.realLinAlg();
                    Matrix<BigDecimal> nullMatrix1 = new Matrix<>(inputMatrix.getHeight(), inputMatrix.getWidth());
                    DRes<Matrix<DRes<SReal>>> input = (myId == 2)
                            ? realLinearAlgebra.input(inputMatrix, 2) : realLinearAlgebra.input(nullMatrix1, 2);
                    Matrix<BigDecimal> nullMatrix2 = new Matrix<>(weightMatrix.getHeight(), weightMatrix.getWidth());
                    DRes<Matrix<DRes<SReal>>> weight = (myId == 2)
                            ? realLinearAlgebra.input(weightMatrix, 2) : realLinearAlgebra.input(nullMatrix2, 2);
                    Matrix<BigDecimal> nullMatrix3 = new Matrix<>(biasMatrix.getHeight(), biasMatrix.getWidth());
                    DRes<Matrix<DRes<SReal>>> bias = (myId == 2)
                            ? realLinearAlgebra.input(biasMatrix, 2) : realLinearAlgebra.input(nullMatrix3, 2);
                    Pair<DRes<Matrix<DRes<SReal>>>, DRes<Matrix<DRes<SReal>>>> parameter = new Pair<>(weight, bias);
                    Pair<DRes<Matrix<DRes<SReal>>>, Pair<DRes<Matrix<DRes<SReal>>>, DRes<Matrix<DRes<SReal>>>>> pairPair = new Pair<>(input, parameter);
                    return () -> pairPair;
                }).seq((seq, input) -> {
                    now = System.currentTimeMillis();
                    System.out.println("********************** Start Time: " + now + " ms.");
                    DRes<Matrix<DRes<SReal>>> res = seq.seq(new PrivacyConvLayerBench(convLayerParameters, input.getFirst(), input.getSecond().getFirst(), input.getSecond().getSecond(), true));
                    return () -> new Matrix<>(1, 1, new ArrayList<>(new ArrayList<>()));
//                    DRes<Matrix<DRes<BigDecimal>>> result = seq.realLinAlg().openMatrix(res);
//                    return () -> result;
//                }).seq((seq, result) -> {
//                    MatrixUtils utils = new MatrixUtils();
//                    Matrix<BigDecimal> res = utils.unwrapMatrix(result);
//                    return () -> res;
                });
            } else { // max pool
                return producer.par(par -> {
                    // Input element
                    RealLinearAlgebra realLinearAlgebra = par.realLinAlg();
                    Matrix<BigDecimal> nullMatrix1 = new Matrix<>(inputMatrix.getHeight(), inputMatrix.getWidth());
                    DRes<Matrix<DRes<SReal>>> input = (myId == 2)
                            ? realLinearAlgebra.input(inputMatrix, 2) : realLinearAlgebra.input(nullMatrix1, 2);
                    return () -> input;
                }).seq((seq, input) -> {
                    now = System.currentTimeMillis();
                    System.out.println("********************** Start Time: " + now + " ms.");
                    DRes<Matrix<DRes<SReal>>> res = seq.seq(new MaxpoolingLayerBench(maxpoolingLayerParameters, input, true));
                    return () -> new Matrix<>(1, 1, new ArrayList<>(new ArrayList<>()));
//                    DRes<Matrix<DRes<BigDecimal>>> result = seq.realLinAlg().openMatrix(res);
//                    return () -> result;
//                }).seq((seq, result) -> {
//                    MatrixUtils utils = new MatrixUtils();
//                    Matrix<BigDecimal> res = utils.unwrapMatrix(result);
//                    return () -> res;
                });
            }
        } else {
            if (testType == TestType.MULT) {
                return producer.par(par -> {
                    // Input element
                    Numeric numericIo = par.numeric();
                    DRes<SInt> x1 = (myId == 2)
                            ? numericIo.input(BigInteger.valueOf(input1), 2) : numericIo.input(null, 2);
                    DRes<SInt> x2 = (myId == 2)
                            ? numericIo.input(BigInteger.valueOf(input2), 2) : numericIo.input(null, 2);
                    Pair<DRes<SInt>, DRes<SInt>> input = new Pair<>(x1, x2);
                    return () -> input;
                }).par((par, input) -> {
                    for (int i = 0; i < number; i++) {
                        par.numeric().mult(input.getFirst(), input.getSecond());
                    }
                    return () -> new Matrix<>(1, 1, new ArrayList<>(new ArrayList<>()));
                });
            } else if (testType == TestType.CMP) {
                return producer.par(par -> {
                    // Input element
                    Numeric numericIo = par.numeric();
                    DRes<SInt> x1 = (myId == 2)
                            ? numericIo.input(BigInteger.valueOf(input1), 2) : numericIo.input(null, 2);
                    DRes<SInt> x2 = (myId == 2)
                            ? numericIo.input(BigInteger.valueOf(input2), 2) : numericIo.input(null, 2);
                    Pair<DRes<SInt>, DRes<SInt>> input = new Pair<>(x1, x2);
                    return () -> input;
                }).par((par, input) -> {
                    for (int i = 0; i < number; i++) {
                        par.comparison().compareLT(input.getFirst(), input.getSecond());
                    }
                    return () -> new Matrix<>(1, 1, new ArrayList<>(new ArrayList<>()));
                });
            } else if (testType == TestType.MATMUL) {
                return producer.par(par -> {
                    // Input element
                    RealLinearAlgebra  realLinearAlgebra = par.realLinAlg();
                    Matrix<BigDecimal> nullMatrix = new Matrix<>(xMatrix.getHeight(), xMatrix.getWidth());
                    DRes<Matrix<DRes<SReal>>> x = (myId == 2)
                            ? realLinearAlgebra.input(xMatrix, 2) : realLinearAlgebra.input(nullMatrix, 2);
                    nullMatrix = new Matrix<>(yMatrix.getHeight(), yMatrix.getWidth());
                    DRes<Matrix<DRes<SReal>>> y = (myId == 2)
                            ? realLinearAlgebra.input(yMatrix, 2) : realLinearAlgebra.input(nullMatrix, 2);
                    Pair<DRes<Matrix<DRes<SReal>>>, DRes<Matrix<DRes<SReal>>>> inputs = new Pair<>(x, y);
                    return () -> inputs;
                }).seq((seq, input) -> {
                    now = System.currentTimeMillis();
                    System.out.println("********************** Start Time: " + now + " ms.");
                    seq.realLinAlg().mult(input.getFirst(), input.getSecond(), false);
                    return () -> new Matrix<>(1, 1, new ArrayList<>(new ArrayList<>()));
                });
            } else if (testType == TestType.RELU) {
                return producer.par(par -> {
                    // Input element
                    RealLinearAlgebra  realLinearAlgebra = par.realLinAlg();
                    Matrix<BigDecimal> nullMatrix = new Matrix<>(xMatrix.getHeight(), xMatrix.getWidth());
                    DRes<Matrix<DRes<SReal>>> x = (myId == 2)
                            ? realLinearAlgebra.input(xMatrix, 2) : realLinearAlgebra.input(nullMatrix, 2);
                    return () -> x;
                }).seq((seq, input) -> {
                    now = System.currentTimeMillis();
                    System.out.println("********************** Start Time: " + now + " ms.");
                    ActivationFunctionsBench activation = new DefaultActivationFunctionsBench(seq);
                    activation.activation(ActivationFunctionsBench.Type.RELU, input);
                    return () -> new Matrix<>(1, 1, new ArrayList<>(new ArrayList<>()));
                });
            } else if (testType == TestType.CONV) {
                return producer.par(par -> {
                    // Input element
                    RealLinearAlgebra realLinearAlgebra = par.realLinAlg();
                    Matrix<BigDecimal> nullMatrix1 = new Matrix<>(inputMatrix.getHeight(), inputMatrix.getWidth());
                    DRes<Matrix<DRes<SReal>>> input = (myId == 2)
                            ? realLinearAlgebra.input(inputMatrix, 2) : realLinearAlgebra.input(nullMatrix1, 2);
                    Matrix<BigDecimal> nullMatrix2 = new Matrix<>(weightMatrix.getHeight(), weightMatrix.getWidth());
                    DRes<Matrix<DRes<SReal>>> weight = (myId == 2)
                            ? realLinearAlgebra.input(weightMatrix, 2) : realLinearAlgebra.input(nullMatrix2, 2);
                    Matrix<BigDecimal> nullMatrix3 = new Matrix<>(biasMatrix.getHeight(), biasMatrix.getWidth());
                    DRes<Matrix<DRes<SReal>>> bias = (myId == 2)
                            ? realLinearAlgebra.input(biasMatrix, 2) : realLinearAlgebra.input(nullMatrix3, 2);
                    Pair<DRes<Matrix<DRes<SReal>>>, DRes<Matrix<DRes<SReal>>>> parameter = new Pair<>(weight, bias);
                    Pair<DRes<Matrix<DRes<SReal>>>, Pair<DRes<Matrix<DRes<SReal>>>, DRes<Matrix<DRes<SReal>>>>> pairPair = new Pair<>(input, parameter);
                    return () -> pairPair;
                }).seq((seq, input) -> {
                    now = System.currentTimeMillis();
                    System.out.println("********************** Start Time: " + now + " ms.");
                    DRes<Matrix<DRes<SReal>>> res = seq.seq(new PrivacyConvLayerBench(convLayerParameters, input.getFirst(), input.getSecond().getFirst(), input.getSecond().getSecond(), false));
                    return () -> new Matrix<>(1, 1, new ArrayList<>(new ArrayList<>()));
                });
            } else { // max pool
                return producer.par(par -> {
                    // Input element
                    RealLinearAlgebra realLinearAlgebra = par.realLinAlg();
                    Matrix<BigDecimal> nullMatrix1 = new Matrix<>(inputMatrix.getHeight(), inputMatrix.getWidth());
                    DRes<Matrix<DRes<SReal>>> input = (myId == 2)
                            ? realLinearAlgebra.input(inputMatrix, 2) : realLinearAlgebra.input(nullMatrix1, 2);
                    return () -> input;
                }).seq((seq, input) -> {
                    now = System.currentTimeMillis();
                    System.out.println("********************** Start Time: " + now + " ms.");
                    DRes<Matrix<DRes<SReal>>> res = seq.seq(new MaxpoolingLayerBench(maxpoolingLayerParameters, input, false));
                    return () -> new Matrix<>(1, 1, new ArrayList<>(new ArrayList<>()));
                });
            }
        }
    }

    public static <ResourcePoolT extends ResourcePool> void main(String[] args) throws IOException {
        CmdLineUtil<ResourcePoolT, ProtocolBuilderNumeric> cmdUtil = new CmdLineUtil<>();
        // use Mdsonic or spdz2k+
        cmdUtil.addOption(Option.builder("sonic").desc("Use the MD-SONIC protocol.").required(false).hasArg(false).build());

        // benchmark type
        cmdUtil.addOption(Option.builder("mult").desc("Benchmark multiplication.").required(false).hasArg(false).build());
        cmdUtil.addOption(Option.builder("cmp").desc("Benchmark comparison.").required(false).hasArg(false).build());
        cmdUtil.addOption(Option.builder("matmult").desc("Benchmark matrix multiplication.").required(false).hasArg(false).build());
        cmdUtil.addOption(Option.builder("relu").desc("Benchmark multiplication.").required(false).hasArg(false).build());
        cmdUtil.addOption(Option.builder("conv").desc("Benchmark relu.").required(false).hasArg(false).build());
        cmdUtil.addOption(Option.builder("mp").desc("Benchmark maxpool.").required(false).hasArg(false).build());

        // if benchmark mult and cmp
        cmdUtil.addOption(Option.builder("n").desc("The number of benchmark.").required(false).hasArg().build());

        // if benchmark matmult and relu
        cmdUtil.addOption(Option.builder("h1").desc("The height of matrix A.").required(false).hasArg().build());
        cmdUtil.addOption(Option.builder("w1").desc("The width of matrix A").required(false).hasArg().build());
        cmdUtil.addOption(Option.builder("h2").desc("The height of matrix B.").required(false).hasArg().build());
        cmdUtil.addOption(Option.builder("w2").desc("The width of matrix B").required(false).hasArg().build());

        // if bench conv
        cmdUtil.addOption(Option.builder("cis").desc("Image size").required(false).hasArg().build());
        cmdUtil.addOption(Option.builder("cc").desc("Input features / channels").required(false).hasArg().build());
        cmdUtil.addOption(Option.builder("co").desc("Output channels / the number of kernels").required(false).hasArg().build());
        cmdUtil.addOption(Option.builder("cf").desc("Kernel size").required(false).hasArg().build());
        cmdUtil.addOption(Option.builder("cs").desc("Stride").required(false).hasArg().build());

        // if bench conv
        cmdUtil.addOption(Option.builder("mis").desc("Image size").required(false).hasArg().build());
        cmdUtil.addOption(Option.builder("mc").desc("Input features / channels").required(false).hasArg().build());
        cmdUtil.addOption(Option.builder("mf").desc("Pool size").required(false).hasArg().build());
        cmdUtil.addOption(Option.builder("ms").desc("Stride").required(false).hasArg().build());


        CommandLine cmd = cmdUtil.parse(args);

        boolean useMdsonic = false;
        if (cmd.hasOption("sonic")) {
            useMdsonic = true;
        }

        TestType testType;
        if (cmd.hasOption("mult")) {
            testType = TestType.MULT;
        } else if (cmd.hasOption("cmp")) {
            testType = TestType.CMP;
        } else if (cmd.hasOption("matmult")) {
            testType = TestType.MATMUL;
        } else if (cmd.hasOption("relu")) {
            testType = TestType.RELU;
        } else if (cmd.hasOption("conv")) {
            testType = TestType.CONV;
        } else if (cmd.hasOption("mp")) {
            testType = TestType.MP;
        } else {
            return;
        }

        NetworkConfiguration networkConfiguration = cmdUtil.getNetworkConfiguration();
        BenchDemo benchDemo;

        if (testType == TestType.MULT || testType == TestType.CMP) {
            // Padding benchDemo
            int n = Integer.parseInt(cmd.getOptionValue("n"));
            benchDemo = new BenchDemo(testType, networkConfiguration.getMyId(), useMdsonic, n);
        } else if (testType == TestType.MATMUL) {
            int h1 = Integer.parseInt(cmd.getOptionValue("h1"));
            int w1 = Integer.parseInt(cmd.getOptionValue("w1"));
            int h2 = Integer.parseInt(cmd.getOptionValue("h2"));
            int w2 = Integer.parseInt(cmd.getOptionValue("w2"));

            ArrayList<BigDecimal> rowA = new ArrayList<>(w1);
            for (int i = 0; i < w1; i++) {
                rowA.add(BigDecimal.valueOf(0));  // maybe 0 ?
            }
            ArrayList<BigDecimal> rowB = new ArrayList<>(w2);
            for (int i = 0; i < w2; i++) {
                rowB.add(BigDecimal.valueOf(0));
            }
            Matrix<BigDecimal> A = new Matrix<>(h1, w1, i -> rowA);
            Matrix<BigDecimal> B = new Matrix<>(h2, w2, i -> rowB);
            benchDemo = new BenchDemo(testType, networkConfiguration.getMyId(), A, B, useMdsonic);
        } else if (testType == TestType.RELU) {
            int h1 = Integer.parseInt(cmd.getOptionValue("h1"));
            int w1 = Integer.parseInt(cmd.getOptionValue("w1"));

            ArrayList<BigDecimal> rowA = new ArrayList<>(w1);
            for (int i = 0; i < w1; i++) {
                rowA.add(BigDecimal.valueOf(0));  // maybe 0 ?
            }

            Matrix<BigDecimal> A = new Matrix<>(h1, w1, i -> rowA);
            benchDemo = new BenchDemo(testType, networkConfiguration.getMyId(), A, useMdsonic);
        } else if (testType == TestType.CONV) {
            int imageHeight = Integer.parseInt(cmd.getOptionValue("cis"));
            int imageWidth = imageHeight;
            int inputFeatures = Integer.parseInt(cmd.getOptionValue("cc"));
            int filters = Integer.parseInt(cmd.getOptionValue("co"));
            int filterSize = Integer.parseInt(cmd.getOptionValue("cf"));
            int stride = Integer.parseInt(cmd.getOptionValue("cs"));
            int padding = 0;
            int batchSize = 1;
            ConvLayerParametersBench parameters = new ConvLayerParametersBench(imageWidth, imageHeight, inputFeatures, filters, filterSize, stride, padding, batchSize);

            // Construct input, weight, bias
            Matrix<BigDecimal> inputMatrix = parameters.constructInput();
            Matrix<BigDecimal> weightMatrix = parameters.constructWeight();
            Matrix<BigDecimal> biasMatrix = parameters.constructBias();

            benchDemo = new BenchDemo(testType, networkConfiguration.getMyId(), parameters, inputMatrix, weightMatrix, biasMatrix, useMdsonic);
        } else {  // Maxpool
            int imageHeight = Integer.parseInt(cmd.getOptionValue("mis"));
            int imageWidth = imageHeight;
            int inputFeatures = Integer.parseInt(cmd.getOptionValue("mc"));
            int poolSize = Integer.parseInt(cmd.getOptionValue("mf"));
            int stride = Integer.parseInt(cmd.getOptionValue("ms"));
            int batchSize = 1;
            MaxpoolingLayerParametersBench parameters = new MaxpoolingLayerParametersBench(imageWidth, imageHeight, inputFeatures, poolSize, stride, batchSize);

            // Construct input, weight, bias
            Matrix<BigDecimal> inputMatrix = parameters.constructInput();

            benchDemo = new BenchDemo(testType, networkConfiguration.getMyId(), parameters, inputMatrix, useMdsonic);
        }

        SecureComputationEngine<ResourcePoolT, ProtocolBuilderNumeric> sce = cmdUtil.getSce();
        cmdUtil.startNetwork();
        ResourcePoolT resourcePool = cmdUtil.getResourcePool();
        Matrix<BigDecimal> result = sce.runApplication(benchDemo, resourcePool, cmdUtil.getNetwork());
        benchDemo.then = System.currentTimeMillis();
        System.out.println("********************** End Time: " + benchDemo.then + " ms.");
        System.out.println("********************** Duration: " + (benchDemo.then - benchDemo.now) + "ms.");
        cmdUtil.closeNetwork();
        sce.shutdownSCE();

    }
}
