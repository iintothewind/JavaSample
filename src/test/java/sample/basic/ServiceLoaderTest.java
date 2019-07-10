package sample.basic;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import sample.pool.Pool;

import java.util.ServiceLoader;

@Ignore
@Slf4j
public class ServiceLoaderTest {


  @Test
  @SuppressWarnings("unchecked")
  public void testPoolLoading() {
    //  due to the limitation of ClassLoader, service provider cannot be generic type
    final ServiceLoader<Pool> pools = ServiceLoader.load(Pool.class);
    final Pool<String> pool = ImmutableList.copyOf(pools).stream().findFirst().orElseThrow(IllegalStateException::new);
    for (int i = 0; i < 3; i++) {
      System.out.println(String.format("%s borrowed: %s", i, pool.borrow()));
    }
  }
}
