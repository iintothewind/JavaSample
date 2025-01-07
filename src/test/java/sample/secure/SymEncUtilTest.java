package sample.secure;

import java.time.ZoneId;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class SymEncUtilTest {

    @Test
    public void testSymEncUtilEnc01() {
        final String input = null;
        final String enced = SymEncUtil.enc(input);
        final String deced = SymEncUtil.dec(enced);
        Assertions.assertThat(deced).isEqualTo(input);
    }

    @Test
    public void testSymEncUtilEnc02() {
        final String input = "";
        final String enced = SymEncUtil.enc(input);
        final String deced = SymEncUtil.dec(enced);
        Assertions.assertThat(deced).isEqualTo(input);
    }


    @Test
    public void testSymEncUtilEnc03() {
        final String input = "                 ";
        final String enced = SymEncUtil.enc(input);
        final String deced = SymEncUtil.dec(enced);
        Assertions.assertThat(deced).isEqualTo(input);
    }

    @Test
    public void testSymEncUtilEnc04() {
        final String input = "A transformation is a string that describes the operation (or set of operations) to be performed on the given input, to produce some output.";
        final String enced = SymEncUtil.enc(input);
        final String deced = SymEncUtil.dec(enced);
        Assertions.assertThat(deced).isEqualTo(input);
    }

    @Test
    public void testSymEncUtilEnc05() {
        final String input = null;
        Assertions.assertThat(SymEncUtil.dec(input)).isEqualTo(input);
    }

    @Test
    public void testSymEncUtilEnc06() {
        final String input = "";
        Assertions.assertThat(SymEncUtil.dec(input)).isEqualTo(input);
    }

    @Test
    public void testSymEncUtilEnc07() {
        final String input = "         ";
        Assertions.assertThat(SymEncUtil.dec(input)).isEqualTo(input);
    }

    @Test
    public void testSymEncUtilEnc08() {
        final String input = "         ";
        final String enced = SymEncUtil.enc(input);
        Assertions.assertThat(SymEncUtil.enc(SymEncUtil.enc(SymEncUtil.enc(enced)))).isEqualTo(enced);
        Assertions.assertThat(SymEncUtil.dec(SymEncUtil.dec(SymEncUtil.enc(SymEncUtil.enc(enced))))).isEqualTo(input);
    }

    @Test
    public void testSymEncUtilEnc09() {
        final String input = "#{}#";
        final String deced = SymEncUtil.dec(input);
        Assertions.assertThat(deced).isEmpty();
    }

    @Test
    public void testGetAvailableZoneIds() {
        Set<String> zoneIds = ZoneId.getAvailableZoneIds();
        System.out.println(zoneIds);
    }
}
