package sample.cglib.enhancer;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.reflect.Invokable;
import com.google.common.reflect.TypeToken;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BlockEnhancer {
    private static final int DEFAULT_BLOCK_TIME = 1000;

    private static class NavigationMethodInterceptor implements MethodInterceptor {
        private final ImmutableMap<String, Integer> methodNamePrefixBlockTimeMap;

        public NavigationMethodInterceptor(String methodNamePrefixBlockTimePairs) {
            methodNamePrefixBlockTimeMap = ImmutableMap.copyOf(Maps.transformEntries(Splitter.on(",").withKeyValueSeparator("=").split(methodNamePrefixBlockTimePairs), new Maps.EntryTransformer<String, String, Integer>() {
                @Override
                public Integer transformEntry(String key, String value) {
                    try {
                        return Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        return DEFAULT_BLOCK_TIME;
                    }
                }
            }));
        }

        @Override
        public Object intercept(Object target, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            final Invokable invokable = Invokable.from(method);
            Optional<Map.Entry<String, Integer>> entry = FluentIterable.from(methodNamePrefixBlockTimeMap.entrySet()).firstMatch(new Predicate<Map.Entry<String, Integer>>() {
                @Override
                public boolean apply(Map.Entry<String, Integer> input) {
                    return invokable.getName().startsWith(input.getKey()) && invokable.isPublic() && invokable.getReturnType().equals(TypeToken.of(Void.TYPE));
                }
            });
            if (entry.isPresent()) {
                TimeUnit.MILLISECONDS.sleep(entry.get().getValue());
            }
            Optional<Block> block = Optional.of(method.getAnnotation(Block.class));
            if (block.isPresent()) {
                System.out.println(String.format("before: %s , after %s", block.get().before(), block.get().after()));
                TimeUnit.MILLISECONDS.sleep(block.get().before());
                Object object = proxy.invokeSuper(target, args);
                TimeUnit.MILLISECONDS.sleep(block.get().after());
                return object;
            }
            return proxy.invokeSuper(target, args);
        }
    }

    @Test
    public void testBlock() throws Exception {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(SampleClass.class);
        enhancer.setCallback(new NavigationMethodInterceptor("tes=5000"));
        SampleClass proxy = (SampleClass) enhancer.create();
        //proxy.testBlock();
        proxy.annotated();
    }


}
