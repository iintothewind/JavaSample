package sample.basic;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Slf4j
public class DateUtil {
    public static LocalDate dateToLocalDate(final Date date) {
        return Optional.ofNullable(date).map(Date::toInstant).map(instant -> instant.atZone(ZoneId.systemDefault()).toLocalDate()).orElse(null);
    }

    public static Date localDateToDate(final LocalDate localDate) {
        return Optional.ofNullable(localDate).map(ld -> Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant())).orElse(null);
    }
}
