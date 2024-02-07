package sample.http;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpStatusClass;
import io.vavr.CheckedFunction1;
import io.vavr.control.Try;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
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
            return new X509Certificate[]{};
        }
    };

    private static final SSLContext trustAllSslContext;

    static {
        try {
            trustAllSslContext = SSLContext.getInstance("SSL");
            trustAllSslContext.init(null, new TrustManager[]{x509TrustManager}, new java.security.SecureRandom());
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

    private static Response handleRedirect(@NonNull final OkHttpClient client, final Response tempResponse, @NonNull final Request request, @NonNull final Integer maxRedirect) {
        if (Objects.nonNull(tempResponse) && HttpStatusClass.REDIRECTION.contains(tempResponse.code()) && maxRedirect > 0) {
            final String redirectedUrl = tempResponse.header(HttpHeaderNames.LOCATION.toString());
            log.info("handle redirection with url: {}", redirectedUrl);
            if (Objects.nonNull(redirectedUrl)) {
                final Request newReq = request.newBuilder().url(redirectedUrl).build();
                final Response newResp = Try.of(() -> client.newCall(newReq).execute())
                        .onFailure(t -> log.error("failed to execute redirected url: {} ", redirectedUrl, t))
                        .getOrElse(new Response.Builder().protocol(Protocol.HTTP_1_0).request(request).code(500).message(String.format("failed to execute request with redirectUrl: %s", redirectedUrl)).build());
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

    public static <T> T sendRequest(@NonNull final Request request, @NonNull final CheckedFunction1<? super Response, T> handler) {
        log.info("sendRequest url: {}", request.url());
        return Try.of(() -> trustAllSslClient.newCall(request).execute())
                .mapTry(response -> handleRedirect(trustAllSslClient, response, request, 5))
                .filter(Objects::nonNull)
                .andThenTry(response -> {
                    if (response.isSuccessful()) {
                        log.info("request succeeded with response code: {}, body: {}", response.code(), peekResponse(response));
                    } else {
                        log.warn("request failed with response code: {}, body: {}", response.code(), peekResponse(response));
                    }
                })
                .flatMapTry(createHandler(handler))
                .onFailure(t -> log.error("error while sending request: {}", request, t))
                .getOrNull();
    }

    public static boolean executeRequest(@NonNull final Request request) {
        return Optional.ofNullable(sendRequest(request, Response::isSuccessful)).orElse(false);
    }
}