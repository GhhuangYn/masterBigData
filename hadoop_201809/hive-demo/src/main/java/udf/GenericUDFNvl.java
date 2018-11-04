package udf;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentTypeException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDFUtils;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/14 15:06
 */
@Description(name = "nvl",
        value = "_FUNC_(value,default_value) - returns defalut value if value is null")
public class GenericUDFNvl extends GenericUDF {

    //returnOIResolver通过获取非null值得变量的类型并使用这个类型确认返回值。
    private GenericUDFUtils.ReturnObjectInspectorResolver returnOIResolver;
    private ObjectInspector[] argumentsOIs;

    //这个方法会被输入的每个输入参数调用，并传入到ObjectInspector对象中
    //可以利用ObjectInspector去判断传入参数的类型和个数
    //这个方法的目的是确定参数的返回类型
    //returnOIResolver通过获取非null值的变量的类型并确定返回值
    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        argumentsOIs = arguments;
        if (argumentsOIs.length != 2) {
            throw new UDFArgumentException("nvl() needs two arguments");
        }
        returnOIResolver = new GenericUDFUtils.ReturnObjectInspectorResolver(true);
        if (!(returnOIResolver.update(arguments[0]) && returnOIResolver.update(arguments[1]))) {
            throw new UDFArgumentTypeException(2, "mismatch params type:" +
                    arguments[0].getTypeName() + "," + arguments[1].getTypeName());
        }
        return returnOIResolver.get();
    }

    //这个函数返回的是第一个非null的值
    @Override
    public Object evaluate(DeferredObject[] arguments) throws HiveException {

        //returnOIResolver从DeferredObject中获取值，
        Object retVal = returnOIResolver.convertIfNecessary(arguments[0].get(), argumentsOIs[0]);
        if (retVal == null) {
            retVal = returnOIResolver.convertIfNecessary(arguments[1].get(), argumentsOIs[1]);
        }
        return retVal;
    }

    //展示调试信息
    @Override
    public String getDisplayString(String[] children) {
        StringBuffer sb = new StringBuffer();
        sb.append("if ");
        sb.append(children[0]);
        sb.append(" is null ");
        sb.append("returns");
        sb.append(children[1]);
        return sb.toString();
    }
}
