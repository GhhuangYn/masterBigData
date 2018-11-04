import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/20 16:01
 */
public class TestMap {

    private static Map<String, Integer> map = new HashMap<>();

    static {
        map.put("leo", 20);
        map.put("lulu", 30);
        map.put("cc", 21);
    }

    @Test
    public void testKeySet() {

        Set<String> keySet = map.keySet();
        keySet.iterator().forEachRemaining((key) -> {
            Integer age = map.get(key);
            System.out.println(key + ":" + age);
        });
    }

    @Test
    public void testEntrySet() {

        Set<Map.Entry<String, Integer>> entrySet = map.entrySet();
        for (Map.Entry<String, Integer> entry : entrySet){
            System.out.println(entry.getKey()+":"+entry.getValue());
        }
    }
}
