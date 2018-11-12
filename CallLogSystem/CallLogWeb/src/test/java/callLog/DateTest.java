package callLog;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/11/9 21:18
 */
public class DateTest {

    @Test
    public void testDate() {
        String end = "20180803";
        LocalDate startDate = LocalDate.of(
                Integer.parseInt(end.substring(0, 4)),
                Integer.parseInt(end.substring(4, 6)),
                Integer.parseInt(end.substring(6, 8)));
        System.out.println(startDate.getMonthValue() + ":" + startDate.getDayOfMonth());

        LocalDate date = LocalDate.parse("20180502", DateTimeFormatter.ofPattern("yyyyMMdd"));
        System.out.println(date.toString());
    }

    @Test
    public void test2() throws ParseException {
        String str = "20180505 050505";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HHmmss");
        Date date = sdf.parse(str);
        System.out.println(date.toString());
    }
}
