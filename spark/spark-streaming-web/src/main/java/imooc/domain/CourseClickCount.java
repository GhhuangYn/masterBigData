package imooc.domain;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/11/2 15:20
 */
public class CourseClickCount {

    private String dayCourse;
    private Long clickCount;

    public CourseClickCount(){}

    public CourseClickCount(String dayCourse, Long clickCount) {
        this.dayCourse = dayCourse;
        this.clickCount = clickCount;
    }

    public String getDayCourse() {
        return dayCourse;
    }

    public void setDayCourse(String dayCourse) {
        this.dayCourse = dayCourse;
    }

    public Long getClickCount() {
        return clickCount;
    }

    public void setClickCount(Long clickCount) {
        this.clickCount = clickCount;
    }

    @Override
    public String toString() {
        return "CourseClickCount{" +
                "dayCourse='" + dayCourse + '\'' +
                ", clickCount=" + clickCount +
                '}';
    }
}
