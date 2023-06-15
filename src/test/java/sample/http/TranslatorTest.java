package sample.http;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class TranslatorTest {
  @Test
  public void testTrans() {
    final String trn = Translator.translate(Translator.Language.Automatic.getCode(), Translator.Language.ChineseSimplified.getCode(), "This is a test.");
  }

  @Test
  public void testWith() {
    final Translator.Translation translation = Translator.Translation.builder().build();
  }

  @Test
  public void testBean() {
    final TestBean bean = TestBean.builder().a("aaaa").b("sfsdfsdfsdfbbbb").build();
    System.out.println(bean.getB());

  }


}
