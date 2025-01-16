build: install move

install:
	cd ../.. && mvn clean install -DskipTests

move:
	mkdir -p server1;
	mkdir -p server2;
	cp target/fresco-demo-nn.jar server1;
	cp target/fresco-demo-nn.jar server2;


runMiniONNSpdz2k64:
	cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -n 1 -Net MiniONN -l -dataset mnist -path /home/fresco-md-sonic/ > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -n 1 -Net MiniONN -l -dataset mnist -path /home/fresco-md-sonic/ > log.txt 2>&1 &

#runSpdz2k32+TinyOT:
#	cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic32  -n 1 -Net MiniONN -sonic -l -dataset mnist -path /home/fresco-md-sonic/ > log.txt 2>&1 &
#	cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic32  -n 1 -Net MiniONN -sonic -l -dataset mnist -path /home/fresco-md-sonic/ > log.txt 2>&1 &
#
#runSpdz2k64+TinyOT:
#	cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64  -n 1 -Net MiniONN -sonic -l -dataset mnist -path /home/fresco-md-sonic/ > log.txt 2>&1 &
#	cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64  -n 1 -Net MiniONN -sonic -l -dataset mnist -path /home/fresco-md-sonic/ > log.txt 2>&1 &

runMiniONNSonic64:
	cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -n 1 -Net MiniONN -sonic -l -dataset mnist -path /home/fresco-md-sonic/ > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -n 1 -Net MiniONN -sonic -l -dataset mnist -path /home/fresco-md-sonic/ > log.txt 2>&1 &

runMiniONNMdml:
	cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -n 1 -Net MiniONN -l -dataset mnist -path /home/fresco-md-sonic/ > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -n 1 -Net MiniONN -l -dataset mnist -path /home/fresco-md-sonic/ > log.txt 2>&1 &



runSecureMLSpdz2k64:
	cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -n 1 -Net SecureML -l -dataset mnist -path /home/fresco-md-sonic/ > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -n 1 -Net SecureML -l -dataset mnist -path /home/fresco-md-sonic/ > log.txt 2>&1 &

runSecureMLSonic64:
	cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -n 1 -Net SecureML -sonic -l -dataset mnist -path /home/fresco-md-sonic/ > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -n 1 -Net SecureML -sonic -l -dataset mnist -path /home/fresco-md-sonic/ > log.txt 2>&1 &

runSecureMLMdml:
	cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -n 1 -Net SecureML -l -dataset mnist -path /home/fresco-md-sonic/ > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -n 1 -Net SecureML -l -dataset mnist -path /home/fresco-md-sonic/ > log.txt 2>&1 &



runSardaSpdz2k64:
	cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -n 1 -Net Sarda -l -dataset mnist -path /home/fresco-md-sonic/ > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -n 1 -Net Sarda -l -dataset mnist -path /home/fresco-md-sonic/ > log.txt 2>&1 &

runSardaSonic64:
	cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -n 1 -Net Sarda -sonic -l -dataset mnist -path /home/fresco-md-sonic/ > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -n 1 -Net Sarda -sonic -l -dataset mnist -path /home/fresco-md-sonic/ > log.txt 2>&1 &

runSardaMdml:
	cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -n 1 -Net Sarda -l -dataset mnist -path /home/fresco-md-sonic/ > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -n 1 -Net Sarda -l -dataset mnist -path /home/fresco-md-sonic/ > log.txt 2>&1 &


runLeNetSpdz2k64:
	cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -n 1 -Net LeNet -l -dataset mnist -path /home/fresco-md-sonic/ > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -n 1 -Net LeNet -l -dataset mnist -path /home/fresco-md-sonic/ > log.txt 2>&1 &

runLeNetSonic64:
	cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -n 1 -Net LeNet -sonic -l -dataset mnist -path /home/fresco-md-sonic/ > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -n 1 -Net LeNet -sonic -l -dataset mnist -path /home/fresco-md-sonic/ > log.txt 2>&1 &

runLeNetMdml:
	cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -n 1 -Net LeNet -l -dataset mnist -path /home/fresco-md-sonic/ > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -n 1 -Net LeNet -l -dataset mnist -path /home/fresco-md-sonic/ > log.txt 2>&1 &



runLeNet5MnistSpdz2k64:
	cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -n 1 -Net LeNet-5 -l -dataset mnist -path /home/fresco-md-sonic/ > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -n 1 -Net LeNet-5 -l -dataset mnist -path /home/fresco-md-sonic/ > log.txt 2>&1 &

runLeNet5MnistSonic64:
	cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -n 1 -Net LeNet-5 -sonic -l -dataset mnist -path /home/fresco-md-sonic/ > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -n 1 -Net LeNet-5 -sonic -l -dataset mnist -path /home/fresco-md-sonic/ > log.txt 2>&1 &

runLeNet5MnistMdml:
	cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -n 1 -Net LeNet-5 -l -dataset mnist -path /home/fresco-md-sonic/ > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -n 1 -Net LeNet-5 -l -dataset mnist -path /home/fresco-md-sonic/ > log.txt 2>&1 &


runLeNet5Cifar10Spdz2k64:
	cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -n 1 -Net LeNet-5 -l -dataset CIFAR10 -path /home/fresco-md-sonic/ > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -n 1 -Net LeNet-5 -l -dataset CIFAR10 -path /home/fresco-md-sonic/ > log.txt 2>&1 &

runLeNet5Cifar10Sonic64:
	cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -n 1 -Net LeNet-5 -sonic -l -dataset CIFAR10 -path /home/fresco-md-sonic/ > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -n 1 -Net LeNet-5 -sonic -l -dataset CIFAR10 -path /home/fresco-md-sonic/ > log.txt 2>&1 &

runLeNet5Cifar10Mdml:
	cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -n 1 -Net LeNet-5 -l -dataset CIFAR10 -path /home/fresco-md-sonic/ > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -n 1 -Net LeNet-5 -l -dataset CIFAR10 -path /home/fresco-md-sonic/ > log.txt 2>&1 &


runLeNet5TinyImageSpdz2k64:
	cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -n 1 -Net LeNet-5 -l -dataset TinyImageNet -path /home/fresco-md-sonic/ > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -n 1 -Net LeNet-5 -l -dataset TinyImageNet -path /home/fresco-md-sonic/ > log.txt 2>&1 &

runLeNet5TinyImageSonic64:
	cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -n 1 -Net LeNet-5 -sonic -l -dataset TinyImageNet -path /home/fresco-md-sonic/ > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -n 1 -Net LeNet-5 -sonic -l -dataset TinyImageNet -path /home/fresco-md-sonic/ > log.txt 2>&1 &

runLeNet5TinyImageMdml:
	cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -n 1 -Net LeNet-5 -l -dataset TinyImageNet -path /home/fresco-md-sonic/ > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -n 1 -Net LeNet-5 -l -dataset TinyImageNet -path /home/fresco-md-sonic/ > log.txt 2>&1 &



runAlexNetCifar10Spdz2k64:
	cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -n 1 -Net AlexNet -l -dataset CIFAR10 -path /home/fresco-md-sonic/ > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -n 1 -Net AlexNet -l -dataset CIFAR10 -path /home/fresco-md-sonic/ > log.txt 2>&1 &

runAlexNetCifar10Sonic64:
	cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -n 1 -Net AlexNet -sonic -l -dataset CIFAR10 -path /home/fresco-md-sonic/ > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -n 1 -Net AlexNet -sonic -l -dataset CIFAR10 -path /home/fresco-md-sonic/ > log.txt 2>&1 &

runAlexNetCifar10Mdml:
	cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -n 1 -Net AlexNet -l -dataset CIFAR10 -path /home/fresco-md-sonic/ > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -n 1 -Net AlexNet -l -dataset CIFAR10 -path /home/fresco-md-sonic/ > log.txt 2>&1 &




runAlexNetTinyImageSpdz2k64:
	cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -n 1 -Net AlexNet -l -dataset TinyImageNet -path /home/fresco-md-sonic/ > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -n 1 -Net AlexNet -l -dataset TinyImageNet -path /home/fresco-md-sonic/ > log.txt 2>&1 &

runAlexNetTinyImageSonic64:
	cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -n 1 -Net AlexNet -sonic -l -dataset TinyImageNet -path /home/fresco-md-sonic/ > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -n 1 -Net AlexNet -sonic -l -dataset TinyImageNet -path /home/fresco-md-sonic/ > log.txt 2>&1 &

runAlexNetTinyImageMdml:
	cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -n 1 -Net AlexNet -l -dataset TinyImageNet -path /home/fresco-md-sonic/ > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -n 1 -Net AlexNet -l -dataset TinyImageNet -path /home/fresco-md-sonic/ > log.txt 2>&1 &



runAlexNetImageSpdz2k64:
	cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -n 1 -Net AlexNet -l -dataset ImageNet -path /home/fresco-md-sonic/ > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -n 1 -Net AlexNet -l -dataset ImageNet -path /home/fresco-md-sonic/ > log.txt 2>&1 &

runAlexNetImageSonic64:
	cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -n 1 -Net AlexNet -sonic -l -dataset ImageNet -path /home/fresco-md-sonic/ > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -n 1 -Net AlexNet -sonic -l -dataset ImageNet -path /home/fresco-md-sonic/ > log.txt 2>&1 &

runAlexNetImageMdml:
	cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -n 1 -Net AlexNet -l -dataset ImageNet -path /home/fresco-md-sonic/ > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -n 1 -Net AlexNet -l -dataset ImageNet -path /home/fresco-md-sonic/ > log.txt 2>&1 &



runResNetCifar10Spdz2k64:
	cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -n 1 -Net ResNet-18 -l -dataset CIFAR10 -path /home/fresco-md-sonic/ > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -n 1 -Net ResNet-18 -l -dataset CIFAR10 -path /home/fresco-md-sonic/ > log.txt 2>&1 &

runResNetCifar10Sonic64:
	cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -n 1 -Net ResNet-18 -sonic -l -dataset CIFAR10 -path /home/fresco-md-sonic/ > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -n 1 -Net ResNet-18 -sonic -l -dataset CIFAR10 -path /home/fresco-md-sonic/ > log.txt 2>&1 &

runResNetCifar10Mdml:
	cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -n 1 -Net ResNet-18 -l -dataset CIFAR10 -path /home/fresco-md-sonic/ > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -n 1 -Net ResNet-18 -l -dataset CIFAR10 -path /home/fresco-md-sonic/ > log.txt 2>&1 &




runResNetImageSpdz2k64:
	cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -n 1 -Net ResNet-18 -l -dataset TinyImageNet -path /home/fresco-md-sonic/ > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -n 1 -Net ResNet-18 -l -dataset TinyImageNet -path /home/fresco-md-sonic/ > log.txt 2>&1 &

runResNetImageSonic64:
	cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -n 1 -Net ResNet-18 -sonic -l -dataset TinyImageNet -path /home/fresco-md-sonic/ > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -n 1 -Net ResNet-18 -sonic -l -dataset TinyImageNet -path /home/fresco-md-sonic/ > log.txt 2>&1 &

runResNetImageMdml:
	cd server1 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -n 1 -Net ResNet-18 -l -dataset TinyImageNet -path /home/fresco-md-sonic/ > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-nn.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -n 1 -Net ResNet-18 -l -dataset TinyImageNet -path /home/fresco-md-sonic/ > log.txt 2>&1 &