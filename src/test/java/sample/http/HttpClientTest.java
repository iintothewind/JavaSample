package sample.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.Test;

import java.io.File;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.Objects;

@Slf4j
public class HttpClientTest {

    @SneakyThrows
    @Test
    public void testGet() {
        final HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create("https://ulala.ca/echobase-web/rest/v1/orders/TEST225000013/shippingLabel"))
                .GET()
                .header("Content-Type", HttpHeaderValues.APPLICATION_JSON.toString())
                .header("Authorization", String.format("Basic %s", Base64.getEncoder().encodeToString("hre.api@ulala.ca:VwGCav3y2H".getBytes())))
                .build();

        try (final HttpClient client = HttpClient.newBuilder().sslContext(HttpUtil.getSslContext()).build()) {
            final HttpResponse<TrackResponse> resp = client.send(request, JsonUtil.handlerOf(new TypeReference<>() {
            }, respInfo -> TrackResponse.builder().build()));
            log.info("resp: {}", resp.body());
        }
    }

    public Path download(final String url, final String parentDir) {
        final File parent = new File(parentDir);
        final File file = new File(parent, url.substring(url.lastIndexOf("/") + 1));
        final HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        try (final HttpClient client = HttpClient.newBuilder().build()) {
            final HttpResponse<Path> resp = client.send(request, BodyHandlers.ofFile(file.toPath()));
            return resp.body();
        } catch (final Exception e) {
            log.error("download failed", e);
            return null;
        }
    }

    public byte[] download(final String url) {
        final HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        try (final HttpClient client = HttpClient.newBuilder().build()) {
            final HttpResponse<byte[]> resp = client.send(request, BodyHandlers.ofByteArray());
            if (resp.statusCode() > 299) {
                throw new IllegalStateException(String.format("download failed, response: %s", resp));
            }
            return resp.body();
        } catch (final Exception e) {
            log.error("download failed", e);
            return new byte[0];
        }
    }

    public static byte[] download02(final String url) {
        final Request request = new Request.Builder().url(url).get().build();
        final byte[] bytes = HttpUtil.sendRequest(request, resp -> Option.of(resp).filter(r -> r.code() < 299).toTry().mapTry(r -> r.peekBody(Integer.MAX_VALUE)).mapTry(ResponseBody::bytes).getOrNull());
        return bytes;
    }


    @Test
    @SneakyThrows
    public void testDownload01() {
        final File f = new File("C:\\Users\\ivar\\code\\JavaSample\\target");
        final byte[] bytes = download("https://s3.us-west-1.wasabisys.com/echobase-photos/ulala/ULALA/e6de6a16-9535-43e8-9b26-401418d8809b.pdf");
        System.out.println(new String(bytes));
    }

    @Test
    @SneakyThrows
    public void testDownload02() {
        final File f = new File("./target/test_001.pdf");
        final byte[] bytes = download02("https://shipease.oss-cn-hangzhou.aliyuncs.com/pdfs/2024-11-21/MSK41102248.pdf");
        Files.write(f.toPath(), bytes, StandardOpenOption.CREATE_NEW);
    }

    @Test
    public void testUri() {
        final Path name = Path.of(URI.create("https://s3.us-west-1.wasabisys.com/echobase-photos/ulala/ULALA/e6de6a16-9535-43e8-9b26-401418d8809b.pdf")).getFileName();
        System.out.println(name);
    }

    @Test
    public void testGet01() {
        final Request request = new Request.Builder()
                .url(Objects.requireNonNull(
                        HttpUrl.parse("https://ulala.ca/echobase-web/rest/v1/orders/TEST225000013/track")))
                .headers(Headers.of(ImmutableMap.of()))
                .header(HttpHeaderNames.CONTENT_TYPE.toString(), HttpHeaderValues.APPLICATION_JSON.toString())
                .header("Authorization", String.format("Basic %s", Base64.getEncoder().encodeToString("hre.api@ulala.ca:VwGCav3y2H".getBytes())))
                .get()
                .build();

        final TrackResponse trackResponse = HttpUtil.sendRequest(request, resp -> JsonUtil.load(HttpUtil.peekResponse(resp), new TypeReference<TrackResponse>() {
        }));

        System.out.println(trackResponse);
    }

    @Test
    public void testGet02() {
        final Request request = new Request.Builder()
                .url(Objects.requireNonNull(
                        HttpUrl.parse("http://3hlssssssrn.shipper.d.veryk.com1/api?action=service&format=json&id=294&sign=Rp8WT%2FjBegYGWBVfNm9oEDfl%2FvQ%2FAyZD83WmS9Q5T8E%3D&timestamp=1729186925")))
                .headers(Headers.of(ImmutableMap.of()))
                .header(HttpHeaderNames.CONTENT_TYPE.toString(), HttpHeaderValues.APPLICATION_JSON.toString())
                .get()
                .build();
        HttpUtil.sendRequest(request, resp -> HttpUtil.peekResponse(resp), UnknownHostException.class, SocketTimeoutException.class);
    }

    public static byte[] downloadUrl(final String url) {
        final byte[] bytes = HttpUtil.sendRequest(new Builder().url(url).get().build(), resp -> Try
                .success(resp)
                .filter(r -> Objects.nonNull(r) && r.isSuccessful())
                .map(Response::body)
                .mapTry(ResponseBody::bytes)
                .getOrNull());
        return bytes;
    }

    @Test
    public void testGet03() {
        final byte[] bytes = downloadUrl("https://shipease.oss-cn-hangzhou.aliyuncs.com/pdfs/2024-11-21/MSK41102248.pdf");
        System.out.println(bytes.length);
    }

    @Test
    public void testCompare01() {
        final double d = 0.0d;
        System.out.println(d <= 0d);
    }

    @Test
    @SneakyThrows
    public void testLoadPod() {
        final byte[] bytes = Files.readAllBytes(ResourceUtil.loadResource("classpath:ul88083.jpg").getFile().toPath());
        final String base64Content = Base64.getEncoder().encodeToString(bytes);
        System.out.println(base64Content);

    }

    @With
    @Getter
    @Setter
    @lombok.Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public static class IpResp {
        @EqualsAndHashCode.Include
        private String origin;
    }

    @Test
    public void testHandler01() {
        final JsonUtil.RespWrapper<IpResp> resp = HttpUtil.sendRequest(HttpRequest.newBuilder().uri(URI.create("https://httpbin.org/ips")).GET().build(), JsonUtil.handlerOfWrapper(new TypeReference<JsonUtil.RespWrapper<IpResp>>() {
        }));
        System.out.println(resp);
    }

}
