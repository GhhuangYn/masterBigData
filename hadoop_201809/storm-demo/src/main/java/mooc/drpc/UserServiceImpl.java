package mooc.drpc;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/10/13 15:05
 */
public class UserServiceImpl implements UserService {

    @Override
    public void addUser(String name, int age) {
        System.out.println("Service Invoked: add user success..name is " + name);
    }
}
