package sample.basic;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.collection.Stream;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.simpleflatmapper.lightningcsv.ClosableCsvWriter;
import org.simpleflatmapper.lightningcsv.CsvWriter;
import sample.http.JsonUtil;
import sample.http.ResourceUtil;
import sample.model.S3Image;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

@Slf4j
public class ResourceUtilTest {

    @Test
    public void testLoadS3Images01() throws IOException {
        final String obj2 = ResourceUtil.readResource("classpath:obj2.json");
        final String obj3 = ResourceUtil.readResource("classpath:obj3.json");
        final String obj5 = ResourceUtil.readResource("classpath:obj5.json");
        final List<S3Image> obj2List = JsonUtil.load(obj2, new TypeReference<List<S3Image>>() {
        });

        final List<S3Image> obj3List = JsonUtil.load(obj3, new TypeReference<List<S3Image>>() {
        }).stream().map(si -> si.withBucket("ai-generate-data3-0")).toList();

        final List<S3Image> obj5List = JsonUtil.load(obj5, new TypeReference<List<S3Image>>() {
        }).stream().map(si -> si.withBucket("ai-generate-data4")).toList();

        final List<S3Image> lst = Stream.concat(obj2List, obj3List, obj5List).sorted(Comparator.comparing(S3Image::getSize)).toJavaList();

        try (final ClosableCsvWriter writer = CsvWriter.dsl().to(new File("./s3images.csv"))) {
            lst.forEach(si -> {
                Try.run(() -> writer.appendRow(si.bucket, si.key, si.getSize().toString()));
            });
        }
    }


}
