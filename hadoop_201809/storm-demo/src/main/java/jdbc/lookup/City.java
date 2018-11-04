package jdbc.lookup;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/10/8 11:06
 */
public class City {

    private int id;
    private String cityName;
    private int cityCode;

    public City() {
    }

    public City(String cityName, int cityCode) {
        this.cityName = cityName;
        this.cityCode = cityCode;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public Integer getCityCode() {
        return cityCode;
    }

    public void setCityCode(Integer cityCode) {
        this.cityCode = cityCode;
    }
}
