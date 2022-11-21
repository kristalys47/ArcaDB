package orc;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public class Commons {

    static public int WORKER_APP_PORT;
    static public int COORDINATOR_APP_PORT;
    static public String REDIS_HOST;
    static public int REDIS_PORT;
    static public String REDIS_HOST_TIMES;
    static public int REDIS_PORT_TIMES;
    static public String S3_BUCKET;
    static public String AWS_S3_ACCESS_KEY;
    static public String AWS_S3_SECRET_KEY;
    static public String POSTGRES_PASSWORD;
    static public String POSTGRES_USERNAME;
    static public String POSTGRES_HOST;
    static public int POSTGRES_PORT;
    static public String POSTGRES_DB_NAME;
    static public String POSTGRES_JDBC;
    static public String MODE;
    static protected RedisClient redisClient;
    // static protected StatefulRedisConnection<String, String> connection;

    static public String ip = null;
    static {
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    static public RedisCommands<String, String> newJedisConnection(){
        if(redisClient == null) {
            redisClient = RedisClient.create("redis://" + REDIS_HOST + ":" + REDIS_PORT);
        }
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();

        return syncCommands;
    }
    static public void closeConnection(){

    }

    static public void createConnectiontoAlluxios(){

    }
}
