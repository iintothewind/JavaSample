package sample.secure;

import io.seruco.encoding.base62.Base62;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
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

    private final static String masterKey = "0X*5brsJqtyM&KFc";

    private final static Base62 base62 = Base62.createInstance();

    private final static Pattern p = Pattern.compile("^#\\{([0-9a-zA-Z]*)}#$");

    private final static String encPrefix = "#{";
    private final static String encSuffix = "}#";

    private final Cipher cipher;
    private final Key key;

    private SymEncUtil(final String masterKey) {
        try {
            key = new SecretKeySpec(masterKey.getBytes(), "AES");
            cipher = Cipher.getInstance("AES");
        } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
            log.error("Failed to encrypt: ", e);
            throw new RuntimeException(e);
        }
    }

    private static class InstanceHolder {

        private static final SymEncUtil INSTANCE = new SymEncUtil(masterKey);
    }

    public String encrypt(String input) {
        if (StringUtils.isNotEmpty(input) && !p.matcher(input).matches()) {
            try {
                cipher.init(Cipher.ENCRYPT_MODE, key);
                final byte[] aesEncrypted = cipher.doFinal(input.getBytes());
                final String encoded = new String(base62.encode(aesEncrypted));
                final String encrypted = encPrefix.concat(encoded).concat(encSuffix);
                return encrypted;
            } catch (BadPaddingException | IllegalBlockSizeException | InvalidKeyException e) {
                log.error("Failed to encrypt: {}", input, e);
                throw new IllegalStateException(String.format("Failed to encrypt: %s", input), e);
            }
        }
        return input;
    }

    public String decrypt(String input) {
        if (StringUtils.isNotEmpty(input)) {
            final Matcher matcher = p.matcher(input);
            if (matcher.find()) {
                try {
                    cipher.init(Cipher.DECRYPT_MODE, key);
                    final String base62Encoded = matcher.group(1);
                    final byte[] base62Decoded = base62.decode(base62Encoded.getBytes());
                    final byte[] aesDecrypted = cipher.doFinal(base62Decoded);
                    final String decrypted = new String(aesDecrypted);
                    return decrypted;
                } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
                    log.error("Failed to decrypt: {}", input, e);
                    throw new IllegalStateException(String.format("Failed to decrypt: %s", input), e);
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
