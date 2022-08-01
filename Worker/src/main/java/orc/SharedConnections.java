package orc;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectListing;
import org.apache.ignite.configuration.ClientConfiguration;
import redis.clients.jedis.Jedis;

public class SharedConnections {
    static public Jedis jedis = new Jedis("redis", 6379);

    static public ClientConfiguration cfg = new ClientConfiguration().setAddresses("136.145.116.98:10800");

    static public AWSCredentials credentials = new BasicAWSCredentials(
            "AKIA6E4TYZ3JLKC2LPFR",
            "UaMYsDlAzWeFCx0r1So4/gZLZIkbgO21kVXiDoP1"
    );

    static public AmazonS3 s3client = AmazonS3ClientBuilder
            .standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.US_EAST_1)
            .build();

    static public String s3Bucket = "testingjoin";
}
