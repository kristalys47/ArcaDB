package orc.helperClasses;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import redis.clients.jedis.Jedis;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;

import static orc.Commons.*;


public class GRACEHashArrayInParts {
    private int buckets;
    private int FILE_SIZE = 16384;
    public LinkedList<String>[] fileBuckets;
    private LinkedList<Tuple>[] records;
    private int recordsLimit = 200;
    private String relation;
    private int mode;

    // TODO: Create own Linked List that will autoflush.

    public GRACEHashArrayInParts(int buckets, int recordSize, String relation, int mode) {
        this.buckets = buckets;
        this.relation = relation;
        this.mode = mode;
        this.fileBuckets = new LinkedList[buckets];
        //TODO: get object size
//        this.recordsLimit = Math.floorDiv(FILE_SIZE, recordSize);
        this.records = new LinkedList[buckets];

        for (int i = 0; i < buckets; i++) {
            fileBuckets[i] = new LinkedList<>();
            records[i] = new LinkedList<>();
        }
    }


    public LinkedList<String>[] getFileBuckets() {
        return fileBuckets;
    }

    public void addRecord(Tuple record) {
        IntegerAttribute key = (IntegerAttribute) record.readAttribute(0);
        int hashValue = (int) Math.abs(key.value % buckets);
        records[hashValue].add(record);
        checkFileFull(hashValue);
    }

    public void checkFileFull(int bucket) {
        if(records[bucket].size() >= recordsLimit){
            flushToFile(bucket);
        }
    }

    private void flushToFile(int bucket) {
        String fileName = "/join/" + bucket + "/" + this.relation + "/" + this.fileBuckets[bucket].size() + "_" + this.hashCode();
        Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT);
        jedis.rpush("/join/" + bucket + "/" + this.relation + "/", this.fileBuckets[bucket].size() + "_" + this.hashCode());
        fileBuckets[bucket].add(fileName);

        System.out.println(fileName + " - " + records[bucket].size());

        AmazonS3 s3client = null;
        if(mode == 2){
            AWSCredentials credentials = new BasicAWSCredentials(AWS_S3_ACCESS_KEY, AWS_S3_SECRET_KEY);
            s3client = AmazonS3ClientBuilder
                    .standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withRegion(Regions.US_EAST_1)
                    .build();
        }
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream o = new ObjectOutputStream(bos);
            o.writeObject(records[bucket]);
            if(mode == 2) {
                InputStream in = new ByteArrayInputStream(bos.toByteArray());
                s3client.putObject(S3_BUCKET, fileName, in, new ObjectMetadata());
            } else {
                jedis.set(fileName.getBytes(), bos.toByteArray());
            }
//            IgniteClient client = Ignition.startClient(new ClientConfiguration().setAddresses(IGNITE_HOST_PORT));
//            ClientCache<String, LinkedList<Tuple>> cache = client.getOrCreateCache("join");
//            cache.put(fileName, records[bucket]);
//            client.close();
            records[bucket].clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void flushRemainders(){
        for (int i = 0; i < buckets; i++) {
            {
                if(records[i].size() > 0){
                    flushToFile(i);
                }
            }
        }
    }
}

