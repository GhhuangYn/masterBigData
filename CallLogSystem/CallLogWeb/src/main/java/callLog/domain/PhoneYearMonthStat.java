package callLog.domain;

import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/11/12 0:26
 */
public class PhoneYearMonthStat {

    private String phone;
    private List<Map<String,Integer>> monthCount;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<Map<String, Integer>> getMonthCount() {
        return monthCount;
    }

    public void setMonthCount(List<Map<String, Integer>> monthCount) {
        this.monthCount = monthCount;
    }
}
