package sample.cglib.enhancer;

import com.google.common.base.Splitter;
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
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class BlockEnhancer {
  private static final int DEFAULT_BLOCK_TIME = 1000;

  @Test
  public void testBlock() throws Exception {
    Enhancer enhancer = new Enhancer();
    enhancer.setSuperclass(SampleClass.class);
    enhancer.setCallback(new NavigationMethodInterceptor("tes=5000"));
    SampleClass proxy = (SampleClass) enhancer.create();
    //proxy.testBlock();
    proxy.annotated();
  }

  private static class NavigationMethodInterceptor implements MethodInterceptor {
    private final ImmutableMap<String, Integer> methodNamePrefixBlockTimeMap;

    public NavigationMethodInterceptor(String methodNamePrefixBlockTimePairs) {
      methodNamePrefixBlockTimeMap = ImmutableMap.copyOf(Maps.transformEntries(Splitter.on(",").withKeyValueSeparator("=").split(methodNamePrefixBlockTimePairs), (key, value) -> {
        try {
          return Integer.parseInt(value);
        } catch (NumberFormatException e) {
          return DEFAULT_BLOCK_TIME;
        }
      }));
    }

    @Override
    public Object intercept(Object target, Method method, Object[] args, MethodProxy proxy) throws Throwable {
      final Invokable invokable = Invokable.from(method);
      Optional<Map.Entry<String, Integer>> entry = methodNamePrefixBlockTimeMap.entrySet().stream().filter(input -> invokable.getName().startsWith(input.getKey()) && invokable.isPublic() && invokable.getReturnType().equals(TypeToken.of(Void.TYPE))).findFirst();
      entry.ifPresent(e -> {
        try {
          TimeUnit.MILLISECONDS.sleep(e.getValue());
        } catch (InterruptedException ex) {
          Thread.currentThread().interrupt();
        }
      });
      return Optional.of(method.getAnnotation(Block.class)).map(b -> {
          System.out.println(String.format("before: %s , after %s", b.before(), b.after()));
          Object object = null;
          try {
            TimeUnit.MILLISECONDS.sleep(b.before());
            object = proxy.invokeSuper(target, args);
            TimeUnit.MILLISECONDS.sleep(b.after());
          } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
          } catch (Throwable e) {
            throw new RuntimeException(e);
          }
          return object;
        }
      ).orElse(proxy.invokeSuper(target, args));
    }
  }


}
