package callLog.dao;

import callLog.domain.CallLog;
import callLog.domain.CallLogDateRange;
import callLog.hbase.HBaseUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/11/6 11:05
 */
@Repository
public class HBaseDao {

    private Admin admin;
    private Configuration conf;
    private Connection connection;
    private String tableName = "callLog";
    private DecimalFormat df = new DecimalFormat("00");

    @Autowired
    private PersonRepository personRepository;

    public HBaseDao() {

        try {
            conf = HBaseConfiguration.create();
            conf.set("hbase.zookeeper.qurom", "node00:2181,node03:2181");
            connection = ConnectionFactory.createConnection(conf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //按照时间范围查询通话记录
    public List<CallLog> listLogByRange(String phone, String startDate, String endDate) {

        List<CallLog> callLogList = new ArrayList<>();

        try {

            Table table = connection.getTable(TableName.valueOf(tableName));
            List<CallLogDateRange> ranges = getRowKeyRange(phone, startDate, endDate);
            ranges.forEach(range -> {
                Scan scan = new Scan();
                scan.setStartRow(Bytes.toBytes(range.getStartRow()));
                scan.setStopRow(Bytes.toBytes(range.getEndRow()));

                ResultScanner resultScanner;
                try {
                    resultScanner = table.getScanner(scan);
                    for (Result result : resultScanner) {

                        String[] row = new String(result.getRow()).split(",");
                        CallLog callLog = new CallLog();

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
                        //如果是主叫
                        if ("0".equals(row[5])) {
                            callLog.setType(0);
                            callLog.setFrom(phone);
                            String to = new String(result.getValue(Bytes.toBytes("cf1"), Bytes.toBytes("to")));
                            callLog.setTo(to);
                            callLog.setToName(personRepository.findByPhone(to).getName());
                            callLog.setDate(sdf.parse(new String(result.getValue(Bytes.toBytes("cf1"), Bytes.toBytes("date")))));
                            callLog.setFromName(personRepository.findByPhone(phone).getName());
                            callLog.setDuration(Long.valueOf(Bytes.toString(result.getValue(Bytes.toBytes("cf1"), Bytes.toBytes("duration")))));
                            callLogList.add(callLog);
                        } else if ("1".equals(row[5])) {

                            //被叫rowkey格式： regionNo,to,date,from,duration.flag
                            callLog.setTo(row[1]);
                            callLog.setToName(personRepository.findByPhone(row[1]).getName());
                            callLog.setDate(sdf.parse(row[2]));
                            callLog.setFrom(row[3]);
                            callLog.setFromName(personRepository.findByPhone(row[3]).getName());
                            callLog.setDuration(Long.valueOf(row[4]));
                            callLog.setType(1);
                            callLogList.add(callLog);

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            return callLogList;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 获取查询的rowkey范围
     * 每个月份单独给定一个查询范围
     *
     * @param phone
     * @param start 年月日
     * @param end
     * @return
     */
    public List<CallLogDateRange> getRowKeyRange(String phone, String start, String end) {

        LocalDate startDate = LocalDate.parse(start, DateTimeFormatter.ofPattern("yyyyMMdd"));
        LocalDate endDate = LocalDate.parse(end, DateTimeFormatter.ofPattern("yyyyMMdd"));
        List<CallLogDateRange> ranges = new ArrayList<>();

        //如果是同年
        if (startDate.getYear() == endDate.getYear()) {

            //遍历同年的月份
            LocalDate tmp = startDate;
            while (endDate.getMonthValue() - tmp.getMonthValue() > 0) {

                String startString = tmp.toString().replace("-", "");
                String startRegionNum = HBaseUtils.getRegionNum(phone, startString);
                String startRow = startRegionNum + "," + phone + "," + startString.substring(0, 6);
                CallLogDateRange range = new CallLogDateRange();
                range.setStartRow(startRow);
                range.setEndRow(startRow + "endTag");
                ranges.add(range);
                tmp = tmp.plusMonths(1);        //每次循环添加一个月
            }
        }
        return ranges;
    }


}