package hbase;

import java.text.DecimalFormat;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/11/7 23:43
 */
public class HBaseUtils {

    public static String getRegionNum(String from, String date) {
        DecimalFormat df = new DecimalFormat("00");
        String last4 = from.substring(7, from.length());
        String yearMonth = date.substring(0, 6);
        return df.format((Long.valueOf(yearMonth) ^ Long.valueOf(last4)) % 100);
    }

    //rowkey格式： regionNo,from,date,to,duration.flag
    public static String getRowKey(String from, String to, String dateTime, String duration) {
        int flag = 0;       //主叫标记为0，被叫为1
        return getRegionNum(from, dateTime.substring(0, 8)) + "," + from + "," + dateTime + "," + to + "," + duration + "," + flag;
    }
}
