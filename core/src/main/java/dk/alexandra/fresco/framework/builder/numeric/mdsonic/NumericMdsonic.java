package dk.alexandra.fresco.framework.builder.numeric.mdsonic;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.SInt;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Basic interface for numeric applications. This is the interface which an arithmetic protocol
 * suite must implement in order to function within FRESCO. Binary protocol suites are also welcome
 * to implement this interface, as that makes it possible to do arithmetic operations using bits as
 * the underlying representation.
 */
public interface NumericMdsonic extends ComputationDirectory {

  /**
   * Adds two secret values and returns the result.
   * @param a Secret value 1
   * @param b Secret value 2
   * @return A deferred result computing a+b
   */
  DRes<SInt> add(DRes<SInt> a, DRes<SInt> b);

  /**
   * Adds a secret value with a public value and returns the result.
   * @param a Public value
   * @param b Secret value
   * @return A deferred result computing a+b
   */
  DRes<SInt> add(BigInteger a, DRes<SInt> b);

  /**
   * Convenience implementation of {@link #add(BigInteger, DRes)}
   */
  default DRes<SInt> add(long a, DRes<SInt> b) {
    return add(BigInteger.valueOf(a), b);
  }

  /**
   * Adds a secret value with a public value and returns the result.
   * @param a Public value
   * @param b Secret value
   * @return A deferred result computing a+b
   */
  DRes<SInt> addOpen(DRes<OInt> a, DRes<SInt> b);

  /**
   * Subtracts two secret values and returns the result.
   * @param a Secret value 1
   * @param b Secret value 2
   * @return A deferred result computing a-b
   */
  DRes<SInt> sub(DRes<SInt> a, DRes<SInt> b);

  /**
   * Subtracts a public value and a secret value and returns the result.
   * @param a Public value
   * @param b Secret value
   * @return A deferred result computing a-b
   */
  DRes<SInt> sub(BigInteger a, DRes<SInt> b);

  /**
   * Convenience implementation of {@link #sub(BigInteger, DRes)}
   */
  default DRes<SInt> sub(long a, DRes<SInt> b) {
    return sub(BigInteger.valueOf(a), b);
  }

  /**
   * Subtracts a public value and a secret value and returns the result.
   * @param a Public value
   * @param b Secret value
   * @return A deferred result computing a-b
   */
  DRes<SInt> subFromOpen(DRes<OInt> a, DRes<SInt> b);

  /**
   * Subtracts a secret value and a public value and returns the result.
   * @param a Secret value
   * @param b Public value
   * @return A deferred result computing a-b
   */
  DRes<SInt> subOpen(DRes<SInt> a, DRes<OInt> b);

  /**
   * Subtracts a secret value and a public value and returns the result.
   * @param a Secret value
   * @param b Public value
   * @return A deferred result computing a-b
   */
  DRes<SInt> sub(DRes<SInt> a, BigInteger b);

  /**
   * Multiplies two secret values and returns the result.
   * @param a Secret value 1
   * @param b Secret value 2
   * @return A deferred result computing a*b
   */
  DRes<SInt> mult(DRes<SInt> a, DRes<SInt> b);

  /**
   * used for MSS in MD-SONIC.
   * @param a Secret value 1
   * @param b Secret value 2
   * @param maskedSecret Masked secret in MSS
   * @return MSS result
   */
  DRes<SInt> mult(DRes<SInt> a, DRes<SInt> b, DRes<SInt> maskedSecret);

  /**
   * Multiplies a public value onto a secret value and returns the result.
   * @param a Public value
   * @param b Secret value
   * @return A deferred result computing a*b
   */
  DRes<SInt> mult(BigInteger a, DRes<SInt> b);

  /**
   * Convenience implementation of {@link #mult(BigInteger, DRes)}
   */
  default DRes<SInt> mult(long a, DRes<SInt> b) {
    return mult(BigInteger.valueOf(a), b);
  }

  /**
   * Multiplies a public value onto a secret value and returns the result.
   * @param a Public value
   * @param b Secret value
   * @return A deferred result computing a*b
   */
  DRes<SInt> multByOpen(DRes<OInt> a, DRes<SInt> b);

  /**
   * Returns a deferred result which creates a secret shared random bit. (This should be computed
   * beforehand to increase the speed of the application)
   * 
   * @return A secret value representing either 0 or 1.
   */
  DRes<SInt> randomBitAsArithmetic();

  /**
   * A random bit as SBool.
   * @return A secret value representing either 0 or 1.
   */
  DRes<SBool> randomBitAsBoolean();

  /**
   * Returns a deferred result which creates a secret shared random element within the field of
   * operation. (This should be computed beforehand to increase the speed of the application)
   * 
   * @return A random element within the field of operation (i.e. the modulus)
   */
  DRes<SInt> randomElement();

  /**
   * Creates a known secret value from a public value. This is primarily a helper function in order
   * to use public values within the FRESCO functions.
   * 
   * @param value The public value.
   * @return A secret value which represents the given public value.
   */
  DRes<SInt> known(BigInteger value);

  /**
   * Convenience implementation of {@link #known(BigInteger)}
   */
  default DRes<SInt> known(long value) {
    return known(BigInteger.valueOf(value));
  }

  /**
   * Creates a secret value from a public value and a secret random, where opened is a byte array.
   */
  DRes<SInt> secret(byte[] opened, DRes<SInt> random);

  /**
   * Closes a public value. If the MPC party calling this method is not providing input, just use
   * null as the input value.
   * 
   * @param value The value to input or null if no input should be given.
   * @param inputParty The ID of the MPC party.
   * @return The closed input value.
   */
  DRes<SInt> input(BigInteger value, int inputParty);

  /**
   * Convenience implementation of {@link #input(BigInteger, int)}
   */
  default DRes<SInt> input(long value, int inputParty) {
    return input(BigInteger.valueOf(value), inputParty);
  }

  /**
   * Opens a value to all MPC parties.
   * @param secretShare The value to open.
   * @return The opened value represented by the closed value.
   */
  DRes<BigInteger> open(DRes<SInt> secretShare);

  /**
   * Opens a value to all MPC parties.
   */
  DRes<OInt> openAsOInt(DRes<SInt> secretShare);

  /**
   * Opens a value to all MPC parties.
   */
  DRes<OInt> openAsOInt(DRes<SInt> secretShare, int outputParty);

  /**
   * Opens a value to a single given party.
   * @param secretShare The value to open.
   * @param outputParty The party to receive the opened value.
   * @return The opened value if you are the outputParty, or null otherwise.
   */
  DRes<BigInteger> open(DRes<SInt> secretShare, int outputParty);

  /**
   * Helper methods for inputting multiple known values at once.
   */
  default List<DRes<SInt>> known(List<BigInteger> values) {
    List<DRes<SInt>> secret = new ArrayList<>(values.size());
    for (BigInteger value : values) {
      secret.add(known(value));
    }
    return secret;
  }

  default DRes<List<DRes<SInt>>> knownAsDRes(List<BigInteger> values) {
    List<DRes<SInt>> secret = new ArrayList<>(values.size());
    for (BigInteger value : values) {
      secret.add(known(value));
    }
    return () -> secret;
  }

  DRes<Void> macCheck(List<DRes<SInt>> randomList, byte[] opened, byte[] seed);  // seedLength == 32


  /**
   * Multiplies two secret values and returns the result.
   * @param a Secret value 1, MSS
   * @param b Secret value 2, MSS
   * @return A deferred result computing a*b, ASS Result.
   */
  DRes<SInt> multMSS(DRes<SInt> a, DRes<SInt> b);

  DRes<OInt> getOpened(DRes<SInt> secretShare);

  // return the product of an edaBit and a daBit
  DRes<SInt> getProduct();
}
