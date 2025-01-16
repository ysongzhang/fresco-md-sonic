# MD-SONIC: Maliciously-Secure Outsourcing Neural Network Inference with Reduced Online Communication
This is the repo of the paper `MD-SONIC: Maliciously-Secure Outsourcing Neural Network Inference with Reduced Online Communication`. It provides an communication efficient and maliciously-secure framework for outsourcing neural network inference with a dishonest majority. This work builds on the [spdz2k-develop](https://github.com/aicis/fresco/tree/spdz2k-develop) branch of [FRESCO](http://github.com/aicis/fresco). It leverages FRESCO's core modules, such as secure communication and application-building primitives, to provide an efficient and customizable solution.

### Table of Contents
1. [Warning](#warning)
2. [Requirement](#requirement)
3. [Source Code](#source-code)
   1. [Repository Structure](#repository-structure)
   2. [Installation](#installation)
4. [Running the Benchmark](#running-the-benchmark)
5. [Running the neural network inference](#running-the-neural-network-inference)

## Warning

This codebase is released solely as a reference for other developers, as a proof-of-concept, and for benchmarking purposes. In particular, it has not had any security review, has a number of implementational TODOs, and thus, should be used at your own risk.



## Requirement

This code works on Linux, MacOS, and Windows.

- The experiments in the paper are conducted with Ubuntu 22.04 LTS.

Requirement packages:

- Java 8
- Maven


## Source Code

We only present the parts that we have added or modified. If you have any questions regarding the FRESCO framework itself, please refer to its official documentation: [http://fresco.readthedocs.org](http://fresco.readthedocs.org).

### Repository Structure

- `core`: The FRESCO core module that provides the lowest-level primitives for software, including network, MPC protocol builder, application builder, evaluator, and logger, etc. To integrate MD-SONIC, we add a serial of computation interfaces (ComputationDictionary) related to MD-SONIC in both the `framework/builder/numeric/mdsonic/` and `lib/real/mdsonic/`.

- `demos/bench`: The testing application to benchmark the throughput of various MPC protocols for multiplication and comparison, as well as to benchmark the costs of the NN building blocks.

- `demos/nn`: The application to evaluate NN inference across the `MNIST`, `CIFAR-10`, `TinyImageNet`, and `ImageNet` datasets using 6 standard neural networks: 3 from the PPML community (`Network-A`, `Network-B` and `Network-C` in the paper) and 3 from the ML community (`LeNet`, `AlexNet` and `ResNet-18`). For implementation details, see `demos/nn/README.md`.

- `suite/mdsonic`: The source code for our protocol, MD-SONIC, includes definitions of basic data types and secret sharing, computations for fundamental protocols such as addition, multiplication, input and output, and constructions for protocols like comparison and ReLU. For implementation details, see `suite/mdsonic/README.md`.

- `suite/mdml`: The source code for [MD-ML](https://www.usenix.org/system/files/usenixsecurity24-yuan.pdf), which we reproduce in the FRESCO framework for fair comparison.

- `suite/spdz2k`: The SPDZ2k+ protocol in the paper that is implemented in the spdz2k-develop branch of FRESCO. We fix a security bug in this protocol related to boolean circuit computations, which is reported in Section 3.4 of the [SPDZ2k](https://eprint.iacr.org/2018/482.pdf) paper.

- `networks`: Contains neural network model weight files and input/output files.

- `scripts`: Contains python code to generate trained models for accuracy testing over a batch.


### Installation

To install MD-SONIC, run the following commands:

```shell
cd fresco-md-sonic
mvn install -DskipTests
```

If you use Maven for your project, you can then use one of the protocols included in the `suite/` in your project by dding it as a dependency in your projects POM file.
For instance, if you want to use our MD-SONIC protocol, your POM file will need to include:

```shell
<dependency>
    <groupId>dk.alexandra.fresco</groupId>
    <artifactId>mdsonic</artifactId>
    <version>1.1.5-SNAPSHOT</version>
</dependency>
```

## Running the Benchmark

After the installation is complete, execute the following command to perform a performance test of the building blocks.

```shell
cd demos/bench
make move
make runMultSonic64
```

You can refer to the `demos/bench/Makefile` file for more detailed instructions and additional test examples. All tests can be evaluated using `SPDZ2k+`, `MD-ML`, and our `MD-SONIC`.

Let me provide an example of the command `runMultSonic64` to elaborate on the parameter information in detail.

```shell
runMultSonic64:
  cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -mult -n 100000 > log.txt 2>&1 &
  cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -mult -n 100000 > log.txt 2>&1 &
```

- `-s`: Specify the MPC protocol to be used.
- `-sonic`: Inform `BenchDemo` to use the sonic-type computation dictionary.
- `-mult`: Specify the test type is the multiplication. We provide six types now: `mult`, `cmp`, `matmult`, `relu`, `conv`, `mp`.
- `-n`: Specify the number of testing multiplications.




## Running the neural network inference

Similar to the benchmark, we can evaluate neural network inference by the following command:

```shell
cd demos/nn
make move
make runSecureMLSonic64
```

All commands are located in `demos/nn/Makefile`.

Let me show the details of the `runSecureMLSonic64` command for Network-A over MNIST dataset.
```shell
runSecureMLSonic64:
  cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -n 1 -Net SecureML -sonic -l -dataset mnist -path /home/fresco-md-sonic/ > log.txt 2>&1 &
  cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -n 1 -Net SecureML -sonic -l -dataset mnist -path /home/fresco-md-sonic/> log.txt 2>&1 &
```
- `-s`: Specify the MPC protocol to be used.
- `-sonic`: Inform `NNDemo` to use the sonic-type computation dictionary.
- `-Net`: Specify the test neural network is Network-A from SecureML. We provide six types now: `SecureML`, `Sarda`, `MiniONN`, `LeNet-5`, `AlexNet`, `ResNet-18`.
- `-dataset`: Specify the dataset for neural network computation. We provide four types now: `mnist`, `CIFAR-10`, `TinyImageNet`, `ImageNet`.
- `-n`: Specify the number of images for inference, no more than 128.
- `-path`: Specify the paths for the weight files, input, and output, that is the path of the `networks` folder.
- `-batch`: Specify the subpath for test cases (input, output and labels), defaulting to `batch1`, corresponding to the folder `/home/fresco-md-sonic/networks/SecureML/csv/batch1/`.
- `-D sonic.useMaskedEvaluation=true`: When it is set to `true`, it indicates the use of circuit-dependent techniques, and the MPC protocol running will be the MD-SONIC as described in the paper. When it is set to `false`, circuit-dependent techniques are not used; for detailed information on the protocol in use, please refer to `suite/mdsonic/README.md`.

For more details, see `demos/nn/README.md`.