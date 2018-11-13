package bigdata.mr.bean;

import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by xiaoguanyu on 2017/12/28.
 * ʵ��Writable�ӿڣ����л�
 */

public class AdMetricBean implements Writable{
    private long click;
    private long pv;
    //�����л�ʱ��Ҫ���ÿղι��캯��������ղι��캯�������ǣ�һ��Ҫ��ʾ����һ�£������ڷ�����ʱ�����쳣
    public AdMetricBean(){}



    //����������Ĺ��췽��
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
     * ���л�
     * @param dataOutput
     * @throws IOException
     * ���л�ʱ��˳��д��
     */
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeLong(pv);
        dataOutput.writeLong(click);
    }
    /**
     * �����л�
     * @param dataInput
     * @throws IOException
     * �����л���˳������л���˳��һ��
     */
    public void readFields(DataInput dataInput) throws IOException {
        //���������л���ʱ����д���pv�����Է����л���ʱ�����ó���pv
        pv = dataInput.readLong();
        click = dataInput.readLong();
    }

}
