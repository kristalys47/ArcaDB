package orc;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.support.ConnectionPoolSupport;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.commons.pool2.impl.SoftReferenceObjectPool;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;

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
    static protected SoftReferenceObjectPool<StatefulRedisConnection<String, String>>  pool;
//    static protected GenericObjectPool<StatefulRedisConnection<String, String>>  pool;
//     static protected StatefulRedisConnection<String, String> connection;

    static public String ip = null;
    static {
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    static public StatefulRedisConnection<String, String> newJedisConnection() {
        if(redisClient == null) {
            RedisURI uri = new RedisURI(REDIS_HOST, REDIS_PORT, Duration.ofMinutes(120));
            redisClient = RedisClient.create(uri);
//
//            pool = ConnectionPoolSupport.createGenericObjectPool(() -> redisClient.connect(), new GenericObjectPoolConfig());
            pool = ConnectionPoolSupport.createSoftReferenceObjectPool(() -> redisClient.connect());
        }
        StatefulRedisConnection<String, String> connection = null;
        try {
            connection = pool.borrowObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        RedisCommands<String, String> jedis = connection.sync();
        return connection;
    }
    static public void closeConnection(){

    }

    static public void createConnectiontoAlluxios(){

    }
}
