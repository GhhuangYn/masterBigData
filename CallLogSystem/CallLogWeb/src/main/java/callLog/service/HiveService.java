package callLog.service;

import callLog.dao.HiveDao;
import callLog.domain.CallLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/11/11 19:34
 */
@Service
public class HiveService {

    @Autowired
    private HiveDao hiveDao;

    public List<CallLog> listLatest10(String phone) {
        return hiveDao.listLatest10(phone);
    }
}
