package mooc.drpc;

/**
 * @Description: hadoop rpc演示
 * @author: HuangYn
 * @date: 2018/10/13 15:04
 */
public interface UserService {

    long versionID = 88888888;
    void addUser(String name,int age);
}
