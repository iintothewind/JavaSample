package sample.secure;

import io.seruco.encoding.base62.Base62;
import io.vavr.control.Try;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class SymEncUtil {

    /**
     * Should be a 16-size string including alpha, number, special chars
     */
    private final static String masterKey = "0X*5brsJqtyM&KFc";

    private final static Base62 base62 = Base62.createInstance();

    private final static Pattern p = Pattern.compile("^#\\{([0-9a-zA-Z]*)}#$");

    private final static String encPrefix = "#{";
    private final static String encSuffix = "}#";

    private final Cipher cipher;
    private final Key key;
    private final ReentrantLock lock;

    private SymEncUtil(final String masterKey) {
        try {
            key = new SecretKeySpec(masterKey.getBytes(), "AES");
            cipher = Cipher.getInstance("AES");
            lock = new ReentrantLock();
        } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
            log.error("Failed to init cipher: ", e);
            throw new RuntimeException(e);
        }
    }

    private static class InstanceHolder {

        private static final SymEncUtil INSTANCE = new SymEncUtil(masterKey);
    }

    public String encrypt(String input) {
        if (StringUtils.isNotEmpty(input) && !p.matcher(input).matches()) {
            try {
                if (lock.tryLock(300L, TimeUnit.MILLISECONDS)) {
                    cipher.init(Cipher.ENCRYPT_MODE, key);
                    final byte[] aesEncrypted = cipher.doFinal(input.getBytes());
                    final String encoded = new String(base62.encode(aesEncrypted));
                    final String encrypted = encPrefix.concat(encoded).concat(encSuffix);
                    return encrypted;
                }
            } catch (BadPaddingException | IllegalBlockSizeException | InterruptedException | InvalidKeyException e) {
                log.error("failed to encrypt: {}", input, e);
                throw new IllegalStateException(String.format("failed to encrypt: %s", input), e);
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    Try.run(lock::unlock);
                }
            }
        }
        return input;
    }

    public String decrypt(String input) {
        if (StringUtils.isNotEmpty(input)) {
            final Matcher matcher = p.matcher(input);
            if (matcher.find()) {
                try {
                    if (lock.tryLock(300L, TimeUnit.MILLISECONDS)) {
                        cipher.init(Cipher.DECRYPT_MODE, key);
                        final String base62Encoded = matcher.group(1);
                        final byte[] base62Decoded = base62.decode(base62Encoded.getBytes());
                        final byte[] aesDecrypted = cipher.doFinal(base62Decoded);
                        final String decrypted = new String(aesDecrypted);
                        return decrypted;
                    }
                } catch (IllegalBlockSizeException | BadPaddingException | InterruptedException | InvalidKeyException e) {
                    log.error("failed to decrypt: {}", input, e);
                    throw new IllegalStateException(String.format("failed to decrypt: %s", input), e);
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        Try.run(lock::unlock);
                    }
                }
            }
        }
        return input;
    }

    public static String enc(String input) {
        return InstanceHolder.INSTANCE.encrypt(input);
    }

    public static String dec(String input) {
        return InstanceHolder.INSTANCE.decrypt(input);
    }
}
