package orc.helperClasses;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GRACEHashArray {
    private int buckets;
    private int FILE_SIZE = 4096;
    private LinkedList<File>[] fileBuckets;
    private LinkedList<String>[] records;
    private int recordsLimit;

    // TODO: Create own Linked List that will autoflush.

    public GRACEHashArray(int buckets, int recordSize) {
        this.buckets = buckets;
        this.fileBuckets = new LinkedList[buckets];
        this.recordsLimit = Math.floorDiv(FILE_SIZE, recordSize);
        this.records = new LinkedList[buckets];

        for (int i = 0; i < buckets; i++) {
            fileBuckets[i] = new LinkedList<>();
            records[i] = new LinkedList<>();
        }
    }


    public void addRecord(long key, String record) throws FileNotFoundException {
        int hashValue = (int) Math.abs(key % buckets);
        records[hashValue].add(key + "," + record);
        checkFileFull(hashValue);
    }

    public void checkFileFull(int bucket) throws FileNotFoundException {
        if(records[bucket].size() >= recordsLimit){
            flushToFile(bucket);
        }
    }

    private void flushToFile(int bucket) {
        String fileName = "/nfs/tmp/join/temp_" + bucket + "_" + fileBuckets[bucket].size() + "_" + UUID.randomUUID();
        File tempFile = new File(fileName);
        fileBuckets[bucket].add(tempFile);

        try(FileOutputStream fos = new FileOutputStream(tempFile);) {
            BufferedOutputStream writer = new BufferedOutputStream(fos);

            //TODO: Implement that the writer is saved and flush as objects are added to avoid the usage of this loop.
            while(records[bucket].size()>0) {
                String record = (String) records[bucket].removeFirst() + "\n";
                writer.write(record.getBytes(StandardCharsets.UTF_8));
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            //TODO: handle error;
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

    public LinkedList<File> getFileBuckets(int bucket) {
        return fileBuckets[bucket];
    }

}

