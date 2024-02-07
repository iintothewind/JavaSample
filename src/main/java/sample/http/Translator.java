package sample.http;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import sample.http.Translator.Translation.Sentence;

@Slf4j
public class Translator {

  private final static OkHttpClient client = new OkHttpClient.Builder().connectionPool(new ConnectionPool()).build();

  public static String translate(final String fromLanguage, final String toLanguage, final String content) {
    final Request request = new Request.Builder()
        .url(new HttpUrl.Builder()
            .scheme("https")
            .host("translate.googleapis.com")
            .addPathSegment("translate_a")
            .addPathSegment("single")
            .addQueryParameter("dt", "t")
            .addQueryParameter("dj", "1")
            .addQueryParameter("client", "gtx")
            .addQueryParameter("sl", fromLanguage)
            .addQueryParameter("tl", toLanguage)
            .addEncodedQueryParameter("q", content)
            .build())
        .header(HttpHeaderNames.CONTENT_TYPE.toString(), HttpHeaderValues.APPLICATION_JSON.toString())
        .get()
        .build();

    log.info("url: {}", request.url().url());

    final String trans = HttpUtil
        .sendRequest(request, r -> JsonUtil
            .load(HttpUtil.peekResponse(r), new TypeReference<Translation>() {
            })
            .map(t -> t.getSentences().stream().findFirst().map(Sentence::getTrans).orElse(""))
            .orElse(""));

    log.info("content: {} => trans: {}", content, trans);
    return trans;
  }

  public enum Language {
    Automatic("auto"),
    ChineseSimplified("zh_cn"),
    ChineseTraditional("zh_tw"),
    English("en"),
    French("fr"),
    German("de"),
    Greek("el"),
    Italian("it"),
    Japanese("ja"),
    Korean("ko"),
    Latin("la"),
    Spanish("es");
    private String code;

    Language(final String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }
  }

  @Getter
  @Setter
  @Builder
  @EqualsAndHashCode
  @AllArgsConstructor
  @NoArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Translation {

    @With
    List<Sentence> sentences;

    @Getter
    @Setter
    @Builder
    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Sentence {

      @With
      String orig;
      @With
      String trans;
    }
  }
}
