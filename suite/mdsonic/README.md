
# MD-SONIC
We implement MD-SONIC within the FRESCO framework. MD-SONIC employs an optimized secret-sharing scheme that seamlessly combines SPDZ2k-sharing with mask-sharing and TinyOT-sharing using edaBits and daBits. We provide the specific implementation details here.

### Table of Contents
1. [DataType](#datatype)
   1. [Arithmetic Secret Sharing](#arithmetic-secret-sharing)
   2. [Binary Secret Sharing](#binary-secret-sharing)
   3. [useMaskedEvaluation](#usemaskedevaluation)
2. [MAC Check](#mac-check)
3. [Functionality](#functionality)


## DataType

### Arithmetic Secret Sharing
Our arithmetic secret sharing is built on top of SPDZ2k-sharing and implemented using `long` types for storage and computation. Through bitwise shift operations, large integer addition and multiplication operations in $Z_{2^{64}}$ and $Z_{2^{128}}$ can be efficiently achieved. Details can be found in Section 6 of the paper [SPDZ2k+](https://eprint.iacr.org/2019/599.pdf).

### Binary Secret Sharing
The TinyOT secret sharing scheme is implemented using a combination of a `boolean` and a `byte array` (`byte[]`). The `boolean` stores the additive secret sharing of the secret value, while the `byte[]` stores the MACs. Each byte represents 8 bits, corresponding to 8 values in $F_2$. In our experiments, TinyOT employs two data structures: $F_{2^{32}}$ and $F_{2^{64}}$, corresponding to byte arrays of length 4 and 8, respectively.

During circuit computation, only bitwise operations are involved. And finite field computations over $F_{2^{32}}$ or $F_{2^{64}}$ occur only during the MAC check procedure. We achieve efficient finite field multiplications based on the following two libraries.

- [Rings](https://github.com/PoslavskySV/rings/): a JAVA library for finite field computations.
- [Bouncy Castle](https://github.com/bcgit/bc-java/tree/main): a JAVA library for cryptographic algorithms.

Specifically, the `implMul64` function in [GCMUtil.java](https://github.com/bcgit/bc-java/blob/main/core/src/main/java/org/bouncycastle/crypto/modes/gcm/GCMUtil.java) from Bouncy Castle achieves multiplication of two 64-bit polynomials each representing a degree-63 polynomials with coefficients in $F_2$. These 64-bit polynomials are stored as `long` types. The function returns the lower 64 bits of the multiplication result.

Thus, we can directly use `implMul64` function to implement polynomial multiplication over the finite field $F_{2^{32}}$, and then utilize the Rings library to conduct the calculation of the multiplication result modulo the irreducible polynomial. Eventually, the multiplication calculation over the finite field $F_{2^{32}}$ can be achieved. For details, see `datatypes/MdsonicGFFactory32.java`.

For finite field multiplication over $F_{2^{64}}$, we can convert the inputs into multiple computations of the `implMul64` function through unsigned number shifting, and then combine the computation results in the end by shifting. After that, we also use the Rings library to calculate the modulus of the combined results, thus achieving efficient finite field multiplication operations over $F_{2^{64}}$. For details, see `datatypes/MdsonicGFFactory64.java`.

Furthermore, it is advisable to choose irreducible polynomials with fewer terms having a coefficient of 1, and to ensure that the terms with a coefficient of 1 have the lowest possible order. This approach minimizes the number of instructions to handle the remainder calculation after modular operations. In our implementation, the irreducible polynomial in $F_{2^{32}}$ is $x^{32} + x^2 + 1$, and the irreducible polynomial in $F_{2^{64}}$ is $x^{64} + x^4 + x^3 + 1$.

### useMaskedEvaluation
When we set `sonic.useMaskedEvaluation=false`, it indicates that the mask-sharing optimization is not used, meaning that the circuit-dependent techniques and related optimized protocols from the paper, such as MSB, ReLU, etc., are not employed. In this case, our MD-SONIC directly uses SPDZ2k-sharing for arithmetic circuit computation and TinyOT-sharing for boolean circuit computation.


## MAC Check
For the MAC check, we try to reduce the additional overhead brought about by the introduction of TinyOT. We make every effort to enable the two types of checks to be completed concurrently and implement communication packing. 

To minimize the number of modular operations over the finite field during the MAC Check procedure, we exploit the commutative property of modular and addition operations. Specifically, we transform the batch MAC check into an inner product operation, which can be computed using only a single modular operation.

See `protocols/computations/MdsonicMacCheckComputation.java` for details.


## Functionality
Due to the differences in data types and protocol functionalities, MD-SONIC requires different ComputationDictionaries. We have implemented all the fundamental computation interfaces, including addition, multiplication, input, and output. Additionally, we have implemented all boolean circuit operations such as AND, XOR, OR at the bit level, as well as computations for nonlinear functions like MSB and ReLU, fixed-point arithmetic interfaces, and matrix dimension computation interfaces.
