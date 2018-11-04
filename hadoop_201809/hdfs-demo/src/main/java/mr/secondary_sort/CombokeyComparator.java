package mr.secondary_sort;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/5 21:10
 */
public class CombokeyComparator extends WritableComparator {

    //必须进行实例化，否则会报空指针
    public CombokeyComparator() {
        super(Combokey.class,true);
    }

    @Override
    public int compare(WritableComparable a, WritableComparable b) {
        System.out.println("=========== CombokeyComparator ============");

        Combokey combokeyA = (Combokey) a;
        Combokey combokeyB = (Combokey) b;
        return combokeyA.compareTo(combokeyB);
    }
}
