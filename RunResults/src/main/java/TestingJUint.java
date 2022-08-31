import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.junit.Test;

import java.io.File;

public class TestingJUint {
    @Test
    public void uploadFilesToAS3(){
        AWSCredentials credentials = new BasicAWSCredentials(
                "AKIA6E4TYZ3JLKC2LPFR",
                "UaMYsDlAzWeFCx0r1So4/gZLZIkbgO21kVXiDoP1"
        );
        AmazonS3 s3client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.US_EAST_1)
                .build();

        for (int i = 0; i < 7; i++) {
            s3client.putObject(
                    "testingjoin",
                    "/product/product.orc",
                    new File("")
            );
        }

    }

}
