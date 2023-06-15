package sample.basic;

import java.util.Base64;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.util.SimpleByteSource;
import org.junit.Test;


public class ShiroTest {
    public static String generatePassword(String plainTextPassword, String salt) {
        final byte[] decodedSalt = Base64.getDecoder().decode(salt);
        String hashedPasswordBase64 = new Sha256Hash(plainTextPassword, new SimpleByteSource(decodedSalt), 1024).toBase64();
        return hashedPasswordBase64;
    }

    @Test
    public void testGenPasswd() {
        final String pwd = generatePassword("admin", "Yl36AOtaM7MY96ULwIqE9g==");
        System.out.println(pwd);

        final String pwd2 = generatePassword("Password@2", "i01GQ6SoMA+IWvsXw24LcA==");
        System.out.println(pwd2);
    }

    @Test
    public void testBase64() {
        final String encoded = Base64.getEncoder().encodeToString("https://ulala-tech.atlassian.net/browse/UTT-362".getBytes());
        System.out.println(encoded);
        final String urlEncoded = Base64.getUrlEncoder().encodeToString("https://home-chat.vercel.app/messageList?topic=sarah_home&user=ulala".getBytes());
        System.out.println(urlEncoded);
        final String decoded = new String(Base64.getDecoder().decode(urlEncoded));
        System.out.println(decoded);

    }
}
