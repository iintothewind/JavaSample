package sample.http;

import com.google.common.io.Resources;
import io.vavr.control.Try;
import java.net.URL;
import java.nio.charset.Charset;
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

    @Test
    public void testLoadResource01() {
        final String content = Try.of(() -> Resources.toString(Resources.getResource("cfg.xml"), Charset.defaultCharset())).getOrElse("");
        System.out.println(content);
    }

    @Test
    public void testLoadResource02() {
        final String content = Try.of(() -> ResourceUtil.readString("cfg.xml")).getOrElse("");
        System.out.println(content);
    }
}
