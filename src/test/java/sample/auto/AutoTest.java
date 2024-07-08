package sample.auto;

import io.seruco.encoding.base62.Base62;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.SneakyThrows;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.Test;

public class AutoTest {

    @Test
    public void testAutoGeSetter() {
        Base62 base62 = Base62.createInstance();
        final byte[] encoded = base62.encode("Hello World".getBytes());
        final String encodedStr = new String(encoded); // is "73XpUgyMwkGr29M"
        System.out.println(encodedStr);

        final byte[] decoded = base62.decode("73XpUgyMwkGr29M".getBytes());
        final String decodedStr = new String(decoded); // is "Hello World"
        System.out.println(decodedStr);
    }

    public static void intercept(@Advice.Return(readOnly = false) String original) {
        if (Objects.nonNull(original)) {
            original = original.concat("ssss");
        }
    }


    class Delegator {

        @Advice.OnMethodExit
        public static void intercept(@Advice.Return(readOnly = false) String original) {
            if (original != null) {
                final String base62 = new String(Base62.createInstance().encode(original.getBytes()));
                original = base62;
            }
        }
    }

    @SneakyThrows
    @Test
    public void testByteBuddy() {
        final AutoImpl mk = new ByteBuddy()
            .subclass(AutoImpl.class)
            .method(ElementMatchers.isGetter().and(ElementMatchers.returns(String.class)).and(ElementMatchers.isAnnotatedWith(CustomAuto.class)))
            .intercept(Advice.to(Delegator.class))
            .make()
            .load(getClass().getClassLoader())
            .getLoaded()
            .getDeclaredConstructor()
            .newInstance();

        mk.setName("");

        System.out.println(mk.getName());
        final String base62Decoded = new String(Base62.createInstance().decode("5Cg459b5V9MHDy65NappeeChY41PMrg".getBytes()));
        System.out.println(base62Decoded);
    }

    @Test
    public void testSymmetricEncPattern() {
        final Pattern p = Pattern.compile("^#\\{([0-9a-zA-Z]*)}#$");
        final boolean r01 = p.matcher("#{}#").matches();
        final boolean r02 = p.matcher("#{123}#").matches();
        final boolean r03 = p.matcher("#{123_647}#").matches();
        final boolean r04 = p.matcher("#{123abc}#").matches();
    }

    private final static String encPrefix = "#{";
    private final static String encSuffix = "}#";

    @Test
    public void testExtractEnc() {
        final String enc = "#{123abc}#";
        final Pattern p = Pattern.compile("^#\\{([0-9a-zA-Z]*)}#$");
        Matcher m = p.matcher(enc);
        if(m.find()) {
            System.out.println(m.group(1));
        }

    }

}
