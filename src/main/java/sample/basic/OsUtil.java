package sample.basic;

import io.vavr.API;
import io.vavr.control.Try;

public class OsUtil {
    public enum OS {
        WINDOWS, LINUX, MAC, GENERIC
    }

    public static OS detectOs() {
        final String property = Try.of(() -> System.getProperty("os.name", "generic").toLowerCase()).getOrNull();
        final OS os = API.Match(property).of(
                API.Case(API.$(p -> p.contains("win")), p -> OS.WINDOWS),
                API.Case(API.$(p -> p.contains("nix")), p -> OS.LINUX),
                API.Case(API.$(p -> p.contains("aix")), p -> OS.LINUX),
                API.Case(API.$(p -> p.contains("nux")), p -> OS.LINUX),
                API.Case(API.$(p -> p.contains("mac")), p -> OS.MAC),
                API.Case(API.$(), p -> OS.GENERIC));
        return os;
    }
}
