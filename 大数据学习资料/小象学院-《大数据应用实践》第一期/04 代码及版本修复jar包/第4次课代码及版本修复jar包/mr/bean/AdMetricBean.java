package bigdata.mr.bean;

import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by xiaoguanyu on 2017/12/28.
 * 实现Writable接口，序列化
 */

public class AdMetricBean implements Writable{
    private long click;
    private long pv;
    //反序列化时需要调用空参构造函数，如果空参构造函数被覆盖，一定要显示定义一下，否则在反序列时会抛异常
    public AdMetricBean(){}



    //定义带参数的构造方法
    public AdMetricBean(long pv,long click){
        this.pv = pv;
        this.click = click;
    }

    public long getPv() {
        return pv;
    }

    public void setPv(long pv) {
        this.pv = pv;
    }

    public long getClick() {
        return click;
    }

    public void setClick(long click) {
        this.click = click;
    }


    /**
     * 序列化
     * @param dataOutput
     * @throws IOException
     * 序列化时按顺序写出
     */
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeLong(pv);
        dataOutput.writeLong(click);
    }
    /**
     * 反序列化
     * @param dataInput
     * @throws IOException
     * 反序列化的顺序跟序列化的顺序一致
     */
    public void readFields(DataInput dataInput) throws IOException {
        //由于在序列化的时候先写入的pv，所以反序列化的时候先拿出来pv
        pv = dataInput.readLong();
        click = dataInput.readLong();
    }

}
