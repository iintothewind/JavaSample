package sample.http;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static sample.http.Translator.Language.ChineseSimplified;

@Slf4j
public class ResourceSyncUtilTest {
  @Test
  public void testSave() {
    ResourceSyncUtil.saveTranslateEntries("classpath:asc.properties", ChineseSimplified.getCode());

  }

  @Test
  public void testSync() {
    ResourceSyncUtil.syncProperties("classpath:asc.properties");
  }
}
