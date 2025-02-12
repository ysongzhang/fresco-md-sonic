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
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -l -matmult -h1 1 -w1 256 -h2 256 -w2 128 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -l -matmult -h1 1 -w1 256 -h2 256 -w2 128 > log.txt 2>&1 &

runMatMulSonic64_1:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -matmult -h1 1 -w1 256 -h2 256 -w2 128 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -matmult -h1 1 -w1 256 -h2 256 -w2 128 > log.txt 2>&1 &

runMatMulMdml_1:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -matmult -h1 1 -w1 256 -h2 256 -w2 128 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -matmult -h1 1 -w1 256 -h2 256 -w2 128 > log.txt 2>&1 &


runMatMulSpdz2k64_2:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -l -matmult -h1 16 -w1 256 -h2 256 -w2 128 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -l -matmult -h1 16 -w1 256 -h2 256 -w2 128 > log.txt 2>&1 &

runMatMulSonic64_2:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -matmult -h1 16 -w1 256 -h2 256 -w2 128 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -matmult -h1 16 -w1 256 -h2 256 -w2 128 > log.txt 2>&1 &

runMatMulMdml_2:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -matmult -h1 16 -w1 256 -h2 256 -w2 128 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -matmult -h1 16 -w1 256 -h2 256 -w2 128 > log.txt 2>&1 &


runMatMulSpdz2k64_3:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -l -matmult -h1 128 -w1 128 -h2 128 -w2 128 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -l -matmult -h1 128 -w1 128 -h2 128 -w2 128 > log.txt 2>&1 &

runMatMulSonic64_3:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -matmult -h1 128 -w1 128 -h2 128 -w2 128 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -matmult -h1 128 -w1 128 -h2 128 -w2 128 > log.txt 2>&1 &

runMatMulMdml_3:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -matmult -h1 128 -w1 128 -h2 128 -w2 128 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -matmult -h1 128 -w1 128 -h2 128 -w2 128 > log.txt 2>&1 &



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


runReluSpdz2k64_3:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -l -relu -h1 512 -w1 512 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -l -relu -h1 512 -w1 512 > log.txt 2>&1 &

runReluSonic64_3:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -relu -h1 512 -w1 512 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -relu -h1 512 -w1 512 > log.txt 2>&1 &

runReluMdml_3:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -relu -h1 512 -w1 512 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -relu -h1 512 -w1 512 > log.txt 2>&1 &



runConvSpdz2k64_1:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -l -conv -cis 28 -cc 1 -co 6 -cf 5 -cs 1 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -l -conv -cis 28 -cc 1 -co 6 -cf 5 -cs 1 > log.txt 2>&1 &

runConvSonic64_1:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -conv -cis 28 -cc 1 -co 6 -cf 5 -cs 1 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -conv -cis 28 -cc 1 -co 6 -cf 5 -cs 1 > log.txt 2>&1 &

runConvMdml_1:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -conv -cis 28 -cc 1 -co 6 -cf 5 -cs 1 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -conv -cis 28 -cc 1 -co 6 -cf 5 -cs 1 > log.txt 2>&1 &


runConvSpdz2k64_2:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -l -conv -cis 32 -cc 3 -co 6 -cf 5 -cs 1 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -l -conv -cis 32 -cc 3 -co 6 -cf 5 -cs 1 > log.txt 2>&1 &

runConvSonic64_2:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -conv -cis 32 -cc 3 -co 6 -cf 5 -cs 1 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -conv -cis 32 -cc 3 -co 6 -cf 5 -cs 1 > log.txt 2>&1 &

runConvMdml_2:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -conv -cis 32 -cc 3 -co 6 -cf 5 -cs 1 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -conv -cis 32 -cc 3 -co 6 -cf 5 -cs 1 > log.txt 2>&1 &


runConvSpdz2k64_3:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -l -conv -cis 64 -cc 3 -co 64 -cf 5 -cs 1 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -l -conv -cis 64 -cc 3 -co 64 -cf 5 -cs 1 > log.txt 2>&1 &

runConvSonic64_3:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -conv -cis 64 -cc 3 -co 64 -cf 5 -cs 1 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -conv -cis 64 -cc 3 -co 64 -cf 5 -cs 1 > log.txt 2>&1 &

runConvMdml_3:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -conv -cis 64 -cc 3 -co 64 -cf 5 -cs 1 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -conv -cis 64 -cc 3 -co 64 -cf 5 -cs 1 > log.txt 2>&1 &



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
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -l -mp -mis 15 -mc 96 -mf 3 -ms 3 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -l -mp -mis 15 -mc 96 -mf 3 -ms 3 > log.txt 2>&1 &

runMpSonic64_2:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -mp -mis 15 -mc 96 -mf 3 -ms 3 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -mp -mis 15 -mc 96 -mf 3 -ms 3 > log.txt 2>&1 &

runMpMdml_2:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -mp -mis 15 -mc 96 -mf 3 -ms 3 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -mp -mis 15 -mc 96 -mf 3 -ms 3 > log.txt 2>&1 &


runMpSpdz2k64_3:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -l -mp -mis 56 -mc 64 -mf 2 -ms 2 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s spdz2k64 -l -mp -mis 56 -mc 64 -mf 2 -ms 2 > log.txt 2>&1 &

runMpSonic64_3:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -mp -mis 56 -mc 64 -mf 2 -ms 2 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -mp -mis 56 -mc 64 -mf 2 -ms 2 > log.txt 2>&1 &

runMpMdml_3:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -mp -mis 56 -mc 64 -mf 2 -ms 2 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -mp -mis 56 -mc 64 -mf 2 -ms 2 > log.txt 2>&1 &




AlexNetSonic_1:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -conv -cis 228 -cc 3 -co 96 -cf 11 -cs 4 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -conv -cis 228 -cc 3 -co 96 -cf 11 -cs 4 > log.txt 2>&1 &
AlexNetSonic_2:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -relu -h1 290400 -w1 1 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -relu -h1 290400 -w1 1 > log.txt 2>&1 &
AlexNetSonic_3:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -conv -cis 31 -cc 96 -co 256 -cf 5 -cs 1 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -conv -cis 31 -cc 96 -co 256 -cf 5 -cs 1 > log.txt 2>&1 &
AlexNetSonic_4:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -relu -h1 186624 -w1 1 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -relu -h1 186624 -w1 1 > log.txt 2>&1 &
AlexNetSonic_5:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -conv -cis 15 -cc 256 -co 384 -cf 3 -cs 1 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -conv -cis 15 -cc 256 -co 384 -cf 3 -cs 1 > log.txt 2>&1 &
AlexNetSonic_6:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -relu -h1 64896 -w1 1 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -relu -h1 64896 -w1 1 > log.txt 2>&1 &
AlexNetSonic_7:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -conv -cis 15 -cc 384 -co 384 -cf 3 -cs 1 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -conv -cis 15 -cc 384 -co 384 -cf 3 -cs 1 > log.txt 2>&1 &
AlexNetSonic_8:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -relu -h1 64896 -w1 1 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -relu -h1 64896 -w1 1 > log.txt 2>&1 &
AlexNetSonic_9:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -conv -cis 15 -cc 384 -co 256 -cf 3 -cs 1 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -conv -cis 15 -cc 384 -co 256 -cf 3 -cs 1 > log.txt 2>&1 &
AlexNetSonic_10:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -relu -h1 43264 -w1 1 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -relu -h1 43264 -w1 1 > log.txt 2>&1 &
AlexNetSonic_11:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -matmult -h1 1 -w1 9216 -h2 9216 -w2 4096 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -matmult -h1 1 -w1 9216 -h2 9216 -w2 4096 > log.txt 2>&1 &
AlexNetSonic_12:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -relu -h1 4096 -w1 1 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -relu -h1 4096 -w1 1 > log.txt 2>&1 &
AlexNetSonic_13:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -matmult -h1 1 -w1 4096 -h2 4096 -w2 4096 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -matmult -h1 1 -w1 4096 -h2 4096 -w2 4096 > log.txt 2>&1 &
AlexNetSonic_14:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -relu -h1 4096 -w1 1 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -relu -h1 4096 -w1 1 > log.txt 2>&1 &
AlexNetSonic_15:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -matmult -h1 1 -w1 4096 -h2 4096 -w2 1000 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s sonic64 -D sonic.useMaskedEvaluation=true -sonic -l -matmult -h1 1 -w1 4096 -h2 4096 -w2 1000 > log.txt 2>&1 &


AlexNetMdml_1:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -conv -cis 228 -cc 3 -co 96 -cf 11 -cs 4 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -conv -cis 228 -cc 3 -co 96 -cf 11 -cs 4 > log.txt 2>&1 &
AlexNetMdml_2:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -relu -h1 290400 -w1 1 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -relu -h1 290400 -w1 1 > log.txt 2>&1 &
AlexNetMdml_3:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -conv -cis 31 -cc 96 -co 256 -cf 5 -cs 1 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -conv -cis 31 -cc 96 -co 256 -cf 5 -cs 1 > log.txt 2>&1 &
AlexNetMdml_4:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -relu -h1 186624 -w1 1 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -relu -h1 186624 -w1 1 > log.txt 2>&1 &
AlexNetMdml_5:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -conv -cis 15 -cc 256 -co 384 -cf 3 -cs 1 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -conv -cis 15 -cc 256 -co 384 -cf 3 -cs 1 > log.txt 2>&1 &
AlexNetMdml_6:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -relu -h1 64896 -w1 1 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -relu -h1 64896 -w1 1 > log.txt 2>&1 &
AlexNetMdml_7:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -conv -cis 15 -cc 384 -co 384 -cf 3 -cs 1 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -conv -cis 15 -cc 384 -co 384 -cf 3 -cs 1 > log.txt 2>&1 &
AlexNetMdml_8:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -relu -h1 64896 -w1 1 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -relu -h1 64896 -w1 1 > log.txt 2>&1 &
AlexNetMdml_9:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -conv -cis 15 -cc 384 -co 256 -cf 3 -cs 1 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -conv -cis 15 -cc 384 -co 256 -cf 3 -cs 1 > log.txt 2>&1 &
AlexNetMdml_10:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -relu -h1 43264 -w1 1 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -relu -h1 43264 -w1 1 > log.txt 2>&1 &
AlexNetMdml_11:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -matmult -h1 1 -w1 9216 -h2 9216 -w2 4096 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -matmult -h1 1 -w1 9216 -h2 9216 -w2 4096 > log.txt 2>&1 &
AlexNetMdml_12:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -relu -h1 4096 -w1 1 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -relu -h1 4096 -w1 1 > log.txt 2>&1 &
AlexNetMdml_13:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -matmult -h1 1 -w1 4096 -h2 4096 -w2 4096 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -matmult -h1 1 -w1 4096 -h2 4096 -w2 4096 > log.txt 2>&1 &
AlexNetMdml_14:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -relu -h1 4096 -w1 1 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -relu -h1 4096 -w1 1 > log.txt 2>&1 &
AlexNetMdml_15:
	cd server1 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 1 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -matmult -h1 1 -w1 4096 -h2 4096 -w2 1000 > log.txt 2>&1 &
	cd server2 && java -jar fresco-demo-bench.jar -e SEQUENTIAL_BATCHED -i 2 -p 1:localhost:8081 -p 2:localhost:8082 -s mdml -l -matmult -h1 1 -w1 4096 -h2 4096 -w2 1000 > log.txt 2>&1 &

