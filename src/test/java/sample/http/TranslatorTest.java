package sample.http;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class TranslatorTest {
  @Test
  public void testTrans() {
    final String trn = Translator.translate(Translator.Language.Automatic.getCode(), Translator.Language.ChineseSimplified.getCode(), "This is a test.");
  }


}
