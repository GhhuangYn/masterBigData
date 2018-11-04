package imooc.dao;

import imooc.domain.CourseClickCount;
import imooc.domain.CourseSearchClickCount;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/11/2 15:18
 */
@Repository
public class CourseClickDao {

    private String cf = "info";
    private String qualfier = "click_count";

    //查询某一天所有课程的访问记录
    public List<CourseClickCount> getDayCourseClickCount(String day) throws IOException {
        Table table = HBaseUtils.getInstance().getTable("imooc_course_clickcount");
        Filter dayFilter = new PrefixFilter(day.getBytes());
        Scan scan = new Scan();
        scan.setFilter(dayFilter);
        ResultScanner resultScanner = table.getScanner(scan);
        List<CourseClickCount> list = new ArrayList<>();
        resultScanner.forEach(result -> {
            CourseClickCount ccc = new CourseClickCount();
            ccc.setDayCourse(new String(result.getRow()));
            ccc.setClickCount(Bytes.toLong(result.getValue(cf.getBytes(), qualfier.getBytes())));
            list.add(ccc);
        });
        return list;
    }

    public CourseSearchClickCount getDayCourseSearchClickCount(String day, String from) {
        Table table = HBaseUtils.getInstance().getTable("imooc_course_search_clickcount");
        try {
            Result result = table.get(new Get(Bytes.toBytes(day + "_" + from)));
            long count = Bytes.toLong(result.getValue(Bytes.toBytes(cf), Bytes.toBytes(qualfier)));
            CourseSearchClickCount cscc = new CourseSearchClickCount();
            cscc.setDayFrom(day + "_" + from);
            cscc.setClickCount(count);
            return cscc;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        CourseClickDao dao = new CourseClickDao();
        List<CourseClickCount> list = dao.getDayCourseClickCount("20181102");
        list.forEach(System.out::println);
    }

}
