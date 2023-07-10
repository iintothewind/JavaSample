package sample.http;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpStatusClass;
import io.vavr.control.Try;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


@Slf4j
public class HttpUtil {
    private static final X509TrustManager x509TrustManager = new X509TrustManager() {
        @Override
        public void checkClientTrusted(final X509Certificate[] x509Certificates, final String s) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] x509Certificates, final String s) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[] {};
        }
    };

    private static final SSLContext trustAllSslContext;

    static {
        try {
            trustAllSslContext = SSLContext.getInstance("SSL");
            trustAllSslContext.init(null, new TrustManager[] { x509TrustManager }, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    private static final SSLSocketFactory trustAllSslSocketFactory = trustAllSslContext.getSocketFactory();

    public static final OkHttpClient trustAllSslClient = buildTrustAllSslClient(15);

    public static OkHttpClient buildTrustAllSslClient(final long timeout) {
        final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(Optional.ofNullable(timeout).filter(t -> t > 0).orElse(15L), TimeUnit.SECONDS)
            .connectionPool(new ConnectionPool())
            .sslSocketFactory(trustAllSslSocketFactory, x509TrustManager)
            .hostnameVerifier((hostname, session) -> true)
            .build();
        return client;
    }

    private static Response handleRedirect(@NonNull final OkHttpClient client, @NonNull final Response tempResponse, @NonNull final Request request, @NonNull final Integer maxRedirect) {
        if (HttpStatusClass.REDIRECTION.contains(tempResponse.code()) && maxRedirect > 0) {
            final String redirectedUrl = tempResponse.header(HttpHeaderNames.LOCATION.toString());
            log.info("handle redirection with url: {}", redirectedUrl);
            if (Objects.nonNull(redirectedUrl)) {
                final Request newReq = request.newBuilder().url(redirectedUrl).build();
                final Response newResp = Try.of(() -> client.newCall(newReq).execute())
                    .onFailure(t -> log.error("failed to execute redirected url: {} with error: {}", redirectedUrl, t.getMessage()))
                    .getOrElse(new Response.Builder().code(500).message(String.format("failed to execute request with redirectUrl: %s", redirectedUrl)).build());
                return handleRedirect(client, newResp, newReq, maxRedirect - 1);
            }
        }
        return tempResponse;
    }

    public static Response sendRequest(@NonNull final Request request) {
        return Try.of(() -> trustAllSslClient.newCall(request).execute())
            .mapTry(response -> handleRedirect(trustAllSslClient, response, request, 5))
            .andThenTry(response -> {
                if (response.isSuccessful()) {
                    log.info("request sent with response code: {}, body: {}", response.code(), Try.of(() -> response.body().string()).getOrElse(""));
                } else {
                    log.warn("request failed with response code: {}, body: {}", response.code(), Try.of(() -> response.body().string()).getOrElse(""));
                }
            })
            .onFailure(t -> log.error("error while sending request: {}", t.getMessage()))
            .getOrElse(new Response.Builder().code(500).message("failed to execute request").build());
    }

    public static boolean executeRequest(@NonNull final Request request) {
        return Try.of(() -> trustAllSslClient.newCall(request).execute())
            .mapTry(response -> handleRedirect(trustAllSslClient, response, request, 5))
            .andThenTry(response -> {
                if (response.isSuccessful()) {
                    log.info("request sent with response code: {}, body: {}", response.code(), Try.of(() -> response.body().string()).getOrElse(""));
                } else {
                    log.warn("request failed with response code: {}, body: {}", response.code(), Try.of(() -> response.body().string()).getOrElse(""));
                }
            })
            .onFailure(t -> log.error("error while sending request: {}", t.getMessage()))
            .map(Response::isSuccessful)
            .getOrElse(false);
    }
}