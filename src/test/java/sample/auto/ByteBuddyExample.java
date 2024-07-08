package sample.auto;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

public class ByteBuddyExample {

    @Test
    public void testByteBuddy() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        AutoImpl mk = new ByteBuddy()
            .subclass(AutoImpl.class)
            .method(ElementMatchers.isGetter().and(ElementMatchers.returns(String.class)))
            .intercept(Advice.to(Delegator.class))
            .make()
            .load(getClass().getClassLoader())
            .getLoaded()
            .getDeclaredConstructor()
            .newInstance();

        System.out.println(mk.getName()); // This should now print "modified"
    }

    static class AutoImpl {

        @CustomAuto
        public String getName() {
            return "original";
        }
    }

    static class Delegator {

        @Advice.OnMethodExit
        public static String intercept(@Advice.Return String original) {
            if (Objects.nonNull(original)) {
                // Example modification logic
                return original.toUpperCase(); // Modify return value here
            }
            return original;
        }
    }

    @interface CustomAuto {

    }
}

