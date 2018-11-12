package callLog.service;

import callLog.domain.CallLog;
import callLog.dao.HBaseDao;
import org.apache.hadoop.hbase.exceptions.DeserializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/11/6 11:22
 */
@Service
public class HBaseService {

    @Autowired
    private HBaseDao hBaseDao;

    public List<CallLog> listLogByRange(String phone, String startDate, String endDate) throws DeserializationException {

        return hBaseDao.listLogByRange(phone,startDate,endDate);
    }



}
