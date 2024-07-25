package sample.basic;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.control.Try;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.util.SimpleByteSource;
import org.junit.Test;
import sample.http.JsonUtil;


public class ShiroTest {

  public static String generatePassword(String plainTextPassword, String salt) {
    final byte[] decodedSalt = Base64.getDecoder().decode(salt);
    String hashedPasswordBase64 = new Sha256Hash(plainTextPassword, new SimpleByteSource(decodedSalt), 1024).toBase64();
    return hashedPasswordBase64;
  }

  @Test
  public void testGenPasswd() {
    final String pwd = generatePassword("admin", "lfcdwHlPIlGZDeKwqzYG8Q==");
    System.out.println(pwd);

    final String pwd1 = generatePassword("Password@1", "UdrHkiRizGkIviRS7FXVQg==");
    System.out.println(pwd1);

    final String pwd2 = generatePassword("mHp@3TUd", "s4U3L/IgzrNHk6liPNlecA==");
    System.out.println(pwd2);

    final String pwd3 = generatePassword("mHp@3TUd", "iA2JTFU584VMu7EReP0LEg==");
    System.out.println(pwd3);
  }

  @Test
  public void testBase64() {
    final String encoded = Base64.getEncoder().encodeToString("https://ulala-tech.atlassian.net/browse/UTT-362".getBytes());
    System.out.println(encoded);
    final String urlEncoded = Base64.getUrlEncoder().encodeToString("https://home-chat.vercel.app/messageList?topic=sarah_home&user=ulala".getBytes());
    System.out.println(urlEncoded);
    final String decoded = new String(Base64.getDecoder().decode(urlEncoded));
    System.out.println(decoded);
  }

  @Test
  public void testUuid() {
    final String s = UUID.randomUUID().toString();
    System.out.println(s.substring(32));
    System.out.println("35a9014e-d56e-44a8-b2f6-393b8bf7".length());
    System.out.println(UUID.randomUUID().toString());
  }

  private static String generateManifestName(final String station) {
    final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyMMddHH");
    return String.format("%s-%s", station, LocalDateTime.now().format(dateTimeFormatter));
  }

  @Test
  public void testDateFormat() {
    final String mn = generateManifestName("YVR");
    System.out.println(mn);
  }

  @Test
  public void testSplitter() {
    final Tuple2<String, String> map = Splitter.on("<>").withKeyValueSeparator(":").split("a:b").entrySet().stream().findFirst().map(Tuple::fromEntry).orElse(Tuple.of("", ""));
    System.out.println(map);
    Object[] objs = new Object[]{"1", "2", 3};
    final Tuple3<Object, Object, Object> t = Tuple.of(objs[0], objs[1], objs[2]);
    System.out.println(t);
  }

  @Getter
  @Setter
  @Builder
  @ToString
  @AllArgsConstructor
  @NoArgsConstructor
  public static class ManifestView {

    private String manifest;
    @With
    private String status;
    @With
    private Long number;
    @With
    private Long total;
    @With
    private Long incomplete;
  }

  @Test
  public void testMapManifestView() {
    final ManifestView mv1 = ManifestView.builder().manifest("001").status("IN_PROGRESS").number(3L).build();
    final ManifestView mv2 = ManifestView.builder().manifest("001").status("COMPLETED").number(6L).build();
    final ManifestView mv3 = ManifestView.builder().manifest("001").status("IN_PROGRESS").number(5L).build();
    final ManifestView mv4 = ManifestView.builder().manifest("002").status("IN_PROGRESS").number(5L).build();
    final ManifestView mv5 = ManifestView.builder().manifest("002").status("MISSING").number(5L).build();

    final List<ManifestView> result = ImmutableList.of(mv1, mv2, mv3, mv4, mv5);

    //        final Map<String, ManifestView> viewMap = result.stream().filter(Objects::nonNull)
    //            .map(mv -> Optional.ofNullable(mv).filter(v -> Objects.isNull(v.getStatus())).map(v -> mv.withStatus("UNKNOWN")).orElse(mv))
    //            .collect(Collectors.toMap( // to get a map of manifest to its view
    //                ManifestView::getManifest, // key as manifestName
    //                mv -> ManifestView.builder() // values as ManifestView, contains manifestName, total num of Orders, and incomplete num of orders
    //                    .manifest(mv.getManifest())
    //                    .total(mv.getNumber())
    //                    .incomplete(Optional
    //                        .ofNullable(mv.getStatus())
    //                        .filter(status -> !"COMPLETED".equalsIgnoreCase(status))
    //                        .map(stat -> mv.getNumber())
    //                        .orElse(0L))
    //                    .build(),
    //                // merge function to handle key conflicts, just keep manifest the same then sum total and incomplete
    //                (l, r) -> ManifestView.builder().manifest(l.getManifest()).total(l.getTotal() + r.getTotal()).incomplete(l.getIncomplete() + r.getIncomplete()).build()
    //            ));
    //        System.out.println(viewMap);

    final List<ManifestView> lst = result.stream().collect(Collectors.groupingBy(ManifestView::getManifest))
        .entrySet().stream()
        .map(kv -> kv.getValue()
            .stream()
            .reduce(
                ManifestView.builder().manifest(kv.getKey()).total(0L).incomplete(0L).build(),
                (z, mv) -> z.withTotal(z.getTotal() + mv.getNumber()).withIncomplete(!"COMPLETED".equalsIgnoreCase(mv.getStatus()) ? z.getIncomplete() + mv.getNumber() : z.getIncomplete()),
                (z1, z2) -> z1.withTotal(z1.getTotal() + z2.getTotal()).withIncomplete(z1.getIncomplete() + z2.getIncomplete())))
        .collect(Collectors.toList());

    System.out.println(lst);
  }

  @Test
  public void testBigDecimal() {
    final Map<String, BigDecimal> map = ImmutableMap.of("a", new BigDecimal("0.125"));
    System.out.println(JsonUtil.dump(map));

  }

  @Test
  public void testContains() {
    final boolean status = ImmutableList.of(1, 2, 3).contains(null);
    System.out.println(status);
  }

  @Builder
  @Getter
  @Setter
  @ToString
  @EqualsAndHashCode
  @AllArgsConstructor
  @NoArgsConstructor
  public static class DriverSettlementSnapshot {

    @With
    private Integer driverId;
    @With
    private Integer total;
    @With
    private Integer completed;
    @With
    private Integer otpEffective;
    @With
    private BigDecimal otp;
  }

  @Test
  public void testBuilder() {
    DriverSettlementSnapshot.builder().driverId(1).completed(1).otpEffective(0).otp(BigDecimal.ZERO).build();
    //        final double sum = Vector.of(1, 2, null).toJavaList().stream()
    //            .mapToInt(i ->i)
    //            .sum();
    //        System.out.println(sum);

    final double d = 1.99D;
    System.out.println(Math.floor(d));

  }

  public static int findMaxIndex(final List<String> names) {
    return Optional.ofNullable(names).orElse(ImmutableList.of())
        .stream()
        .filter(s -> s.contains("-"))
        .map(s -> s.substring(s.lastIndexOf('-') + 1))
        .mapToInt(s -> Try.of(() -> Integer.parseInt(s)).getOrElse(0))
        .max()
        .orElse(0);
  }

  @Test
  public void testFindMaxIndex() {
    final int max = findMaxIndex(ImmutableList.of("23110715-205-1", "23110715-206-2", "23110715-208-4", "23110716-111-5"));
    System.out.println(max);

    final String manifest = "YVR-23110912";
    final String prefix = manifest.substring(0, manifest.indexOf('-'));
    System.out.println(prefix);
    System.out.println(ImmutableMap.of("YYZ", 1, "YVR", 2));

  }

  public static String extractFsa(final String postCode) {
    return Optional.ofNullable(postCode)
        .filter(s -> StringUtils.length(s) >= 3)
        .map(s -> StringUtils.lowerCase(StringUtils.substring(s, 0, 3)))
        .orElse(null);
  }

  @Test
  public void testExtractFsa() {
    final String fsa = extractFsa("V5J");

    System.out.println(fsa);
  }

  @Test
  public void testMapForeach() {
    final ImmutableMap<Integer, ImmutableList<String>> map = ImmutableMap.of(1, ImmutableList.of("a", "b", "c"), 2, ImmutableList.of("c", "d", "e"));
    map.forEach((k,v)-> System.out.println(k + "=" + v));
  }

}
