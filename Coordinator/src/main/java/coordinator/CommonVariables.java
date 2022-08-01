package coordinator;

import redis.clients.jedis.Jedis;

public class CommonVariables {

    static public Jedis jedis = new Jedis("redis", 6379);

    static public final int APP_PORT  = 7272;

}
