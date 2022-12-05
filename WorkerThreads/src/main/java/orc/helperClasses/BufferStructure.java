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
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
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
        StatefulRedisConnection<String, String> connection = newJedisConnection();
        RedisCommands<String, String> jedis = connection.sync();
        jedis.rpush("result", fileName);
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream o = new ObjectOutputStream(bos);
            o.writeObject(records);
            FileOutStream out = createfilealluxios(fileName, "136.145.77.83");
            out.write(bos.toByteArray());
            out.flush();
            out.close();
            records.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
        connection.close();
    }

    public void flushRemainders(){
        flushToFile();
    }
}
