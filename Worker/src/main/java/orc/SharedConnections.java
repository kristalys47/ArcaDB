package orc;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.apache.ignite.configuration.ClientConfiguration;
import redis.clients.jedis.Jedis;

public class SharedConnections {
    static public final String REDIS_HOST = "redis";
    static public final int REDIS_PORT = 6379;
    static public final String IGNITE_HOST = "172.25.213.131"; //136.145.116.98
    static public final int IGNITE_PORT = 10800;
    static public final String IGNITE_HOST_PORT = IGNITE_HOST + ":" + IGNITE_PORT;
    static public final String S3_BUCKET= "testingjoin";

    static public final String AWS_S3_ACCESS_KEY = "AKIA6E4TYZ3JLKC2LPFR";
    static public final String AWS_S3_SECRET_KEY = "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";



}
