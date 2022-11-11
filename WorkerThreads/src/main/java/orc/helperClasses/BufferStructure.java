package orc.helperClasses;

import alluxio.AlluxioURI;
import alluxio.client.file.FileOutStream;
import alluxio.client.file.FileSystem;
import alluxio.conf.Configuration;
import alluxio.conf.PropertyKey;
import alluxio.grpc.CreateFilePOptions;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import orc.Commons;
import redis.clients.jedis.Jedis;
import java.util.UUID;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;

import static orc.Commons.*;

public class BufferStructure {
    private LinkedList<Tuple> records;
    private int recordsLimit = 200;
    private String result;
    private int count = 0;

    public BufferStructure(String result, int recordsLimit){
        this.result = result;
        //TODO: get object size
        this.recordsLimit = recordsLimit;
        this.records = new LinkedList<>();
    }


    public void addRecord(Tuple record) {
        records.add(record);
        checkFileFull();
    }

    public void checkFileFull() {
        if(records.size() >= recordsLimit){
            flushToFile();
        }
    }

    private void flushToFile() {
        String fileName = "/results/" + ip + "_" + this.hashCode() + "_" + UUID.randomUUID().toString();
        this.count++;
        Jedis jedis = Commons.newJedisConnection();
        jedis.rpush("result", fileName);

//        System.out.println(fileName + " - " + records[bucket].size());
//        AmazonS3 s3client = null;
//        if(mode == 2){
//            AWSCredentials credentials = new BasicAWSCredentials(AWS_S3_ACCESS_KEY, AWS_S3_SECRET_KEY);
//            s3client = AmazonS3ClientBuilder
//                    .standard()
//                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
//                    .withRegion(Regions.US_EAST_1)
//                    .build();
//        }
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream o = new ObjectOutputStream(bos);
            o.writeObject(records);
//            if(mode == 2) {
//                InputStream in = new ByteArrayInputStream(bos.toByteArray());
//                s3client.putObject(S3_BUCKET, fileName, in, new ObjectMetadata());
//            } else {

                Configuration.set(PropertyKey.MASTER_HOSTNAME, "136.145.77.83");
                Configuration.set(PropertyKey.SECURITY_LOGIN_USERNAME, "root");

                FileSystem fs = FileSystem.Factory.create();
                AlluxioURI path = new AlluxioURI("alluxio://136.145.77.83:19998" + fileName);
                CreateFilePOptions options = CreateFilePOptions
                        .newBuilder()
                        .setRecursive(true)
                        .build();
                FileOutStream out = fs.createFile(path, options);
                out.write(bos.toByteArray());
                out.flush();
                out.close();

//                jedis.set(fileName.getBytes(), bos.toByteArray());

//            }
//            IgniteClient client = Ignition.startClient(new ClientConfiguration().setAddresses(IGNITE_HOST_PORT));
//            ClientCache<String, LinkedList<Tuple>> cache = client.getOrCreateCache("join");
//            cache.put(fileName, records[bucket]);
//            client.close();
            records.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
        jedis.close();
    }

    public void flushRemainders(){
        flushToFile();
    }
}
