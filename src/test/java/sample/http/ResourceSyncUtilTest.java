package sample.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Test;

import static sample.http.Translator.Language.ChineseSimplified;
import com.google.common.io.Files;


@Slf4j
public class ResourceSyncUtilTest {
  @Test
  public void testSave() {
    PropertiesSyncUtil.saveTranslateEntries("classpath:asc.properties", ChineseSimplified.getCode());
  }

  @Test
  public void testEscape() {
    PropertiesSyncUtil.escapeEntries("classpath:asc_es.txt");
    PropertiesSyncUtil.escapeEntries("classpath:asc_fr.txt");
  }

  @Test
  public void testSync() {
    PropertiesSyncUtil.syncProperties("classpath:asc.properties");
  }
}
