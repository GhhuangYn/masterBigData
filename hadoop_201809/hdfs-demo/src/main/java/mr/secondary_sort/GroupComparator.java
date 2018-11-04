package mr.secondary_sort;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/5 20:45
 */
public class GroupComparator extends WritableComparator {

    public GroupComparator() {
        super(Combokey.class,true);
    }

    @Override
    public int compare(WritableComparable a, WritableComparable b) {
        System.out.println("=========== GroupComparator ============");
        Combokey combokeyA = (Combokey) a;
        Combokey combokeyB = (Combokey) b;
        return combokeyA.getYear() - combokeyB.getYear();

    }
}
