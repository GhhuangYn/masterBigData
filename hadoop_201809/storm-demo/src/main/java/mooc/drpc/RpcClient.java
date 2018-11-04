package mooc.drpc;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * @Description: RPC客户端
 * @author: HuangYn
 * @date: 2018/10/13 15:12
 */
public class RpcClient {

    public static void main(String[] args) throws IOException {
        Configuration conf = new Configuration();
        UserService userService = RPC.getProxy(UserService.class, UserService.versionID,
                new InetSocketAddress("localhost", 9998), conf, SocketFactory.getDefault());
        userService.addUser("tom", 10);
        System.out.println("rpc client invoke .. ");
        RPC.stopProxy(userService);
    }
}
