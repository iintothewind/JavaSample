package sample.location;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import org.junit.Test;
import org.springframework.util.DigestUtils;
import sample.http.JsonUtil;
import sample.http.ResourceUtil;

public class LocationParser {

    @Test
    public void testLoad01() {
        final List<String> jsonLines = ResourceUtil.readLines("classpath:resp_1_to_5000.json");
        final List<GoogleResp> lst = jsonLines.stream().map(json -> JsonUtil.load(json, new TypeReference<GoogleResp>() {
        })).toList();

        System.out.println(lst);
    }



    @Test
    public void testLoad02() {
        final String hex = DigestUtils.md5DigestAsHex("sss".getBytes());
        System.out.println(hex);
    }

}
