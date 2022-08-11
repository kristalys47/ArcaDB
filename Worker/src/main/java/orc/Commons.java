package orc;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Commons {
    static public final String REDIS_HOST = "136.145.116.85"; //redis
    static public final int REDIS_PORT = 6379;
    static public final String REDIS_HOST_TIMES = "136.145.116.85"; //redis
    static public final int REDIS_PORT_TIMES = 6380;
    static String ip;
    static {
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

//    static public final String IGNITE_HOST = REDIS_HOST; //136.145.116.98
//    static public final int IGNITE_PORT = 10800;
//    static public final String IGNITE_HOST_PORT = IGNITE_HOST + ":" + IGNITE_PORT;

    static public final String S3_BUCKET= "testingjoin";
    static public final String AWS_S3_ACCESS_KEY = "AKIA6E4TYZ3JLKC2LPFR";
    static public final String AWS_S3_SECRET_KEY = "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";

}

