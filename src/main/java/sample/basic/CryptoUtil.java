package sample.basic;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;


public class CryptoUtil {
  private final static String ALGORITHM = "DES";
  private final static String SEED = "03DCD075-8F4A-4905-B331-9BD93A9478A3";
  private final Cipher cryptCipher;
  private final Cipher decryptCipher;
  private final SecretKey secretKey;

  private CryptoUtil() {
    try {
      cryptCipher = Cipher.getInstance(ALGORITHM);
      decryptCipher = Cipher.getInstance(ALGORITHM);
      final byte[] encoded = Base64.getEncoder().encode(SEED.getBytes());
      secretKey = SecretKeyFactory.getInstance(ALGORITHM).generateSecret(new DESKeySpec(Base64.getDecoder().decode(encoded)));
    } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidKeySpecException e) {
      throw new IllegalStateException(e);
    }
  }

  public static byte[] encrypt(byte[] plain) {
    try {
      InstanceHolder.instance.cryptCipher.init(Cipher.ENCRYPT_MODE, InstanceHolder.instance.secretKey);
      return InstanceHolder.instance.cryptCipher.doFinal(plain);
    } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
      throw new IllegalStateException(e);
    }
  }

  public static String encryptToBase64(byte[] plain) {
    return Base64.getEncoder().encodeToString(encrypt(plain));
  }

  public static String decrypt(byte[] encrypted) {
    try {
      InstanceHolder.instance.decryptCipher.init(Cipher.DECRYPT_MODE, InstanceHolder.instance.secretKey);
      return new String(InstanceHolder.instance.decryptCipher.doFinal(encrypted));
    } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
      throw new IllegalStateException(e);
    }
  }

  public static String decryptFromBase64(String source) {
    return decrypt(Base64.getDecoder().decode(source));
  }

  private static class InstanceHolder {
    private static final CryptoUtil instance = new CryptoUtil();
  }
}
