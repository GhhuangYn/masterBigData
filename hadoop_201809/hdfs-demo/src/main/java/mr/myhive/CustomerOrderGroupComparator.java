package mr.myhive;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/8 13:30
 */
public class CustomerOrderGroupComparator extends WritableComparator {

    public CustomerOrderGroupComparator() {
        super(CustomerOrderKey.class, true);
    }

    @Override
    public int compare(WritableComparable a, WritableComparable b) {

        CustomerOrderKey a0 = (CustomerOrderKey) a;
        CustomerOrderKey b0 = (CustomerOrderKey) b;
        System.out.println("gorup ====");
        System.out.println(a0.getType() + ":" + a0.getCid() + ":" + b0.getType() + ":" + b0.getCid());
        return a0.getCid() - b0.getCid();               //在每个reduce中进行分组，相同cid的分到一组
    }
}
