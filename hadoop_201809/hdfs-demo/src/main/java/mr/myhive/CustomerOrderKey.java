package mr.myhive;

import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/8 12:12
 */
public class CustomerOrderKey implements WritableComparable {

    private int type;
    private int cid;
    private String customerInfo = "";
    private String orderInfo = "";

    @Override
    public int compareTo(Object o) {
        return 0;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(type);
        out.writeInt(cid);
        out.writeUTF(customerInfo);
        out.writeUTF(orderInfo);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        type = in.readInt();
        cid = in.readInt();
        customerInfo = in.readUTF();
        orderInfo = in.readUTF();
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public String getCustomerInfo() {
        return customerInfo;
    }

    public void setCustomerInfo(String customerInfo) {
        this.customerInfo = customerInfo;
    }

    public String getOrderInfo() {
        return orderInfo;
    }

    public void setOrderInfo(String orderInfo) {
        this.orderInfo = orderInfo;
    }

    @Override
    public String toString() {
        return "CustomerOrderKey{" +
                "type=" + type +
                ", cid=" + cid +
                ", customerInfo='" + customerInfo + '\'' +
                ", orderInfo='" + orderInfo + '\'' +
                '}';
    }
}
