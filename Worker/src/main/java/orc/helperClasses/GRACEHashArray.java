package orc.helperClasses;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.util.*;

import static orc.Commons.*;

public class GRACEHashArray {
    private int buckets;
    private int FILE_SIZE = 4096;
    private LinkedList<String>[] fileBuckets;
    private LinkedList<Tuple>[] records;
    private String relation;
    private int recordsLimit = 200;

    // TODO: Create own Linked List that will autoflush.

    public GRACEHashArray(int buckets, int recordSize, String relation) {
        this.buckets = buckets;
        this.fileBuckets = new LinkedList[buckets];
//        this.recordsLimit = Math.floorDiv(FILE_SIZE, recordSize);
        this.records = new LinkedList[buckets];

        for (int i = 0; i < buckets; i++) {
            fileBuckets[i] = new LinkedList<>();
            records[i] = new LinkedList<>();
        }
    }


    public void addRecord(Tuple record) throws FileNotFoundException {
        IntegerAttribute key = (IntegerAttribute) record.readAttribute(0);
        int hashValue = (int) Math.abs(key.value % buckets);
        records[hashValue].add(record);
        checkFileFull(hashValue);
    }

    public void checkFileFull(int bucket) throws FileNotFoundException {
        if(records[bucket].size() >= recordsLimit){
            flushToFile(bucket);
        }
    }

    private void flushToFile(int bucket) {
        //TODO: this can change depending if the container does this think in the disk or in the cache
        int mode = 2;
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

    //TODO: this is private should be
    public Map<String, HashNode<String>> readRecords(int bucket){
        Map<String, HashNode<String>> map = new TreeMap<>();
        for (int i = 0; i < fileBuckets[bucket].size(); i++) {
            try {
                FileInputStream reader = new FileInputStream(fileBuckets[bucket].get(i));
                Scanner scanner = new Scanner(reader, "UTF-8").useDelimiter("\n");
                String theString = null;

                while (scanner.hasNext()) {
                    theString = scanner.next();
                    String[] read = theString.split(",", 2);
                    if(!map.containsKey(read[0])){
                        map.put(read[0], new HashNode<String>(read[1], null));
                    } else {
                        map.put(read[0], new HashNode<String>(read[1], map.get(read[0])));
                    }
                }
                scanner.close();

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return map;
    }

    public LinkedList<String> getFileBuckets(int bucket) {
        return fileBuckets[bucket];
    }

}

