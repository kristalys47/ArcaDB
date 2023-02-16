package coordinator.Utils;

import coordinator.Commons;
import redis.clients.jedis.Jedis;

import java.util.List;

public class Catalog {
    public static List<String> filesForTable(String tableName){
        String path =  tableName ;
        Jedis jedis = new Jedis(Commons.REDIS_HOST, Commons.REDIS_PORT);
        List<String> files = jedis.lrange(path,0, -1);
        return files;
    }
}
