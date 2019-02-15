package sample.blocker;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.reflect.Invokable;
import com.google.common.reflect.TypeToken;
import net.sf.cglib.proxy.*;
import sample.blocker.annoncation.Block;
import sample.blocker.config.Blocker;
import sample.blocker.config.Config;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


public class BlockEnhancer<T> {
  private final Queue<Blocker> blockers;
  private final Class<T> target;
  private final Object[] constructorArgs;
  private final Class<?>[] constructorArgTypes;

  private BlockEnhancer(Class<T> target, Class[] constructorArgTypes, Object[] constructorArgs) {
    XmlMapper mapper = new XmlMapper();
    mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    try {
      blockers = Optional.ofNullable(mapper.readValue(getClass().getResourceAsStream("/BlockerConfig.xml"), Config.class).getBlockers()).orElse(new LinkedBlockingQueue<Blocker>());
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    this.target = target;
    this.constructorArgTypes = constructorArgTypes;
    this.constructorArgs = constructorArgs;
  }

  public static <T> BlockEnhancer<T> enhance(Class<T> target, Class[] constructorArgTypes, Object[] constructorArgs) {
    return new BlockEnhancer<>(target, constructorArgTypes, constructorArgs);
  }

  public static <T> BlockEnhancer<T> enhance(Class<T> target) {
    return new BlockEnhancer<>(target, null, null);
  }

  @SuppressWarnings("unchecked")
  public T create() {
    CallbackHelper callbackHelper = new CallbackHelper(target, target.getInterfaces()) {
      @Override
      protected Object getCallback(final Method method) {
        Optional<Blocker> configBlock = blockers.stream().filter(input -> {
          final Invokable invokable = Invokable.from(method);
          return invokable.getName().startsWith(input.getPrefix()) && invokable.isPublic() && invokable.getReturnType().equals(TypeToken.of(Void.TYPE));
        }).findFirst();
        Optional<Block> annotatedBlock = Optional.ofNullable(method.getAnnotation(Block.class));
        return configBlock.map(c -> (Object) new BlockerMethodInterceptor(configBlock.get().getBefore(), configBlock.get().getAfter()))
          .orElseGet(() -> annotatedBlock.map(a -> (Object) new BlockerMethodInterceptor(a.before(), a.after())).orElse(NoOp.INSTANCE));
      }
    };
    Enhancer enhancer = new Enhancer();
    enhancer.setSuperclass(target);
    enhancer.setCallbackFilter(callbackHelper);
    enhancer.setCallbacks(callbackHelper.getCallbacks());
    return constructorArgs != null ? (T) enhancer.create(constructorArgTypes, constructorArgs) : (T) enhancer.create();
  }


  private static class BlockerMethodInterceptor implements MethodInterceptor {
    private final int before;
    private final int after;

    public BlockerMethodInterceptor(int before, int after) {
      this.before = before;
      this.after = after;
    }

    @Override
    public Object intercept(Object target, Method method, Object[] args, MethodProxy proxy) throws Throwable {
      if (before > 0) {
        TimeUnit.MILLISECONDS.sleep(before);
      }
      Object object = proxy.invokeSuper(target, args);
      if (after > 0) {
        TimeUnit.MILLISECONDS.sleep(after);
      }
      return object;
    }
  }


}


