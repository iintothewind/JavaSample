package sample.basic;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import io.vavr.collection.Vector;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StreamUtils;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;


@Slf4j
public class ResourceUtil {

    private final static ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    /**
     * load resources according to given location pattern
     *
     * @param locationPattern e.g: classpath:META-INF/spring.factories, META-INF/spring.factories, classpath*:META-INF/spring.factories, file:d:/*.txt Mor details, please refer PathMatchingResourcePatternResolver
     * @return
     */
    public static List<Resource> loadResources(final String locationPattern) {
        return Try
                .of(() -> resourcePatternResolver.getResources(locationPattern))
                .mapTry(resources -> Vector.ofAll(Arrays.stream(resources)).filter(Resource::exists).toJavaList())
                .onSuccess(v -> v.forEach(r -> log.info("loaded resource: {}", Try.of(() -> r.getURI().toString()).getOrElse(""))))
                .onFailure(Throwables::throwIfUnchecked)
                .getOrElse(ImmutableList.of());
    }

    /**
     * load the first matching resource according to given pattern:
     *
     * @param locationPattern e.g: classpath:META-INF/spring.factories, META-INF/spring.factories, classpath*:META-INF/spring.factories, file:d:/*.txt Mor details, please refer PathMatchingResourcePatternResolver
     * @return
     */
    public static Resource loadResource(final String locationPattern) {
        return loadResources(locationPattern)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("unable to load resource of pattern: %s", locationPattern)));
    }

    /**
     * load all resources as Strings according to given pattern
     *
     * @param locationPattern e.g: classpath:META-INF/spring.factories, META-INF/spring.factories, classpath*:META-INF/spring.factories, file:d:/*.txt Mor details, please refer PathMatchingResourcePatternResolver
     * @return
     */
    public static List<String> readResources(final String locationPattern) {
        return Try
                .of(() -> resourcePatternResolver.getResources(locationPattern))
                .mapTry(resources -> Vector.ofAll(Arrays.stream(resources)).filter(Resource::exists).toJavaList())
                .onSuccess(v -> v.forEach(r -> log.info("loaded resource: {}", Try.of(() -> r.getURI().toString()).getOrElse(""))))
                .onFailure(Throwables::throwIfUnchecked)
                .flatMap(resources -> Try.traverse(resources, r -> Try.of(() -> StreamUtils.copyToString(r.getInputStream(), Charset.defaultCharset()))))
                .getOrElse(Vector.empty())
                .toJavaList();
    }

    /**
     * load the first matching resource as string according to given pattern
     *
     * @param locationPattern e.g: classpath:META-INF/spring.factories, META-INF/spring.factories, classpath*:META-INF/spring.factories, file:d:/*.txt Mor details, please refer PathMatchingResourcePatternResolver
     * @return
     */
    public static String readResource(final String locationPattern) {
        return readResources(locationPattern)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("unable to read resource of pattern: %s", locationPattern)));
    }

    public static String readString(final String file) {
        return Try.of(() -> Files.readString(Paths.get(ClassLoader.getSystemResource(file).toURI())))
                .getOrElseThrow(t -> new IllegalStateException(String.format("unable to read file: %s", file), t));
    }
}
