package orc;

import alluxio.AlluxioURI;
import alluxio.client.file.FileInStream;
import alluxio.client.file.FileSystem;

import alluxio.conf.PropertyKey;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.google.gson.JsonArray;
import orc.helperClasses.*;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.exec.vector.BytesColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.LongColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.VectorizedRowBatch;
import org.apache.orc.OrcFile;
import org.apache.orc.Reader;
import org.apache.orc.TypeDescription;
import org.apache.orc.impl.RecordReaderImpl;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.util.*;

import static orc.Commons.*;

public class JoinManager {

    public static void readerPrint(String path)throws IOException {
        HashMap<Long, String> hm = new HashMap<Long, String>();
        Configuration conf = new Configuration();

        Reader reader = OrcFile.createReader(new Path(path), OrcFile.readerOptions(conf));
//        TypeDescription schema = reader.getSchema();
        RecordReaderImpl records = (RecordReaderImpl) reader.rows(reader.options());
        VectorizedRowBatch batch = reader.getSchema().createRowBatch();

        while (records.nextBatch(batch)) {
            for(int r=0; r < batch.size; ++r) {
                BytesColumnVector stringVector = (BytesColumnVector)  batch.cols[1];
                LongColumnVector intVector = (LongColumnVector) batch.cols[0];
                hm.put(intVector.vector[r], new String(stringVector.vector[r], stringVector.start[r], stringVector.length[r]));
            }
        }
        records.close();
        batch.reset();

        Reader reader2 = OrcFile.createReader(new Path("/JavaCode/insertedTest"), OrcFile.readerOptions(conf));
        TypeDescription schema2 = reader2.getSchema();
        RecordReaderImpl records2 = (RecordReaderImpl) reader2.rows(reader2.options());
        VectorizedRowBatch batch2 = reader2.getSchema().createRowBatch();


        while (records2.nextBatch(batch2)) {
            for(int r=0; r < batch2.size; ++r) {
                BytesColumnVector stringVector = (BytesColumnVector)  batch2.cols[1];
                LongColumnVector intVector = (LongColumnVector) batch2.cols[0];
                if(hm.containsKey(intVector.vector[r])){
                    //make function
                    hm.put(intVector.vector[r], hm.get(intVector.vector[r]) + " " +  new String(stringVector.vector[r], stringVector.start[r], stringVector.length[r]));
                }
            }
        }
        records2.close();
        batch2.reset();
//        System.out.println(hm.toString());
    }

    public static void joinPartition(String path, String column, String relation, String buckets, int mode) throws IOException {
        long start = System.currentTimeMillis();
        AWSCredentials credentials = new BasicAWSCredentials(AWS_S3_ACCESS_KEY, AWS_S3_SECRET_KEY);
        AmazonS3 s3client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.US_EAST_1)
                .build();
        InputStream in = s3client.getObject(S3_BUCKET, path).getObjectContent();
        FileUtils.copyInputStreamToFile(in, new File(path));
        long end = System.currentTimeMillis();
//        Jedis jedisr = new Jedis(REDIS_HOST_TIMES, REDIS_PORT_TIMES);
//        jedisr.rpush("times", "Partition (Read File) " + ip + " " + start + " " + end + " " + (end-start));
        System.out.println("TIME_LOG: Partition (Read File) " + ip + " " + start + " " + end + " " + (end-start));
//        System.out.println("File retrieved from s3");
        scannedToMap(path, column, relation, Integer.valueOf(buckets), mode);
    }

    public static void joinProbing(String pathS, String pathR, String bucketID, int mode) throws IOException {
        int bucket = Integer.valueOf(bucketID);
        Map<String, HashNode<Tuple>> map = new TreeMap<>();
        Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT);
        AmazonS3 s3client = null;
        if(mode == 2){
            AWSCredentials credentials = new BasicAWSCredentials(AWS_S3_ACCESS_KEY, AWS_S3_SECRET_KEY);
            s3client = AmazonS3ClientBuilder
                    .standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withRegion(Regions.US_EAST_1)
                    .build();
        }
        long start = System.currentTimeMillis();
        while(jedis.llen(pathS) > 0){
            try {
                String jkey = pathS + jedis.lpop(pathS);
                ByteArrayInputStream b = null;
                if (mode == 2) {
                    InputStream in = s3client.getObject(S3_BUCKET, jkey).getObjectContent();
                    b = new ByteArrayInputStream(in.readAllBytes());
                } else {
                    alluxio.conf.Configuration.set(PropertyKey.MASTER_HOSTNAME, "136.145.77.83");
                    alluxio.conf.Configuration.set(PropertyKey.SECURITY_LOGIN_USERNAME, "root");

                    FileSystem fs = FileSystem.Factory.get();
                    AlluxioURI path = new AlluxioURI("alluxio://136.145.77.83:19998"+jkey);
                    FileInStream in = fs.openFile(path);
                    b = new ByteArrayInputStream(in.readAllBytes());
//                    b = new ByteArrayInputStream(jedis.get(jkey.getBytes()));
                    in.close();
                }
                ObjectInputStream o = new ObjectInputStream(b);
                LinkedList<Tuple> records = (LinkedList<Tuple>) o.readObject();
                for (Tuple record : records) {
                    String key = record.readAttribute(0).getStringValue();
                    if (!map.containsKey(key)) {
                        map.put(key, new HashNode<Tuple>(record, null));
                    } else {
                        map.put(key, new HashNode<Tuple>(record, map.get(key)));
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            //Making sure the connection exits
            jedis.close();
            jedis = new Jedis(REDIS_HOST, REDIS_PORT);
        }
        long end = System.currentTimeMillis();
//        Jedis jedisr = new Jedis(REDIS_HOST_TIMES, REDIS_PORT_TIMES);
//        jedisr.rpush("times", "Probing (Create Hash) " + ip + " " + start + " " + end + " " + (end-start));
        System.out.println("TIME_LOG: Probing (Create Hash) " + ip + " " + start + " " + end + " " + (end-start));
        start = System.currentTimeMillis();

        //REdefinition of broken pipe
        jedis.close();
        jedis = new Jedis(REDIS_HOST, REDIS_PORT);
        while(jedis.llen(pathR) > 0){
            try {
                String jkey = pathR + jedis.lpop(pathR);
                ByteArrayInputStream b = null;
                if (mode == 2) {
                    InputStream in = s3client.getObject(S3_BUCKET, jkey).getObjectContent();
                    b = new ByteArrayInputStream(in.readAllBytes());
                } else {

                    alluxio.conf.Configuration.set(PropertyKey.MASTER_HOSTNAME, "136.145.77.83");
                    alluxio.conf.Configuration.set(PropertyKey.SECURITY_LOGIN_USERNAME, "root");

                    FileSystem fs = FileSystem.Factory.get();
                    AlluxioURI path = new AlluxioURI("alluxio://136.145.77.83:19998" + jkey);
                    FileInStream in = fs.openFile(path);
                    b = new ByteArrayInputStream(in.readAllBytes());
                    in.close();

//                    b = new ByteArrayInputStream(jedis.get(jkey.getBytes()));
                }

                ObjectInputStream o = new ObjectInputStream(b);
                LinkedList<Tuple> records = (LinkedList<Tuple>) o.readObject();
                jedis = new Jedis(REDIS_HOST, REDIS_PORT);
                BufferStructure results = new BufferStructure("", Integer.parseInt(jedis.get("joinTupleLength")));
                for (Tuple record : records) {
                    String key = record.readAttribute(0).getStringValue();
                    if(map.containsKey(key)){
                        HashNode<Tuple> current = map.get(key);
                        do {
                            Tuple joined = Tuple.joinTuple(record, current.getElement());
                            results.addRecord(joined);
                            current = current.getNext();
                        }while(current != null);
                    }
                }
                results.flushRemainders();
            } catch (Exception e){
                e.printStackTrace();
            }
            //Making sure it exist for the next loop
            jedis.close();
            jedis = new Jedis(REDIS_HOST, REDIS_PORT);
        }
        jedis.close();
        end = System.currentTimeMillis();
//        jedisr.rpush("times", "Probing (Join) " + ip + " " + start + " " + end + " " + (end-start));
        System.out.println("TIME_LOG: Probing (Join) " + ip + " " + start + " " + end + " " + (end-start));
        //        jedisr.close();
    }

    private static LinkedList<Tuple> getPartitions(String path, int mode, Jedis jedis, AmazonS3 s3client) throws IOException, ClassNotFoundException {
        String jkey = path + jedis.lpop(path);
        ByteArrayInputStream b = null;
        if (mode == 2) {
            InputStream in = s3client.getObject(S3_BUCKET, jkey).getObjectContent();
            b = new ByteArrayInputStream(in.readAllBytes());
        } else {
            b = new ByteArrayInputStream(jedis.get(jkey.getBytes()));
        }
        ObjectInputStream o = new ObjectInputStream(b);
        LinkedList<Tuple> records = (LinkedList<Tuple>) o.readObject();
        return records;

        //                IgniteClient client = Ignition.startClient(new ClientConfiguration().setAddresses(IGNITE_HOST_PORT));
//                ClientCache<String, LinkedList<Tuple>> cache = client.getOrCreateCache("join");
//                LinkedList<Tuple> records = cache.get(pathS + jedis.lpop(pathS));
//                client.close();
    }

    public static void join(JsonArray pathR, String columnR, String relationR, JsonArray pathS, String columnS, String relationS, String buckets, int mode) throws IOException {

        int bucket = Integer.valueOf(buckets);
        //TODO: ******************** what to do if the join is with the same table but different columns
        //TODO: what to do with the fixed bucket size
        //TODO: where do i save the list?
        long start = System.currentTimeMillis();
        orcToMap(pathR, columnR, bucket,  relationR, mode);
        orcToMap(pathS, columnS, bucket,  relationS, mode);
        long end = System.currentTimeMillis();
//        Jedis jedisr = new Jedis(REDIS_HOST_TIMES, REDIS_PORT_TIMES);
//        jedisr.rpush("times", "Partition Single " + ip + " " + start + " " + end + " " + (end-start));
        System.out.println("TIME_LOG: Partition Single " + ip + " " + start + " " + end + " " + (end-start));

        //TODO: Do i want threading?
//        ExecutorService pool = Executors.newFixedThreadPool(10);
//        TODO: ****************** select the one with least size to be the map
        start = System.currentTimeMillis();
        for (int i = 0; i < bucket; i++) {
            String r = "/join/" + i + "/" + relationR + "/";
            String s = "/join/" + i + "/" + relationS + "/";

//            System.out.println(r + " - " + s);
            joinProbing(s, r, String.valueOf(i), mode);
        }
        end = System.currentTimeMillis();
//        jedisr.rpush("times", "Probing Single " + ip + " " + start + " " + end + " " + (end-start));
        System.out.println("TIME_LOG: Probing Single " + ip + " " + start + " " + end + " " + (end-start));

    }
    public static void scannedToMap(String path, String column, String relation, int buckets, int mode) throws IOException {
        long start = System.currentTimeMillis();
        Configuration conf = new Configuration();

        Reader reader = OrcFile.createReader(new Path(path), OrcFile.readerOptions(conf));
//        TypeDescription schema = reader.getSchema();
        RecordReaderImpl records = (RecordReaderImpl) reader.rows(reader.options());
//        System.out.println("crea el reader");
        VectorizedRowBatch batch = reader.getSchema().createRowBatch();
        int joinKey = reader.getSchema().getFieldNames().indexOf(column.replace("\"", ""));
        // TODO: size needs to be calculated, make a formula for that.

        //TODO: Generate this key
        //TODO: change string

        TypeDescription schema = reader.getSchema();
        List<String> colName = schema.getFieldNames();
        List<TypeDescription> colType = schema.getChildren();
        GRACEHashArrayInParts table = null;// new GRACEHashArrayInParts(buckets, 100);
        AES hashing = new AES("helohelohelohelo");


        while (records.nextBatch(batch)) {
//            System.out.println("reading...");
            for(int r=0; r < batch.size; ++r) {
                Tuple created = new Tuple(batch.cols.length + 1);
                StringBuilder key = new StringBuilder();
                for (int j = 0; j < batch.cols.length; j++) {
                    created.addAttribute(colType.get(j), j+1, colName.get(j), batch.cols[j], r);
                    if(j == joinKey){
                        batch.cols[j].stringifyValue(key, r);
                        created.addAttribute(Attribute.AttributeType.Long, 0, colName.get(j), (hashFunction(key.toString(), hashing)), r);
                    }
                }
                // TODO: create better hashfunction
                if(table == null) {
                    //fix the relation name
                    table = new GRACEHashArrayInParts(buckets, 1, relation, mode);
                }
                table.addRecord(created);
            }
        }


//        System.out.println("Termina el batch");
        records.close();
        table.flushRemainders();
//        System.out.println("Termina el batch2");
        long end = System.currentTimeMillis();
//        Jedis jedis = new Jedis(REDIS_HOST_TIMES, REDIS_PORT_TIMES);
//        jedis.rpush("times", "Partition (Tuples to Buckets) " + ip + " " + start + " " + end + " " + (end-start));
        System.out.println("TIME_LOG: Partition (Tuples to Buckets) " + ip + " " + start + " " + end + " " + (end-start));
    }

    public static void orcToMap(JsonArray path, String column, int buckets, String relation, int mode) throws IOException {
        Configuration conf = new Configuration();
//        System.out.println(path);
        AWSCredentials credentials = new BasicAWSCredentials(AWS_S3_ACCESS_KEY, AWS_S3_SECRET_KEY);
        AmazonS3 s3client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.US_EAST_1)
                .build();

        GRACEHashArrayInParts table = new GRACEHashArrayInParts(buckets, 1, relation, mode);
//        Jedis jedisr = new Jedis(REDIS_HOST_TIMES, REDIS_PORT_TIMES);
        for (int i = 0; i < path.size(); i++) {
            long start = System.currentTimeMillis();
            InputStream in = s3client.getObject(S3_BUCKET, path.get(i).getAsString()).getObjectContent();
            FileUtils.copyInputStreamToFile(in, new File(path.get(i).getAsString()));
            long end = System.currentTimeMillis();
//            jedisr.rpush("times", "Partition (Reading File) " + ip + " " + start + " " + end + " " + (end-start));
            System.out.println("TIME_LOG: Partition (Reading File) " + ip + " " + start + " " + end + " " + (end-start));
//            System.out.println("File retrieved from s3");
            Reader reader = OrcFile.createReader(new Path(path.get(i).getAsString()), OrcFile.readerOptions(conf));
            RecordReaderImpl records = (RecordReaderImpl) reader.rows(reader.options());
            VectorizedRowBatch batch = reader.getSchema().createRowBatch();
            int joinKey = reader.getSchema().getFieldNames().indexOf(column);
            // TODO: size needs to be calculated, make a formula for that.
            //TODO: Generate this key
            //TODO: change string
            TypeDescription schema = reader.getSchema();
            List<String> colName = schema.getFieldNames();
            List<TypeDescription> colType = schema.getChildren();
            AES hashing = new AES("helohelohelohelo");
            start = System.currentTimeMillis();
            while (records.nextBatch(batch)) {
                for(int r=0; r < batch.size; ++r) {
                    Tuple created = new Tuple(batch.cols.length + 1);
                    StringBuilder key = new StringBuilder();
                    for (int j = 0; j < batch.cols.length; j++) {
                        created.addAttribute(colType.get(j), j+1, colName.get(j), batch.cols[j], r);
                        if(j == joinKey){
                            batch.cols[j].stringifyValue(key, r);
                            created.addAttribute(Attribute.AttributeType.Long, 0, colName.get(j), (hashFunction(key.toString(), hashing)), r);
                        }
                    }
                    table.addRecord(created);
                }
            }
            end = System.currentTimeMillis();
//            jedisr.rpush("times", "Partition (Tuples to bucket) " + ip + " " + start + " " + end + " " + (end-start));
            System.out.println("TIME_LOG: Partition (Tuples to bucket) " + ip + " " + start + " " + end + " " + (end-start));
            records.close();
            batch.reset();
        }
        table.flushRemainders();
    }

    public static long hashFunction(String s, AES hashing){
        long result = hashing.encrypt(s);
        return result;
    }

}

