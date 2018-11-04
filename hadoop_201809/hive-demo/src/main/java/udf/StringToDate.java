package udf;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/13 16:18
 */
@Description(name = "stringToDate",
        value = "_FUNC_() - transform string to date")
public class StringToDate extends UDF {

    public Date evaluate() {
        return new Date();
    }

    public Date evaluate(String dateStr) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
        return sdf.parse(dateStr);
    }
}
