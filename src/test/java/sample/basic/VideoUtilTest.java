package sample.basic;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.nio.file.Path;
import java.util.List;

@Slf4j
public class VideoUtilTest {

    @Test
    public void testConcat01() {
        final List<String> inputFiles = ImmutableList.of(
                "https://collov-test.s3.us-west-1.amazonaws.com/20250627/guest_e609574cab3d4dfce1949d3bf9fa56b1.mp4",
                "https://collov-test.s3.us-west-1.amazonaws.com/20250627/guest_3a578b3ea13322e0fc23c6f2a43a1a65.mp4");

        final Path outputVideo = VideoUtil.concat(inputFiles);
        log.info("output: {}", outputVideo);
    }
}
