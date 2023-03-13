package orc.helperClasses;

import alluxio.AlluxioURI;
import alluxio.client.file.FileOutStream;
import alluxio.client.file.FileSystem;
import alluxio.conf.Configuration;
import alluxio.conf.PropertyKey;
import alluxio.grpc.CreateFilePOptions;
import redis.clients.jedis.Jedis;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.UUID;

import static orc.Commons.*;


public class GRACEHashArrayInParts {
    private int buckets;
    //TODO: In an ideal world, this changes according to what is wanted.
    // Pass within the metadata of the plan.
//    private int FILE_SIZE = 16384;
    public LinkedList<String>[] fileBuckets;
    private LinkedList<Tuple>[] records;
    private int recordsLimit = 200;
    private String relation;

    // TODO: Create own Linked List that will autoflush.

    public GRACEHashArrayInParts(int buckets, String relation) {
        this.buckets = buckets;
        this.relation = relation;
        this.fileBuckets = new LinkedList[buckets];
        //TODO: get object size
        Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT);
        //TODO:SEND metadata with this info to plan
        this.recordsLimit = 50000;
        jedis.close();
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
        LongAttribute key = (LongAttribute) record.readAttribute(0);
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
        String uuid = UUID.randomUUID().toString();
        String fileName = "/join/" + bucket + "/" + this.relation + "/" + this.fileBuckets[bucket].size() + "_" + this.hashCode() + "_" + uuid;
        Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT);
        jedis.rpush("/join/" + bucket + "/" + this.relation + "/", this.fileBuckets[bucket].size() + "_" + this.hashCode() + "_" + uuid);
        fileBuckets[bucket].add(fileName);
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream o = new ObjectOutputStream(bos);
            o.writeObject(records[bucket]);
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

            records[bucket].clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
        jedis.close();
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
