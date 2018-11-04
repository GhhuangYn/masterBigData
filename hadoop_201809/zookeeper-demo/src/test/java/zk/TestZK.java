package zk;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.junit.Test;
import sun.util.resources.LocaleData;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/15 16:06
 */
public class TestZK {

    @Test
    public void listChildren() throws Exception {
        ZooKeeper zooKeeper = new ZooKeeper("node01:2181", 5000, null, true);
        getDir(zooKeeper, "/");
    }

    private void getDir(ZooKeeper zookeeper, String dir) throws Exception {

        List<String> children = zookeeper.getChildren(dir, null);
        if (children.size() == 1) {
            System.out.println(dir + "/" + children.get(0));
        } else {
            children.forEach((child) -> {
                String parent;
                if (dir.equals("/")) {
                    parent = dir + child;
                } else {
                    parent = dir + "/" + child;
                }
                System.out.println(parent);
                try {
                    getDir(zookeeper, parent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

    }

    @Test
    public void testGet() throws Exception {
        ZooKeeper zooKeeper = new ZooKeeper("node01:2181", 5000, null, true);
        Stat stat = new Stat();   //存放元数据
        byte[] bytes = zooKeeper.getData("/a", null, stat);
        System.out.println(new String(bytes));
        System.out.println(stat.getVersion());
        System.out.println(new Date(stat.getCtime()));

        Stat result = zooKeeper.setData("/a", "hayu".getBytes(), stat.getVersion());
        System.out.println(result.getVersion());

    }

    @Test
    public void testSet() throws Exception {
        ZooKeeper zooKeeper = new ZooKeeper("node01:2181,node02:2181,node03:2181", 5000, null, true);
        String data = "";
        for (int i = 0; i < 100040; i++) {
            data += i;
        }
        Stat result = zooKeeper.setData("/a", data.getBytes(), 13);
        System.out.println(result.getVersion());
        

    }

    //创建有序节点
    @Test
    public void createSeqZnode() throws IOException, KeeperException, InterruptedException {

        ZooKeeper zooKeeper = new ZooKeeper("node01:2181,node02:2181,node03:2181", 5000, null, true);
        zooKeeper.create("/a/myapp", "YYYYYYY".getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);

//        System.out.println(zooKeeper.getSessionId());
    }

    //监视器，一般情况，注册一次只能监听一次
    @Test
    public void testWatcher() throws IOException, KeeperException, InterruptedException {

        ZooKeeper zooKeeper = new ZooKeeper("node01:2181", 5000, null, true);
        Stat stat = new Stat();
        Watcher watcher = new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("数据改了！！");

                try {
                    zooKeeper.getData("/a", this, null);    //每次回调了这个参数之后就再次向zk注册
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        byte[] bytes = zooKeeper.getData("/a", watcher, stat);
        System.out.println(new String(bytes));
        while (true) {
            Thread.sleep(1000);
        }
    }

}
