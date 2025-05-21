package sample.http;

import com.fasterxml.jackson.core.type.TypeReference;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpStatusClass;
import io.vavr.CheckedFunction1;
import io.vavr.control.Try;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;


@Slf4j
public abstract class HttpUtil {

    private HttpUtil() {
    }

    static {
        // see java.net.http.module-info
        System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");
        System.setProperty("jdk.httpclient.keepalive.timeout", "30");
    }

    public final static long connectTimeoutSeconds = 15L;
    public final static long readTimeoutSeconds = 30L;
    public final static long writeTimeoutSeconds = 30L;

    private static final X509TrustManager x509TrustManager = new X509TrustManager() {
        @Override
        public void checkClientTrusted(final X509Certificate[] x509Certificates, final String s) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] x509Certificates, final String s) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
        }
    };

    public static final SSLContext trustAllSslContext;

    static {
        try {
            trustAllSslContext = SSLContext.getInstance("SSL");
            trustAllSslContext.init(null, new TrustManager[]{x509TrustManager}, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    private static final SSLSocketFactory trustAllSslSocketFactory = trustAllSslContext.getSocketFactory();

    public static final OkHttpClient trustAllSslClient = buildTrustAllSslClient(60L, 120L, 120L);

    public static SSLContext getSslContext() {
        return trustAllSslContext;
    }

    public static OkHttpClient buildTrustAllSslClient(final Long connectTimeout, final Long readTimeout, final Long writeTimeout) {
        final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(Optional.ofNullable(connectTimeout).filter(t -> t > 0).orElse(connectTimeoutSeconds), TimeUnit.SECONDS)
            .readTimeout(Optional.ofNullable(readTimeout).filter(t -> t > 0).orElse(readTimeoutSeconds), TimeUnit.SECONDS)
            .writeTimeout(Optional.ofNullable(writeTimeout).filter(t -> t > 0).orElse(writeTimeoutSeconds), TimeUnit.SECONDS)
            .connectionPool(new ConnectionPool())
            .sslSocketFactory(trustAllSslSocketFactory, x509TrustManager)
            .hostnameVerifier((hostname, session) -> true)
            .build();
        return client;
    }

    private static Response handleRedirect(@NonNull final OkHttpClient client, final Response tempResponse, @NonNull final Request request, @NonNull final Integer maxRedirect) {
        if (Objects.nonNull(tempResponse) && HttpStatusClass.REDIRECTION.contains(tempResponse.code()) && maxRedirect > 0) {
            final String redirectedUrl = tempResponse.header(HttpHeaderNames.LOCATION.toString());
            log.info("handleRedirect, redirectedUrl: {}", redirectedUrl);
            if (Objects.nonNull(redirectedUrl)) {
                final Request newReq = request.newBuilder().url(redirectedUrl).build();
                final Response newResp = Try.of(() -> client.newCall(newReq).execute())
                    .onFailure(t -> log.error("handleRedirect failed, redirectedUrl: {} ", redirectedUrl, t))
                    .getOrElse(new Response.Builder().protocol(Protocol.HTTP_1_0).request(request).code(HttpURLConnection.HTTP_INTERNAL_ERROR).message(String.format("failed to execute request with redirectUrl: %s", redirectedUrl)).build());
                return handleRedirect(client, newResp, newReq, maxRedirect - 1);
            }
        }
        return tempResponse;
    }

    public static <T> CheckedFunction1<? super Response, Try<T>> createHandler(@NonNull final CheckedFunction1<? super Response, T> handler) {
        return response -> Try.success(response).filter(Objects::nonNull).mapTry(handler).andFinallyTry(response::close);
    }

    /**
     * peek the response as string but not close it.
     *
     * @param response
     * @return response as string
     */
    public static String peekResponse(final Response response) {
        return Try.of(() -> response.peekBody(Long.MAX_VALUE).string()).getOrElse("");
    }

    public static <T> T sendRequest(@NonNull final OkHttpClient client, @NonNull final Request request, @NonNull final CheckedFunction1<? super Response, T> handler, final Class<?>... exceptions) {
        log.info("sendRequest, request: {}", request);
        return Try.of(() -> client.newCall(request).execute())
            .mapTry(response -> handleRedirect(client, response, request, 5))
            .filter(Objects::nonNull)
            .andThenTry(response -> {
                final String respContentType = response.header("Content-Type");
                final boolean isTextResp = StringUtils.containsIgnoreCase(respContentType, "text") || StringUtils.containsIgnoreCase(respContentType, "json");
                if (response.isSuccessful()) {
                    log.info("sendRequest succeeded, request: {}, response code: {}, body: {}", request, response.code(), isTextResp ? peekResponse(response) : String.format("non-text response body, content-type: %s", respContentType));
                } else {
                    log.warn("sendRequest failed, request: {}, response code: {}, body: {}", request, response.code(), isTextResp ? peekResponse(response) : String.format("non-text response body, content-type: %s", respContentType));
                }
            })
            .flatMapTry(createHandler(handler))
            .onFailure(throwable -> Arrays.asList(exceptions).forEach(ex -> {
                    if (ex.isInstance(throwable)) {
                        throw new RuntimeException(throwable);
                    } else {
                        log.error("sendRequest failed, request: {}, with exception", request, throwable);
                    }
                }
            ))
            .getOrNull();
    }

    public static <T> T sendRequest(@NonNull final Request request, @NonNull final CheckedFunction1<? super Response, T> handler, final Class<?>... exceptions) {
        return sendRequest(trustAllSslClient, request, handler, exceptions);
    }

    public static boolean executeRequest(@NonNull final OkHttpClient client, @NonNull final Request request, final Class<?>... exceptions) {
        return Optional.ofNullable(sendRequest(client, request, Response::isSuccessful, exceptions)).orElse(false);
    }

    public static boolean executeRequest(@NonNull final Request request, final Class<?>... exceptions) {
        return Optional.ofNullable(sendRequest(trustAllSslClient, request, Response::isSuccessful, exceptions)).orElse(false);
    }

    /**
     * send a single HttpRequest and return a handled response. If you would like to reuse HttpClient, then you should create and maintain HttpClient in your own context.
     */
    public static <T> T sendRequest(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
        log.info("sendRequest, request: {}", request);
        if (Objects.nonNull(request) && Objects.nonNull(responseBodyHandler)) {
            try (final HttpClient client = HttpClient
                .newBuilder()
                .followRedirects(Redirect.ALWAYS)
                .sslContext(trustAllSslContext)
                .connectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
                .build()) {
                final HttpResponse<T> resp = client.send(request, responseBodyHandler);
                return resp.body();
            } catch (Exception e) {
                log.error("sendRequest failed, request: {}, with exception", request, e);
            }
        }
        return null;
    }
}
