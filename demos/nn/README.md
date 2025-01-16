
# Demos: Neural Network Inference
This is an application of neural network inference, related to the secure inference testing experiments described in the paper.

## Implementation Detail

### Secure Inference
We implemented the JAVA and FRESCO versions of the [Falcon](https://arxiv.org/pdf/2004.02229) neural network frontend, supporting operations such as convolution, fully connected layers, ReLU, max pooling, average pooling, and residual connections. With the exception of residual connections, all operations are designed to be pluggable and configurable, allowing for easy extension to other neural networks. Additionally, all model parameters and input images are read from CSV files.

Once the neural network application is constructed, secure inference can be performed using MPC protocols. We provide three protocols: `SPDZ2k+`, `MD-ML`, and `MD-SONIC`. Upon completion of the inference, the runtime and communication overhead are reported. It is important to note that the statistics collected *do not* account for the input of weight parameters or images. Additionally, for AlexNet and ResNet-18, due to their high costs, we do not support batch inference for multiple images now. Therefore, the `-n` parameter for these two neural networks is restricted to 1.

For AlexNet on the CIFAR10 and TinyImageNet datasets, we adopt the same network architecture as Falcon. For AlexNet on the ImageNet dataset, we utilize the same network architecture as MD-ML. For LeNet and ResNet-18, we employ the widely-known network architectures.

### Accuracy Test
We only provide accuracy tests for Network-A, Network-B, Network-C, and LeNet-5 over the MNIST dataset. Our accuracy testing code has been integrated into `NNDemo`, and all scripts are located in `scripts/Mnist/`.

Let me show the details of how to conduct the accuracy experiment for `Network-A` in the [SecureML](https://eprint.iacr.org/2017/396.pdf).

1. Set up the Python environment, run `scripts/Mnist/SecureML.py` to train Network-A, and save the final trained model parameters and test samples from test set.

2. Use the script `scripts/txt2csv.py` to convert the files into CSV format and move them to `networks/SecureML/csv/`. Currently, this folder contains the weights and tests obtained from our previous training.

3. Modify the `-n` parameter of the two commands under `runSecureMLSonic64` in the `Makefile` to 128, and add the `-batch` parameter to each command to specify the folder containing the test cases. For example, for folder `networks/SecureML/csv/batch2/`, the corresponding `-batch` would be batch2.
   ```shell
   runSecureMLSonic64:
       cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -n 128 -Net SecureML -sonic -l -dataset mnist -batch batch2 -path /home/fresco-md-sonic/ > log.txt 2>&1 &
       cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -n 128 -Net SecureML -sonic -l -dataset mnist -batch batch2 -path /home/fresco-md-sonic/ > log.txt 2>&1 &
   ```

4. Run the following command to obtain the accuracy and relative error of MD-SONIC inference for Network-A.
   ```shell
   make move
   make runSecureMLSonic64
   ```


