import util.MySqlUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 获取数据的dao
 * @author: HuangYn
 * @date: 2018/10/27 19:09
 */
public class TopNDao {

    //这里定义一些课程id和课程名的映射
    private static Map<Long, String> courses = new HashMap<>();

    static {
        courses.put(32L, "Scala入门到精通");
        courses.put(12L, "SpringBoot高级");
        courses.put(11L, "python数据分析");
        courses.put(6L, "Java数据科学");
        courses.put(29L, "Java设计模式");
    }

    public List<VideoAccessTopN> listVideoAccessTopN(String day) {

        Connection connection = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        List<VideoAccessTopN> result = new ArrayList<>();
        try {
            connection = MySqlUtil.getConnetcion();
            pst = connection.prepareStatement(
                    "SELECT * FROM day_video_access_topn_stat WHERE day=? ORDER BY times DESC limit 5");
            pst.setString(1, day);
            rs = pst.executeQuery();
            while (rs.next()) {
                VideoAccessTopN domain = new VideoAccessTopN();
                domain.setName(courses.get(rs.getLong("cmsId")));
                domain.setValue(rs.getLong("times"));
                result.add(domain);
            }
            return result;

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            MySqlUtil.release(connection, pst, rs);
        }
        return null;
    }

    public static void main(String[] args) {
        TopNDao topNDao = new TopNDao();
        List<VideoAccessTopN> list = topNDao.listVideoAccessTopN("20170718");
        list.forEach(System.out::println);
    }

}
