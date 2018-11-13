package cn.chinahadoop.udf;

import org.apache.hadoop.hive.ql.exec.UDF;

/**
 * Created by xiaoguanyu on 2018/1/2.
 */
public class AgePartitionFunction extends UDF {
    public String evaluate(int age) {
        String partition = "p0";
        if(age <=20){
            partition = "p0";
        }else if(age > 20 && age <=50){
            partition = "p1";
        }else if(age > 50){
            partition = "p2";
        }
        return partition;
    }
}
