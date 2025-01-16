package dk.alexandra.fresco.demo.cli;

import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.network.AsyncNetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.mdsonic.datatypes.*;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePool;
import dk.alexandra.fresco.suite.mdml.MdmlProtocolSuite128;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUInt128;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUInt128Factory;
import dk.alexandra.fresco.suite.mdml.datatypes.MdmlCompUIntFactory;
import dk.alexandra.fresco.suite.mdml.resource.MdmlResourcePool;
import dk.alexandra.fresco.suite.mdml.resource.MdmlResourcePoolImpl;
import dk.alexandra.fresco.suite.mdml.resource.storage.MdmlDummyDataSupplier;
import dk.alexandra.fresco.suite.mdml.resource.storage.MdmlOpenedValueStoreImpl;
import dk.alexandra.fresco.suite.mdsonic.MdsonicProtocolSuite128;
import dk.alexandra.fresco.suite.mdsonic.MdsonicProtocolSuite64;
import dk.alexandra.fresco.suite.mdsonic.resource.MdsonicResourcePoolImpl;
import dk.alexandra.fresco.suite.mdsonic.resource.storage.MdsonicDummyDataSupplier;
import dk.alexandra.fresco.suite.mdsonic.resource.storage.MdsonicOpenedBooleanValueStoreImpl;
import dk.alexandra.fresco.suite.mdsonic.resource.storage.MdsonicOpenedValueStoreImpl;
import dk.alexandra.fresco.suite.spdz2k.Spdz2kProtocolSuite128;
import dk.alexandra.fresco.suite.spdz2k.Spdz2kProtocolSuite64;
import dk.alexandra.fresco.suite.spdz2k.datatypes.*;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePoolImpl;
import dk.alexandra.fresco.suite.spdz2k.resource.storage.Spdz2kDummyDataSupplier;
import dk.alexandra.fresco.suite.spdz2k.resource.storage.Spdz2kOpenedValueStoreImpl;
import org.apache.commons.cli.ParseException;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Properties;

/**
 * Utility for reading all configuration from command line. <p> A set of default configurations are
 * used when parameters are not specified at runtime. </p>
 */
public class CmdLineProtocolSuite {

  private final int myId;
  private final int noOfPlayers;
  private final ProtocolSuite<?, ?> protocolSuite;
  private final ResourcePool resourcePool;

  static String getSupportedProtocolSuites() {
    String[] strings = {"spdz2k32",  "spdz2k64", "sonic32", "sonic64", "mdml"};
    return Arrays.toString(strings);
  }

  CmdLineProtocolSuite(String protocolSuiteName, Properties properties, NetworkConfiguration conf) throws ParseException, NoSuchAlgorithmException {
    this.myId = conf.getMyId();
    this.noOfPlayers = conf.noOfParties();
    if (protocolSuiteName.equals("spdz2k32")) {
      this.protocolSuite = getSpdz2kProtocolSuite(properties, false);
      this.resourcePool =
          createSpdz2kResourcePool(false, conf);
    } else if (protocolSuiteName.equals("spdz2k64")) {
      this.protocolSuite = getSpdz2kProtocolSuite(properties, true);
      this.resourcePool =
          createSpdz2kResourcePool(true, conf);
    } else if (protocolSuiteName.equals("sonic32")) {
      this.protocolSuite = getMdsonicProtocolSuite(properties, false);
      this.resourcePool = createMdsonicResourcePool(false, conf);
    } else if (protocolSuiteName.equals("sonic64")) {
      this.protocolSuite = getMdsonicProtocolSuite(properties, true);
      this.resourcePool = createMdsonicResourcePool(true, conf);
    } else if (protocolSuiteName.equals("mdml")) {
      this.protocolSuite = getMdmlProtocolSuite(properties);
      this.resourcePool = createMdmlResourcePool(conf);
    } else {
      throw new IllegalArgumentException("you can only choose spdz2k32, spdz2k64, sonic32, sonic64, mdml.");
    }
  }

  public ResourcePool getResourcePool() {
    return resourcePool;
  }

  public ProtocolSuite<?, ?> getProtocolSuite() {
    return this.protocolSuite;
  }

  private ProtocolSuite<?, ?> getSpdz2kProtocolSuite(Properties properties, boolean large) {
    Properties p = getProperties(properties);
    return large ? new Spdz2kProtocolSuite128(true, 13) : new Spdz2kProtocolSuite64(true, 7);
  }

  private ProtocolSuite<?, ?> getMdsonicProtocolSuite(Properties properties, boolean large) {
    Properties p = getProperties(properties);
    boolean useMaskedEvaluation = Boolean.parseBoolean(p.getProperty("sonic.useMaskedEvaluation", "false"));
    return large ? new MdsonicProtocolSuite128(useMaskedEvaluation, 13) : new MdsonicProtocolSuite64(useMaskedEvaluation, 7);
  }

  private ProtocolSuite<?, ?> getMdmlProtocolSuite(Properties properties) {
    Properties p = getProperties(properties);
    return new MdmlProtocolSuite128(13);
  }

  private Properties getProperties(Properties properties) {
    return properties;
  }

  private Spdz2kResourcePool createSpdz2kResourcePool(boolean large, NetworkConfiguration conf) {
    if (large) {
      CompUIntFactory<CompUInt128> factory = new CompUInt128Factory();
      CompUInt128 keyShare = factory.createRandom();
      Spdz2kResourcePool<CompUInt128> resourcePool =
              new Spdz2kResourcePoolImpl<>(
                      myId,
                      noOfPlayers, new AesCtrDrbg(new byte[32]),
                      new Spdz2kOpenedValueStoreImpl<>(),
                      new Spdz2kDummyDataSupplier<>(myId, noOfPlayers, keyShare, factory),
                      factory, AesCtrDrbg::new);
      resourcePool.initializeJointRandomness(() -> new AsyncNetwork(conf), AesCtrDrbg::new, 32);
      return resourcePool;
    } else {
      CompUIntFactory<CompUInt64> factory = new CompUInt64Factory();
      CompUInt64 keyShare = factory.createRandom();
      Spdz2kResourcePool<CompUInt64> resourcePool =
              new Spdz2kResourcePoolImpl<>(
                      myId,
                      noOfPlayers, new AesCtrDrbg(new byte[32]),
                      new Spdz2kOpenedValueStoreImpl<>(),
                      new Spdz2kDummyDataSupplier<>(myId, noOfPlayers, keyShare, factory),
                      factory, AesCtrDrbg::new);
      resourcePool.initializeJointRandomness(() -> new AsyncNetwork(conf), AesCtrDrbg::new, 32);
      return resourcePool;
    }
  }

  private MdsonicResourcePool createMdsonicResourcePool(boolean large, NetworkConfiguration conf) {
    if (large) {
      MdsonicCompUIntFactory<MdsonicCompUInt128> factory = new MdsonicCompUInt128Factory();
      MdsonicGFFactory<MdsonicGF64> booleanFactory = new MdsonicGFFactory64();
      MdsonicCompUInt128 keyShare = factory.createRandom();
      MdsonicResourcePool<MdsonicCompUInt128, MdsonicGF64> resourcePool =
              new MdsonicResourcePoolImpl<>(
                      myId, noOfPlayers, new AesCtrDrbg(new byte[32]),
                      new MdsonicOpenedValueStoreImpl<>(),
                      new MdsonicOpenedBooleanValueStoreImpl<>(),
                      new MdsonicDummyDataSupplier<>(myId, noOfPlayers, keyShare, factory, booleanFactory),
                      factory, booleanFactory, AesCtrDrbg::new);
      resourcePool.initializeJointRandomness(() -> new AsyncNetwork(conf), AesCtrDrbg::new, 32);
      return resourcePool;
    } else {
      MdsonicCompUIntFactory<MdsonicCompUInt64> factory = new MdsonicCompUInt64Factory();
      MdsonicGFFactory<MdsonicGF32> booleanFactory = new MdsonicGFFactory32();
      MdsonicCompUInt64 keyShare = factory.createRandom();
      MdsonicResourcePool<MdsonicCompUInt64, MdsonicGF32> resourcePool =
              new MdsonicResourcePoolImpl<MdsonicCompUInt64, MdsonicGF32>(
                      myId,
                      noOfPlayers, new AesCtrDrbg(new byte[32]),
                      new MdsonicOpenedValueStoreImpl<>(),
                      new MdsonicOpenedBooleanValueStoreImpl<>(),
                      new MdsonicDummyDataSupplier<>(myId, noOfPlayers, keyShare, factory, booleanFactory),
                      factory, booleanFactory, AesCtrDrbg::new);
      resourcePool.initializeJointRandomness(() -> new AsyncNetwork(conf), AesCtrDrbg::new, 32);
      return resourcePool;
    }
  }

  private MdmlResourcePool createMdmlResourcePool(NetworkConfiguration conf) {
    MdmlCompUIntFactory<MdmlCompUInt128> factory = new MdmlCompUInt128Factory();
    MdmlResourcePool<MdmlCompUInt128> resourcePool =
            new MdmlResourcePoolImpl<>(myId, noOfPlayers, new AesCtrDrbg(new byte[32]),
                    new MdmlOpenedValueStoreImpl<>(),
                    new MdmlDummyDataSupplier<>(myId, noOfPlayers, factory),
                    factory, AesCtrDrbg::new);

    resourcePool.initializeJointRandomness(() -> new AsyncNetwork(conf), AesCtrDrbg::new, 32);
    return resourcePool;
  }
}
