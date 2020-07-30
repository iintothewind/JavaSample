package sample.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.schibsted.spt.data.jslt.Expression;
import com.schibsted.spt.data.jslt.Parser;
import io.vavr.control.Try;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JsltSample {

  public static String readLines(final String path) {
    return Try.of(() -> Files.readAllLines(Paths.get(ClassLoader.getSystemResource(path).toURI())))
      .map(list -> String.join("\n", list))
      .onFailure(Throwable::printStackTrace)
      .getOrElse("");
  }

  public String parse(final String jslt, final String data) {
    final ObjectMapper mapper = new ObjectMapper();
    return Try.of(() -> Parser.compileString(jslt))
      .mapTry(p -> p.apply(mapper.readTree(data)))
      .mapTry(mapper::writeValueAsString)
      .onFailure(Throwable::printStackTrace)
      .getOrElse("");
  }

  @Test
  public void testParser() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    final Expression jslt = Parser.compileString("let idparts = split(.id, \"-\")\n" +
      "let xxx = [for ($idparts) \"x\" * size(.)]\n" +
      "\n" +
      "{\n" +
      "  \"id\" : join($xxx, \"-\"),\n" +
      "  \"type\" : \"Anonymized-View\",\n" +
      "  * : .\n" +
      "}\n");

    final JsonNode node = jslt.apply(mapper.readTree("{\n" +
      "  \"schema\" : \"http://schemas.schibsted.io/thing/pulse-simple.json#1.json\",\n" +
      "  \"id\" : \"w23q7ca1-8729-24923-922b-1c0517ddffjf1\",\n" +
      "  \"published\" : \"2017-05-04T09:13:29+02:00\",\n" +
      "  \"type\" : \"View\",\n" +
      "  \"environmentId\" : \"urn:schibsted.com:environment:uuid\",\n" +
      "  \"url\" : \"http://www.aftenposten.no/\"\n" +
      "}\n"));

    System.out.println(mapper.writeValueAsString(node));
  }

  @Test
  public void testMapping() throws JsonProcessingException {
    final ObjectMapper mapper = new ObjectMapper();
    final String s = mapper.writeValueAsString(ImmutableMap.of("name", "aname", "namespace", "namespace"));
    final String output = parse(readLines("cfg/config01.conf"), s);
    System.out.println(output);

  }
}
