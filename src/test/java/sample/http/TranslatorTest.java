package sample.http;

import com.google.common.collect.ImmutableMap;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import java.io.IOException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
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
  
  @Test
  public void testExecuteRequest() {
    String fromLanguage;
    final Request request = new Request.Builder()
            .url(new HttpUrl.Builder()
                    .scheme("https")
                    .host("translate.googleapis.com")
                    .addPathSegment("translate_a")
                    .addPathSegment("single")
                    .addQueryParameter("dt", "t")
                    .addQueryParameter("dj", "1")
                    .addQueryParameter("client", "gtx")
                    .addQueryParameter("sl", Translator.Language.Automatic.getCode())
                    .addQueryParameter("tl", Translator.Language.ChineseSimplified.getCode())
                    .addEncodedQueryParameter("q", "This is a test")
                    .build())
            .header(HttpHeaderNames.CONTENT_TYPE.toString(), HttpHeaderValues.APPLICATION_JSON.toString())
            .get()
            .build();

    HttpUtil.executeRequest(request);
  }

  @Test
  public void testWebHook() {
    final Request request = new Request.Builder()
        .url(Objects.requireNonNull(
            HttpUrl.parse("https://dev-webhooks.techdinamics.com/mapper/SubmitFile.ashx?sender=ULALA&receiver=GTAGSM&apiKey=CD3255E3C03249D2A89FA07E5A9DF3FC&version=1&doctype=STATUS")))
        .headers(Headers.of(ImmutableMap.of()))
        .header(HttpHeaderNames.CONTENT_TYPE.toString(), HttpHeaderValues.APPLICATION_JSON.toString())
        .post(RequestBody.create("{\n" +
            "    \"token\": \"whe-fa8b17ec-191e-4ae9-afb3-910fe2fb6bab\",\n" +
            "    \"type\": \"ORDER_STATUS_CHANGE\",\n" +
            "    \"createdOn\": \"2021-04-09 11:14:57\",\n" +
            "    \"clientName\": \"shipper@ulala.ca\",\n" +
            "    \"payload\": {\n" +
            "        \"clientName\": \"RAVEN_FORCE\",\n" +
            "        \"trackNumber\": \"49828416\",\n" +
            "        \"routeName\": \"0323-BBY-3\",\n" +
            "        \"oldOrderStatus\": \"IN_PROGRESS\",\n" +
            "        \"newOrderStatus\": \"COMPLETED\",\n" +
            "        \"statusReason\": \"B1DEL\",\n" +
            "        \"destCity\": \"Burnaby\",\n" +
            "        \"destProv\": \"BC\",\n" +
            "        \"createdOnMilliseconds\": 1617992097570,\n" +
            "        \"podUrl\": \"https://s3.us-west-1.wasabisys.com/echobase-photos/ulala/RAVEN_FORCE/RFCWPp777369-050_1.jpeg\"\n" +
            "    }\n" +
            "}", MediaType.parse("application/json")))
        .build();
    HttpUtil.executeRequest(request);
  }

  @Test
  public void testParse() {
    final Object n = Integer.parseInt("000110");
    System.out.println(n);

    String postCode = "000110";
    String regex = "^0+(?!$)"; // remove the leading zeros in zipcode
    String cleanedPostalCode = postCode.replaceAll(regex, "");
    System.out.println(cleanedPostalCode);

  }

}
