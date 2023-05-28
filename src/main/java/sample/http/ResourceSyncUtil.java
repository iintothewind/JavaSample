package sample.http;

import com.google.common.base.Throwables;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ResourceSyncUtil {

  private static void checkEntries(final String file) {
    final List<String> invalidLines = ResourceUtil.readLines(file).stream().filter(StringUtils::isNoneBlank).filter(s -> s.split("=").length != 2).collect(Collectors.toList());
    if (!CollectionUtils.isEmpty(invalidLines)) {
      throw new IllegalStateException(String.format("invalid entries found: %s", invalidLines));
    }
  }

  private static List<Tuple2<String, String>> loadEntries(final String file) {
    checkEntries(file);
    final List<Tuple2<String, String>> entries = ResourceUtil.readLines(file).stream().filter(StringUtils::isNoneBlank).map(s -> s.split("=")).map(pair -> Tuple.of(pair[0], pair[1])).collect(Collectors.toList());
    return entries;
  }

  private static List<String> translateEntries(final List<Tuple2<String, String>> entries, final String targetLanguage) {
    final List<String> lines = entries.stream().map(t -> String.format("%s=%s", t._1, StringEscapeUtils.escapeJava(Translator.translate("en", targetLanguage, t._2)))).collect(Collectors.toList());
    return lines;
  }

  public static void saveTranslateEntries(final String file, final String targetLanguage) {
    final Resource resource = ResourceUtil.loadResource(file);
    final String parent = Try.of(() -> resource.getFile().getParent()).getOrElse("");
    final String child = Try
      .of(() -> resource.getFile().getName())
      .map(n -> n.split("\\."))
      .map(pair -> String.format("%s_%s.%s", pair[0], targetLanguage, pair[1]))
      .getOrElse("");
    final List<String> lines = translateEntries(loadEntries(file), targetLanguage);
    final File targetFile = new File(parent, child);
    log.info("saving translated entries to: {}", targetFile.getPath());
    Try.run(() -> Files.write(targetFile.toPath(), lines, StandardOpenOption.CREATE))
      .onFailure(Throwables::throwIfUnchecked);
  }

  public static void syncProperties(final String enPropertiesFile) {
    saveTranslateEntries(enPropertiesFile, Translator.Language.ChineseSimplified.getCode());
    saveTranslateEntries(enPropertiesFile, Translator.Language.ChineseTraditional.getCode());
    saveTranslateEntries(enPropertiesFile, Translator.Language.French.getCode());
    saveTranslateEntries(enPropertiesFile, Translator.Language.Spanish.getCode());
  }

}
