package sample.cglib.enhancer;


import net.sf.cglib.proxy.*;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * The anonymous subclass of FixedValue would become hardly referenced from the enhanced SampleClass
 * such that neither the anonymous FixedValue instance or the class holding the @Test method would ever be garbage collected.
 * This can introduce nasty memory leaks in your applications.
 * Therefore, do not use non-static inner classes with cglib.
 * (I only use them in this overview for keeping the examples short.)
 */
public class EnhancerTest {

  @Test
  public void testFixedValue() {
    Enhancer enhancer = new Enhancer();
    enhancer.setSuperclass(SampleClass.class);
    enhancer.setCallback((FixedValue) () -> "Hello cglib!");
    SampleClass proxy = (SampleClass) enhancer.create();
    Assertions.assertThat(proxy.test(null)).isEqualTo("Hello cglib!");
  }

  @Test
  public void testLazyLoader() {
    Enhancer enhancer = new Enhancer();
    enhancer.setSuperclass(SampleClass.class);
    /*
    The LazyLoader is actually supposed to return an instance of a subclass of the enhanced class.
    This instance is requested only when a method is called on the enhanced object and then stored for future invocations of the generated proxy.
     */
    enhancer.setCallback((LazyLoader) () -> new SampleClass() {
      public String test(String input) {
        return "Hello cglib!";
      }
    });
    SampleClass proxy = (SampleClass) enhancer.create();
    System.out.println(proxy.test(null));
    Assertions.assertThat(proxy.test(null)).isEqualTo("Hello cglib!");
  }

  @Test
  public void testDispatcher() {
    Enhancer enhancer = new Enhancer();
    enhancer.setSuperclass(SampleClass.class);
    /*
    The Dispatcher is like the LazyLoader but will be invoked on every method call without storing the loaded object.
    This allows to change the implementation of a class without changing the reference to it.
     */
    enhancer.setCallback((Dispatcher) () -> new SampleClass() {
      public String test(String input) {
        return "Hello cglib!";
      }
    });
    SampleClass proxy = (SampleClass) enhancer.create();
    System.out.println(proxy.test(null));
    Assertions.assertThat(proxy.test(null)).isEqualTo("Hello cglib!");
  }

  @Test(expected = RuntimeException.class)
  public void testInvocationHandler() {
    Enhancer enhancer = new Enhancer();
    enhancer.setSuperclass(SampleClass.class);
    enhancer.setCallback((InvocationHandler) (proxy, method, args) -> {
      if (method.getDeclaringClass() != Object.class && method.getReturnType() == String.class) {
        return "Hello cglib!";
      } else {
        throw new RuntimeException("Do not know what to do.");
      }
    });
    SampleClass proxy = (SampleClass) enhancer.create();
    Assertions.assertThat(proxy.test(null)).isEqualTo("Hello cglib!");
    Assertions.assertThat(proxy.toString()).isEqualTo("Do not know what to do.");
  }


  @Test
  public void testMethodInterceptor() {
    Enhancer enhancer = new Enhancer();
    enhancer.setSuperclass(SampleClass.class);
    enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
      if (method.getDeclaringClass() != Object.class && method.getReturnType() == String.class) {
        return "Hello cglib!";
      } else {
        return proxy.invokeSuper(obj, args);
      }
    });
    SampleClass proxy = (SampleClass) enhancer.create();
    Assertions.assertThat(proxy.test(null)).isEqualTo("Hello cglib!");
    Assertions.assertThat(proxy.toString()).isNotEqualTo("Hello cglib!");
    System.out.println(proxy.toString());
    proxy.hashCode(); // Does not throw an exception or result in an endless loop.
  }

  @Test
  public void testCallbackFilter() {
    Enhancer enhancer = new Enhancer();
    CallbackHelper callbackHelper = new CallbackHelper(SampleClass.class, SampleClass.class.getInterfaces()) {
      @Override
      protected Object getCallback(Method method) {
        if (method.getDeclaringClass() != Object.class && method.getReturnType() == String.class) {
          return new MethodInterceptor() {
            @Override
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
              return proxy.invokeSuper(obj, args).toString().concat(" and cglib");
            }
          };
        } else {
          return NoOp.INSTANCE; // A singleton provided by NoOp.
        }
      }
    };
    enhancer.setSuperclass(SampleClass.class);
    enhancer.setCallbackFilter(callbackHelper);
    enhancer.setCallbacks(callbackHelper.getCallbacks());
    SampleClass proxy = (SampleClass) enhancer.create();

    System.out.println(proxy.test(null));
    System.out.println(proxy.toString());

    Assertions.assertThat(proxy.test(null)).isNotEqualTo("Hello cglib!");
    Assertions.assertThat(proxy.toString()).isNotEqualTo("Hello cglib!");
    proxy.hashCode(); // Does not throw an exception or result in an endless loop.
  }
}
