package sample.http;

import com.fasterxml.jackson.core.type.TypeReference;
import io.netty.handler.codec.http.HttpHeaderValues;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class HttpClientTest {

    @SneakyThrows
    @Test
    public void testGet() {
        final HttpRequest request = HttpRequest
            .newBuilder()
            .uri(URI.create("https://ulala.ca/echobase-web/rest/v1/orders/TEST225000013/track"))
            .GET()
            .header("Content-Type", HttpHeaderValues.APPLICATION_JSON.toString())
            .header("Authorization", String.format("Basic %s", Base64.getEncoder().encodeToString("hre.api@ulala.ca:VwGCav3y2H".getBytes())))
            .build();

        try(final HttpClient client = HttpClient.newBuilder().build()) {
            final HttpResponse<TrackResponse> resp = client.send(request, JsonUtil.handlerOf(new TypeReference<TrackResponse>() {
            }));

            log.info("resp: {}", resp.body());

        };

    }

}
