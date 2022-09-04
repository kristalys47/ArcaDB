package orc;

import java.net.InetAddress;
import java.net.UnknownHostException;

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

    static public String ip = null;
    static {
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}

