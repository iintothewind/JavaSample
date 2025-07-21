package sample.http;

import com.google.common.io.Resources;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;

import static sample.http.Translator.Language.ChineseSimplified;


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
        final String content =  ResourceUtil.readString("fsa/yul.txt");
        System.out.println(content);
    }
}
