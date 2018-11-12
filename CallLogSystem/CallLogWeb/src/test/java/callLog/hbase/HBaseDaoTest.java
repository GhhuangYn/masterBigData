package callLog.hbase;

import callLog.domain.CallLog;
import callLog.domain.CallLogDateRange;
import callLog.dao.HBaseDao;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/11/7 13:17
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class HBaseDaoTest {

    @Autowired
    private HBaseDao hBaseDao;

    @Test
    public void listLogByRange() {
        List<CallLog> list = hBaseDao.listLogByRange("18879735663", "20180101", "20180904");
        list.forEach(System.out::println);
    }

    @Test
    public void getRowKeyRange() {
        List<CallLogDateRange> ranges = hBaseDao.getRowKeyRange("18879735663", "20180101", "20180904");
        Assert.assertNotNull(ranges);
        ranges.forEach(System.out::println);
    }



}