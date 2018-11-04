package udf;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/13 16:18
 */
@Description(name = "myAdd",
        value = "_FUNC_(a,b) - from the input integer a,b "+
                "returns the result of a+b",
        extended = "Example:\n"
                + " > SELECT _FUNC_(a,b); the result is a+b\n"
                + " > SELECT _FUNC_(a,b,c) the result is a+b+c;")
public class AddUDF extends UDF {

    public int evaluate(int a, int b) {
        return a + b;
    }

    public int evaluate(int a, int b, int c) {
        return a + b + c;
    }
}
