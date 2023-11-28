package sample.http;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import org.junit.Test;
import io.vavr.API;


public class BeanTest {
    @Test
    public void testGetter() {
        final TestBean tb = TestBean.builder().a("ssss").b("dddd").build();
        System.out.println(tb);
    }

    public static int mtch(final int a, final int b) {
        return API.Match(API.Tuple(a, b)).of(
            API.Case(API.$(t -> t._1 == 1 && t._2 == 2), a + b),
            API.Case(API.$(t -> t._1 == 3 && t._2 == 4), a * b),
            API.Case(API.$(), 0)
        );
    }

    public static String getDateStr(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(date);
    }

    public static Date localDateToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static Date getDateFromStr(String dateStr, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        LocalDate localDate = LocalDate.parse(dateStr, formatter);
        return localDateToDate(localDate);
    }

    @Test
    public void testPatternMatch() {
        final int result1 = mtch(1,2);
        System.out.println(result1);
        final int result2 = mtch(3,4);
        System.out.println(result2);
        final int result3 = mtch(5,4);
        System.out.println(result3);
    }

    @Test
    public void testDateUtil() {
        final Date dte = getDateFromStr("230723", "yyMMdd");
        System.out.println(getDateStr(dte, "yyyy-MM-dd"));

        final String placeholder = "Invoice";
        System.out.println();
        System.out.println("RF-Reliable-WeeklyInvoice230724".substring("RF-Reliable-WeeklyInvoice230724".indexOf(placeholder) + placeholder.length()));
        final String dtr = "RF-Reliable-WeeklyInvoice230724".substring("RF-Reliable-WeeklyInvoice230724".indexOf(placeholder) + placeholder.length());

    }

}
