package sample.basic;

import io.vavr.API;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import sample.http.HttpUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class VideoUtil {
    private final static String LINUX_FFMPEG_PATH = "/usr/bin/ffmpeg";
    private final static String WINDOWS_FFMPEG_PATH = "C:\\tools\\ffmpeg\\bin\\ffmpeg.exe";
    public final static String collovLogo = "data:image/png;base64,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";

    private final static FFmpeg ffmpeg = Try.of(() -> OsUtil.detectOs())
            .flatMapTry(detectedOs -> API.Match(detectedOs).of(
                    API.Case(API.$(os -> Objects.equals(os, OsUtil.OS.WINDOWS)), os -> Try.of(() -> new FFmpeg(WINDOWS_FFMPEG_PATH))),
                    API.Case(API.$(), os -> Try.of(() -> new FFmpeg(LINUX_FFMPEG_PATH)))
            ))
            .getOrElseThrow(t -> new IllegalArgumentException("failed to init ffmpeg", t));

    private final static FFmpegExecutor executor = Try.of(() -> new FFmpegExecutor(ffmpeg))
            .getOrElseThrow(t -> new IllegalArgumentException("failed to init ffmpegExecutor", t));

    public static String findCollovLogo() {
        final String logoPath = Try.of(() -> ResourceUtil.loadResource("classpath:collov_logo.png"))
                .mapTry(resource -> resource.getFile().getPath())
                .recoverWith(Exception.class, x -> Try.of(() -> Files.write(Try.of(() -> Files.createTempFile("collov_logo", ".png")).getOrNull(),
                                Base64.getDecoder().decode(collovLogo.split(",")[1]),
                                StandardOpenOption.TRUNCATE_EXISTING))
                        .map(Path::toString))
                .getOrElseThrow(t -> new IllegalStateException("failed to load logo", t));

        return logoPath;
    }

    public static Path watermark(final String videoUrl, final String logo) {
        final Path inputVideo = HttpUtil.downloadAsTmpFile(videoUrl, "ffmpeg_input_", ".mp4");
        final Path logoPath = Try.of(() -> Paths.get(logo)).filter(Files::exists).getOrNull();
        if (Objects.nonNull(inputVideo) && Objects.nonNull(logoPath)) {
            final Path outputVideo = Try.of(() -> Files.createTempFile("ffmpeg_output_", ".mp4")).getOrNull();

            if (Objects.nonNull(outputVideo)) {
                final FFmpegBuilder builder = new FFmpegBuilder()
                        .addInput(inputVideo.toString())
                        .addInput(logoPath.toString())
                        .setComplexFilter("overlay=x=W-w:y=0")
                        .addOutput(outputVideo.toString())
                        .addExtraArgs("-threads", "8")
                        .done();
                executor.createJob(builder).run();
                return outputVideo;
            }
        }
        return null;
    }


    public static Path watermark(final String videoUrl) {
        return watermark(videoUrl, findCollovLogo());
    }

    public static Path concat(List<String> videoUrls) {
        if (Objects.nonNull(videoUrls) && !videoUrls.isEmpty()) {
            final List<Path> inputVideos = videoUrls
                    .stream().map(url -> HttpUtil.downloadAsTmpFile(url, "ffmpeg_input_", ".mp4"))
                    .collect(Collectors.toList());
            final Path listFile = Try.of(() -> Files.write(Try.of(() -> Files.createTempFile("ffmpeg_list_", ".txt")).getOrNull(), inputVideos.stream().map(f -> String.format("file '%s'", f.toString())).collect(Collectors.toList())))
                    .onFailure(t -> log.error("failed to create concat list file", t))
                    .getOrNull();
            final Path outputVideo = Try.of(() -> Files.createTempFile("ffmpeg_output_", ".mp4"))
                    .onFailure(t -> log.info("failed to create output video file", t))
                    .getOrNull();
            if (Objects.nonNull(listFile) && Objects.nonNull(outputVideo)) {
                final FFmpegBuilder builder = new FFmpegBuilder()
                        .setFormat("concat") // same as -f concat
                        .addExtraArgs("-safe", "0")
                        .addInput(listFile.toString()) // input is the list
                        .addOutput(outputVideo.toString())
                        .setVideoCodec("copy")
                        .setAudioCodec("copy")
                        .addExtraArgs("-threads", "8")
                        .done();
                executor.createJob(builder).run();
                return outputVideo;
            }
        }
        return null;
    }
}
