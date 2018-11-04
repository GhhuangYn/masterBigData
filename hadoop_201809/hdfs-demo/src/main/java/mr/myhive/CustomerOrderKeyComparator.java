package mr.myhive;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/8 13:39
 */
public class CustomerOrderKeyComparator extends WritableComparator {

    public CustomerOrderKeyComparator() {
        super(CustomerOrderKey.class, true);
    }

    //按照key进行排序，先排customer的，再排order的
    @Override
    public int compare(WritableComparable a, WritableComparable b) {

        CustomerOrderKey a0 = (CustomerOrderKey) a;
        CustomerOrderKey b0 = (CustomerOrderKey) b;

        if (a0.getType() == 0 && b0.getType() == 0) {       //如果都是customer
            return a0.getCid() - b0.getCid();
        } else if (a0.getType() == 1 && b0.getType() == 1) {
            return a0.getCid() - b0.getCid();
        } else if (a0.getType() == 0 && b0.getType() == 1) {
            return a0.getCid() - b0.getCid();          //如果是customer和order相比，直接把cus排在order前
        } else {
            return a0.getCid() - b0.getCid();
        }

    }
}
