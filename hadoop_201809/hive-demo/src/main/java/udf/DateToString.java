package udf;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/13 16:18
 */
@Description(name = "dateToString",
        value = "_FUNC_() - transform date to string")
public class DateToString extends UDF {

    public String evaluate() {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    public String evaluate(String format) {

        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date());
    }
}
