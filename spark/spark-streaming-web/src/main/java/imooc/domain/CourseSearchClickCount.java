package imooc.domain;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/11/2 15:21
 */
public class CourseSearchClickCount {

    private String dayFrom;
    private Long clickCount;



    public String getDayFrom() {
        return dayFrom;
    }

    public void setDayFrom(String dayFrom) {
        this.dayFrom = dayFrom;
    }

    public Long getClickCount() {
        return clickCount;
    }

    public void setClickCount(Long clickCount) {
        this.clickCount = clickCount;
    }
}
