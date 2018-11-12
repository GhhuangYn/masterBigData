package callLog.hive;

import callLog.dao.HiveDao;
import callLog.domain.CallLog;
import callLog.domain.PhoneYearMonthStat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/11/11 18:43
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class HiveDaoTest {

    @Autowired
    private HiveDao hiveDao;

    @Test
    public void listLast10() {
        List<CallLog> list = hiveDao.listLatest10("13147193238");
        list.forEach(System.out::println);
    }

    @Test
    public void eachMonthStat() {
        PhoneYearMonthStat phoneYearMonthStat = hiveDao.listPhoneMonthStat("13422115728", "2018");
        phoneYearMonthStat.getMonthCount().forEach(m -> {
            m.forEach((k, v) -> {
                System.out.println(k + ":" + v);
            });
        });
    }
}