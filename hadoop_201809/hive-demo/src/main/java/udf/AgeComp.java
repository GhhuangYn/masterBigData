package udf;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/14 14:41
 */
@Description(name = "age comparator",
        value = "_FUNC_() - compare age ")
public class AgeComp extends UDF {

    public String evaluate(String name, int age) {

        if (age < 20) {
            System.out.println(name + " is younger than 20 years old");
            return name + "is " + age + ", < 20";
        } else if (age < 30) {
            System.out.println(name + " is older than 20 years old,smaller than 30 years old");
            return name + "is " + age + ", > 20 , <30";
        } else {
            System.out.println(name + " is older than 30 years old");
            return name + "is " + age + ", > 30";
        }
    }
}
