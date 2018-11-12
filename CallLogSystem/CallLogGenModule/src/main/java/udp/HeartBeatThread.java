package udp;

import utils.PropertyUtils;

import java.io.IOException;
import java.net.*;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/11/12 10:03
 */
public class HeartBeatThread extends Thread {

    private DatagramSocket socket;

    public HeartBeatThread() {
        try {
            socket = new DatagramSocket(PropertyUtils.getInt("heartbeat.udp.send.port"));
            this.setDaemon(true);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        byte[] bytes = new byte[1];
        bytes[0] = (byte) PropertyUtils.getInt("heartbeat.udp.broadcast.flag");
        DatagramPacket packet = new DatagramPacket(bytes, 0, 1);        //发送消费者标识
        String hostname = PropertyUtils.getString("heartbeat.udp.broadcast.host");
        int port = PropertyUtils.getInt("heartbeat.udp.broadcast.port");
        packet.setSocketAddress(new InetSocketAddress(hostname, port));
        int heartBeat = PropertyUtils.getInt("heartbeat.udp.time.ms");
        try {
            socket.send(packet);
            Thread.sleep(heartBeat);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
