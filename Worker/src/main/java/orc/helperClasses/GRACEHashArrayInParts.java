package orc.helperClasses;

import org.apache.ignite.Ignition;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.IgniteClient;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static orc.SharedConnections.*;


public class GRACEHashArrayInParts {
    private int buckets;
    private int FILE_SIZE = 16384;
    private LinkedList<String>[] fileBuckets;
    private LinkedList<Tuple>[] records;
    private int recordsLimit = 200;
    private String relation;

    // TODO: Create own Linked List that will autoflush.

    public GRACEHashArrayInParts(int buckets, int recordSize, String relation) {
        this.buckets = buckets;
        this.relation = relation;
        this.fileBuckets = new LinkedList[buckets];
        //TODO: get object size
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
        String fileName = "/join/" + bucket + "/" + this.relation + "/" + this.fileBuckets[bucket].size() + "_" + this.hashCode();
        fileBuckets[bucket].add(fileName);

        jedis.rpush("/join/" + bucket + "/" + this.relation + "/", this.fileBuckets[bucket].size() + "_" + this.hashCode());

        try (IgniteClient client = Ignition.startClient(cfg)) {
            ClientCache<String, LinkedList<Tuple>> cache = client.getOrCreateCache("join");
            cache.put(fileName, records[bucket]);
            records[bucket].clear();
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

    public LinkedList<String>[] getFiles(int bucket) {
        return fileBuckets;
    }



}

