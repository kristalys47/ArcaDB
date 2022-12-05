package orc;

import alluxio.AlluxioURI;
import alluxio.client.file.FileInStream;
import alluxio.client.file.FileOutStream;
import alluxio.client.file.FileSystem;
import alluxio.conf.Configuration;
import alluxio.conf.PropertyKey;
import alluxio.exception.AlluxioException;
import alluxio.grpc.CreateFilePOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.support.ConnectionPoolSupport;
import org.apache.commons.pool2.impl.SoftReferenceObjectPool;

import java.io.IOException;
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
    static public int CONNECTION_RETRIES;
    static protected RedisClient redisClient;
    static protected SoftReferenceObjectPool<StatefulRedisConnection<String, String>>  pool;
//    static protected GenericObjectPool<StatefulRedisConnection<String, String>>  pool;
//     static protected StatefulRedisConnection<String, String> connection;

    static public String ip = null;
    static {
        CONNECTION_RETRIES = 5;
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

    static public FileOutStream createfilealluxios(String fileName, String host) throws IOException, AlluxioException {
        Configuration.set(PropertyKey.SECURITY_LOGIN_USERNAME, "root");
        Configuration.set(PropertyKey.MASTER_HOSTNAME, host);
        FileSystem fs = FileSystem.Factory.create();
        AlluxioURI path = new AlluxioURI("alluxio://" + host + ":19998" + fileName);
        CreateFilePOptions options = CreateFilePOptions
                .newBuilder()
                .setRecursive(true)
                .build();
        Configuration.set(PropertyKey.MASTER_HOSTNAME, host);
        FileOutStream out = fs.createFile(path, options);
        return out;
    }
    static public FileInStream getfilealluxios(String path, String host) throws IOException, AlluxioException {
        Configuration.set(PropertyKey.SECURITY_LOGIN_USERNAME, "root");
        Configuration.set(PropertyKey.MASTER_HOSTNAME, host);
        FileSystem fs = FileSystem.Factory.get();
        AlluxioURI pathAlluxio = new AlluxioURI("alluxio://" + host + ":19998" + path);
        alluxio.conf.Configuration.set(PropertyKey.MASTER_HOSTNAME, host);
        FileInStream in = fs.openFile(pathAlluxio);
        return in;
    }
}
