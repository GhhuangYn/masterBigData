/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/10/27 19:10
 */
public class VideoAccessTopN {

    private String name;
    private Long value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    @Override
    public String
    toString() {
        return "VideoAccessTopN{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
