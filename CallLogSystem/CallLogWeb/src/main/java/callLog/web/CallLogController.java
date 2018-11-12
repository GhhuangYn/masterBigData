package callLog.web;

import callLog.service.HBaseService;
import callLog.service.HiveService;
import org.apache.hadoop.hbase.exceptions.DeserializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;


/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/11/7 10:50
 */
@Controller
public class CallLogController {

    @Autowired
    private HBaseService hBaseService;
    @Autowired
    private HiveService hiveService;

    @GetMapping("/index")
    public String index() {
        return "index";
    }

    @PostMapping("/findCallLog")
    public ModelAndView findCallLogByRange(@RequestParam("phone") String phone,
                                           @RequestParam("start") String startDate,
                                           @RequestParam("end") String endDate) throws DeserializationException {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("showLog");
        modelAndView.addObject("myPhone", phone);
        modelAndView.addObject("logs", hBaseService.listLogByRange(phone, startDate, endDate));
        return modelAndView;
    }

    @PostMapping
    public ModelAndView findLatest(String phone) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("showLatest");
        modelAndView.addObject("logs", hiveService.listLatest10(phone));
        return modelAndView;
    }
}
