package callLog.dao;

import callLog.domain.CallLog;
import callLog.domain.PhoneYearMonthStat;
import org.apache.avro.generic.GenericData;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.hibernate.cfg.annotations.ResultsetMappingSecondPass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * @Description: 使用hive与hbase整合, 使得数据分析更加简便
 * @author: HuangYn
 * @date: 2018/11/11 17:16
 */
@Repository
public class HiveDao {

    @Autowired
    private PersonRepository personRepository;

    private Connection connection;
    private String hiveUrl = "jdbc:hive2://node03:10000/rel";

    public HiveDao() {

        try {
            Class.forName("org.apache.hive.jdbc.HiveDriver");
            connection = DriverManager.getConnection(hiveUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<CallLog> listLatest10(String phone) {

        List<CallLog> list = new ArrayList<>();
        String hql = "select * from ext_calllogs_in_hbase where caller = ? or to = ? order by date desc limit 10";
        try {
            PreparedStatement pst = connection.prepareStatement(hql);
            pst.setString(1, phone);
            pst.setString(2, phone);
            ResultSet resultSet = pst.executeQuery();
            while (resultSet.next()) {
                CallLog callLog = new CallLog();
                callLog.setFrom(resultSet.getString("caller"));
                callLog.setFromName(personRepository.findByPhone(callLog.getFrom()).getName());
                callLog.setTo(resultSet.getString("to"));
                callLog.setToName(personRepository.findByPhone(callLog.getTo()).getName());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                callLog.setDate(sdf.parse(resultSet.getString("date")));
                callLog.setDuration(resultSet.getLong("duration"));
                if (resultSet.getString("caller").equals(phone)) {
                    callLog.setType(0);
                } else {
                    callLog.setType(1);
                }
                list.add(callLog);
            }
            resultSet.close();
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //统计某个电话一年的通话记录数
    public PhoneYearMonthStat listPhoneMonthStat(String phone, String year) {

        String hql = "select count(*) c,substring(date,1,6) ym from ext_calllogs_in_hbase where caller=? or to=? and SUBSTRING(date,1,4)=? group by substring(date,1,6)";

        try {
            PreparedStatement pst = connection.prepareStatement(hql);
            pst.setString(1, phone);
            pst.setString(2, phone);
            pst.setString(3, year);
            ResultSet resultset = pst.executeQuery();
            PhoneYearMonthStat pyms = new PhoneYearMonthStat();
            pyms.setPhone(phone);
            List<Map<String, Integer>> list = new ArrayList<>();
            while (resultset.next()) {
                Map<String, Integer> monthCount = new HashMap<>();
                monthCount.put(resultset.getString("ym"), resultset.getInt("c"));
                list.add(monthCount);
            }
            pyms.setMonthCount(list);
            return pyms;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
