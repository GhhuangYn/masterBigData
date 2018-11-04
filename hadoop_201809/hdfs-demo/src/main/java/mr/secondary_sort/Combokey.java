package mr.secondary_sort;

import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/5 17:33
 */
public class Combokey implements WritableComparable {

    private int year;
    private int temperature;

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    @Override
    public int compareTo(Object o) {
        Combokey combokey = (Combokey) o;
        if (combokey.getYear() != this.getYear()) {
            //年份升序排列
            return this.getYear() - combokey.getYear();
        } else {
            //气温降序排列
            return -(this.getTemperature() - combokey.getTemperature());
        }
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(year);
        out.writeInt(temperature);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.year = in.readInt();
        this.temperature = in.readInt();
    }
}
