build: install move

install:
	cd ../.. && mvn clean install -DskipTests

move:
	mkdir -p server1;
	mkdir -p server2;
	cp target/fresco-demo-bench.jar server1;
	cp target/fresco-demo-bench.jar server2;

runMultSpdz2k64:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -l -mult -n 100000 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -l -mult -n 100000 > log.txt 2>&1 &

#runMultSpdz2k64+TinyOT:
#	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -sonic -l -mult -n 100000 > log.txt 2>&1 &
#	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -sonic -l -mult -n 100000 > log.txt 2>&1 &

runMultSonic64:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -mult -n 100000 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -mult -n 100000 > log.txt 2>&1 &

runMultMdml:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -mult -n 100000 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -mult -n 100000 > log.txt 2>&1 &


runCMPSpdz2k64:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -l -cmp -n 5000 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -l -cmp -n 5000 > log.txt 2>&1 &

runCMPSonic64:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -cmp -n 5000 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -cmp -n 5000 > log.txt 2>&1 &

runCMPMdml:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -cmp -n 5000 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -cmp -n 5000 > log.txt 2>&1 &


runMatMulSpdz2k64_1:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -l -matmult -h1 128 -w1 128 -h2 128 -w2 128 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -l -matmult -h1 128 -w1 128 -h2 128 -w2 128 > log.txt 2>&1 &

runMatMulSonic64_1:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -matmult -h1 128 -w1 128 -h2 128 -w2 128 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -matmult -h1 128 -w1 128 -h2 128 -w2 128 > log.txt 2>&1 &

runMatMulMdml_1:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -matmult -h1 128 -w1 128 -h2 128 -w2 128 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -matmult -h1 128 -w1 128 -h2 128 -w2 128 > log.txt 2>&1 &


runMatMulSpdz2k64_2:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -l -matmult -h1 128 -w1 2048 -h2 2048 -w2 10 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -l -matmult -h1 128 -w1 2048 -h2 2048 -w2 10 > log.txt 2>&1 &

runMatMulSonic64_2:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -matmult -h1 128 -w1 2048 -h2 2048 -w2 10 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -matmult -h1 128 -w1 2048 -h2 2048 -w2 10 > log.txt 2>&1 &

runMatMulMdml_2:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -matmult -h1 128 -w1 2048 -h2 2048 -w2 10 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -matmult -h1 128 -w1 2048 -h2 2048 -w2 10 > log.txt 2>&1 &


runReluSpdz2k64_1:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -l -relu -h1 32 -w1 32 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -l -relu -h1 32 -w1 32 > log.txt 2>&1 &

runReluSonic64_1:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -relu -h1 32 -w1 32 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -relu -h1 32 -w1 32 > log.txt 2>&1 &

runReluMdml_1:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -relu -h1 32 -w1 32 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -relu -h1 32 -w1 32 > log.txt 2>&1 &


runReluSpdz2k64_2:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -l -relu -h1 128 -w1 128 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -l -relu -h1 128 -w1 128 > log.txt 2>&1 &

runReluSonic64_2:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -relu -h1 128 -w1 128 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -relu -h1 128 -w1 128 > log.txt 2>&1 &

runReluMdml_2:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -relu -h1 128 -w1 128 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -relu -h1 128 -w1 128 > log.txt 2>&1 &


runConvSpdz2k64_1:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -l -conv -cis 28 -cc 1 -co 1 -cf 5 -cs 1 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -l -conv -cis 28 -cc 1 -co 1 -cf 5 -cs 1 > log.txt 2>&1 &

runConvSonic64_1:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -conv -cis 28 -cc 1 -co 1 -cf 5 -cs 1 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -conv -cis 28 -cc 1 -co 1 -cf 5 -cs 1 > log.txt 2>&1 &

runConvMdml_1:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -conv -cis 28 -cc 1 -co 1 -cf 5 -cs 1 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -conv -cis 28 -cc 1 -co 1 -cf 5 -cs 1 > log.txt 2>&1 &


runConvSpdz2k64_2:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -l -conv -cis 8 -cc 16 -co 120 -cf 5 -cs 1 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -l -conv -cis 8 -cc 16 -co 120 -cf 5 -cs 1 > log.txt 2>&1 &

runConvSonic64_2:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -conv -cis 8 -cc 16 -co 120 -cf 5 -cs 1 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -conv -cis 8 -cc 16 -co 120 -cf 5 -cs 1 > log.txt 2>&1 &

runConvMdml_2:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -conv -cis 8 -cc 16 -co 120 -cf 5 -cs 1 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -conv -cis 8 -cc 16 -co 120 -cf 5 -cs 1 > log.txt 2>&1 &


runMpSpdz2k64_1:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -l -mp -mis 24 -mc 16 -mf 2 -ms 2 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -l -mp -mis 24 -mc 16 -mf 2 -ms 2 > log.txt 2>&1 &

runMpSonic64_1:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -mp -mis 24 -mc 16 -mf 2 -ms 2 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -mp -mis 24 -mc 16 -mf 2 -ms 2 > log.txt 2>&1 &

runMpMdml_1:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -mp -mis 24 -mc 16 -mf 2 -ms 2 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -mp -mis 24 -mc 16 -mf 2 -ms 2 > log.txt 2>&1 &


runMpSpdz2k64_2:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -l -mp -mis 8 -mc 50 -mf 4 -ms 4 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -l -mp -mis 8 -mc 50 -mf 4 -ms 4 > log.txt 2>&1 &

runMpSonic64_2:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -mp -mis 8 -mc 50 -mf 4 -ms 4 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -mp -mis 8 -mc 50 -mf 4 -ms 4 > log.txt 2>&1 &

runMpMdml_2:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -mp -mis 8 -mc 50 -mf 4 -ms 4 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -mp -mis 8 -mc 50 -mf 4 -ms 4 > log.txt 2>&1 &

