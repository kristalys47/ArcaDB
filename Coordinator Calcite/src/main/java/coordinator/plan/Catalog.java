package coordinator.plan;

import redis.clients.jedis.Jedis;

import java.util.List;

import static coordinator.Commons.*;

public class Catalog {
    public static List<String> filesForTable(String tableName){
        String path =  tableName ;
        Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT);
        List<String> files = jedis.lrange(path,0, -1);
//        List<String> files1 = jedis.lrange(path,0, -1);
//        files.add(files1.get(0));
        return files;
    }
}