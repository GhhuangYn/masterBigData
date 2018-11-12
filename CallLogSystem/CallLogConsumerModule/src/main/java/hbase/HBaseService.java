package hbase;

import java.text.DecimalFormat;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/11/6 11:22
 */
public class HBaseService {

    private HBaseDao hBaseDao;
    private DecimalFormat df;
    private String cf = "cf1";

    public HBaseService() {
        hBaseDao = new HBaseDao();
        df = new DecimalFormat("00");
    }

    public void save(String log) {

        String[] logArr = log.split(",");
        String from = logArr[0];
        String to = logArr[1];
        String dateTime = logArr[2].replace("-", "").replace(":", "").replace("T", "");
        String duration = logArr[3];

        //rowkey格式: regionNum,from,date,to,duration
        String rowkey = HBaseUtils.getRowKey(from, to, dateTime, duration);
        String[] qualifiers = {"from", "to", "date", "duration"};
        String[] values = {from, to, dateTime, duration};
        hBaseDao.save("callLog", rowkey, cf, qualifiers, values);
    }


       public static void main(String[] args) {
        HBaseService service = new HBaseService();
        service.save("18271656035,13147193666,2018-07-03T05:29:41,777");
    }

}
