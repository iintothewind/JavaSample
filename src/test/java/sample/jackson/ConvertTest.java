package sample.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.collect.FluentIterable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import sample.blocker.config.Blocker;
import sample.blocker.config.Config;

import java.io.IOException;
import java.util.function.Consumer;


public class ConvertTest {
    private final Logger log = LogManager.getLogger();

    @Test
    public void serialize() throws JsonProcessingException {
        XmlMapper mapper = new XmlMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        Config cfg = new Config();
        Blocker blocker1 = new Blocker();
        blocker1.setPrefix("type");
        blocker1.setBefore(500);
        blocker1.setAfter(500);
        Blocker blocker2 = new Blocker();
        blocker2.setPrefix("click");
        blocker2.setBefore(500);
        blocker2.setAfter(500);
        cfg.addBlocker("blocker", blocker1);
        cfg.addBlocker("blocker", blocker2);
        log.info(mapper.writeValueAsString(cfg));
    }

    @Test
    public void deserialize() throws IOException {
        XmlMapper mapper = new XmlMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        Config cfg = mapper.readValue(getClass().getResourceAsStream("/BlockerConfig.xml"), Config.class);
        FluentIterable.from(cfg.getBlockers()).forEach(new Consumer<Blocker>() {
            @Override
            public void accept(Blocker blocker) {
                log.info("prefix: {}, before: {}, after: {}", blocker.getPrefix(), blocker.getBefore(), blocker.getAfter());
            }
        });
    }
}
