package sample.basic;

import com.google.common.collect.ImmutableList;
import io.vavr.collection.HashMap;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import sample.http.JsonUtil;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FsaTest {

  public static List<String> loadFsas(final String path) {
    final List<String> fsas = ImmutableList.copyOf(ResourceUtil.readResource(path).split("\n")).stream().filter(StringUtils::isNotBlank).map(String::trim).map(String::toLowerCase).distinct().sorted().collect(Collectors.toList());
    return fsas;
  }

  @Test
  public void testLoad() {
    final List<String> yegs = loadFsas("classpath:fsa/yeg.txt");
    final List<String> yows = loadFsas("classpath:fsa/yow.txt");
    final List<String> yuls = loadFsas("classpath:fsa/yul.txt");
    final List<String> yvrs = loadFsas("classpath:fsa/yvr.txt");
    final List<String> yxus = loadFsas("classpath:fsa/yxu.txt");
    final List<String> yycs = loadFsas("classpath:fsa/yyc.txt");
    final List<String> yyzs = loadFsas("classpath:fsa/yyz.txt");

    final Map<String, List<String>> fsaCoverage = HashMap.<String, List<String>>empty()
        .put("yeg", yegs)
        .put("yow", yows)
        .put("yul", yuls)
        .put("yvr", yvrs)
        .put("yxu", yxus)
        .put("yyc", yycs)
        .put("yyz", yyzs)
        .toJavaMap();

    final String dumps = JsonUtil.dump(fsaCoverage);
    System.out.println(dumps);
  }

}
