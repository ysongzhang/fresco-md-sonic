package dk.alexandra.fresco.suite.mdsonic.datatypes;

import java.util.List;

/**
 * Represents an authenticated, secret-share element.
 *
 * @param <SecretP> type of underlying plain value, i.e., the value type we use for security parameter.
 */
public class MdsonicASBoolBoolean<SecretP extends MdsonicGF<SecretP>> extends MdsonicASBool<SecretP> {  // the class of ASS over F2

  /**
   * Creates a {@link MdsonicASBoolBoolean}.
   */
  public MdsonicASBoolBoolean(boolean share, SecretP macShare) {
    super(share, macShare);
  }

  /**
   * Creates a {@link MdsonicASBoolBoolean} from a public value. <p>All parties compute the mac
   * share of the value but only party one (by convention) stores the public value as the share, the
   * others store 0.</p>
   */
  public MdsonicASBoolBoolean(boolean share, SecretP macKeyShare, SecretP zero, boolean isPartyOne) {
    this(isPartyOne && share, share ? macKeyShare : zero);
  }

  /**
   * Compute sum of this and other.
   */
  public MdsonicASBoolBoolean<SecretP> xor(MdsonicASBoolBoolean<SecretP> other) {
    return new MdsonicASBoolBoolean<>(share ^ other.getShare(), macShare.add(other.macShare));
  }

  /**
   * Compute product of this and constant (open) value.
   */
  public MdsonicASBoolBoolean<SecretP> and(boolean other) {
    SecretP mac;
    if(other){
      mac = macShare.getClone();
    } else {
      mac = macShare.getZero();
    }
    return new MdsonicASBoolBoolean<>(share & other, mac);
  }

  /**
   * Compute sum of this and constant (open) value. <p>All parties compute their mac share of the
   * public value and add it to the mac share of the authenticated value, however only party 1 adds
   * the public value to is value share.</p>
   */
  public MdsonicASBoolBoolean<SecretP> xorOpen(boolean other, SecretP macKeyShare, SecretP zero, boolean isPartyOne) {
    MdsonicASBoolBoolean<SecretP> wrapped = new MdsonicASBoolBoolean<>(other, macKeyShare, zero, isPartyOne);
    return xor(wrapped);
  }

  public boolean open(List<MdsonicASBoolBoolean<SecretP>> allShares) {
    boolean res = false;
    for (MdsonicASBoolBoolean<SecretP> share : allShares) {
      res ^= share.share;
    }
    return res;
  }


  @Override
  public String toString() {
    return "DomainASBoolBoolean{" +
        "share=" + share + "macShare=" + macShare +
        '}';
  }

}
