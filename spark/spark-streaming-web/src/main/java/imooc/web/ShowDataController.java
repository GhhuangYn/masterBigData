package imooc.web;

import imooc.dao.CourseClickDao;
import imooc.domain.CourseClickCount;
import imooc.domain.DataVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 需求：展示某一天每个课程的访问量
 * @author: HuangYn
 * @date: 2018/11/2 16:41
 */
@Controller
public class ShowDataController {

    @Autowired
    private CourseClickDao courseClickDao;

    private static Map<String, String> dayCourse = new HashMap<>();

    static {
        dayCourse.put("112", "Spark SQL 实战");
        dayCourse.put("145", "Hadoop基础");
        dayCourse.put("139", "Storm实战");
        dayCourse.put("123", "Spark Streaming实战");
        dayCourse.put("169", "大数据面试");
    }

    @GetMapping("/showDay")
    @ResponseBody
    public List<DataVo> getCourseClickCount() throws IOException {

        List<CourseClickCount> list = courseClickDao.getDayCourseClickCount("20181103");
        List<DataVo> dataVoList = new ArrayList<>();
        list.forEach(ccc -> {
            DataVo dataVo = new DataVo();
            dataVo.setName(dayCourse.get(ccc.getDayCourse().split("_")[1]));
            dataVo.setValue(ccc.getClickCount());
            dataVoList.add(dataVo);
        });

        return dataVoList;
    }
}
