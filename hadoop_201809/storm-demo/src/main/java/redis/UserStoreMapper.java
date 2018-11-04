package redis;

import org.apache.storm.redis.common.mapper.RedisDataTypeDescription;
import org.apache.storm.redis.common.mapper.RedisStoreMapper;
import org.apache.storm.tuple.ITuple;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/10/13 18:40
 */
public class UserStoreMapper implements RedisStoreMapper {

    private RedisDataTypeDescription description;

    public UserStoreMapper() {
        description = new RedisDataTypeDescription(
                RedisDataTypeDescription.RedisDataType.STRING);
    }

    @Override
    public RedisDataTypeDescription getDataTypeDescription() {
        return description;
    }

    @Override
    public String getKeyFromTuple(ITuple iTuple) {
        System.out.println("key:"+iTuple.getStringByField("id"));
        return iTuple.getStringByField("id");
    }

    @Override
        public String getValueFromTuple(ITuple iTuple) {
        System.out.println("value:"+iTuple.getStringByField("name"));
        return iTuple.getStringByField("name");
    }
}
