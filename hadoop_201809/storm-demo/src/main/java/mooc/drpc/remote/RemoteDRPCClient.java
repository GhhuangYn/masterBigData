package mooc.drpc.remote;

import groovy.util.ConfigObject;
import org.apache.storm.Config;
import org.apache.storm.thrift.TException;
import org.apache.storm.thrift.transport.TTransportException;
import org.apache.storm.utils.DRPCClient;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/10/13 16:14
 */
public class RemoteDRPCClient {

    public static void main(String[] args) throws TException {
        DRPCClient client = new DRPCClient(new Config(), "node00", 3772);
        String result = client.execute("addUser", "hello");
        System.out.println("Client invoked: " + result);
    }
}
