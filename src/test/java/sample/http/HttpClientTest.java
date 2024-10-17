package sample.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import java.io.File;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Objects;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.junit.Test;

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

        try (final HttpClient client = HttpClient.newBuilder().build()) {
            final HttpResponse<TrackResponse> resp = client.send(request, JsonUtil.handlerOf(new TypeReference<>() {
            }));
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

    @Test
    @SneakyThrows
    public void testDownload() {
        final File f = new File("C:\\Users\\ivar\\code\\JavaSample\\target");
        final byte[] bytes = download("https://s3.us-west-1.wasabisys.com/echobase-photos/ulala/ULALA/e6de6a16-9535-43e8-9b26-401418d8809b.pdf");
        System.out.println(new String(bytes));
        Files.write(bytes, new File(f, "test.pdf"));
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

}
