package orc;

import orc.helper.classes.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.exec.vector.BytesColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.LongColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.VectorizedRowBatch;
import org.apache.orc.OrcFile;
import org.apache.orc.Reader;
import org.apache.orc.TypeDescription;
import org.apache.orc.impl.RecordReaderImpl;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        System.out.println(hm.toString());
    }


    //, String pathS, String[] columns
    public static void join(String pathR, String columnR, String pathS, String columnS, String resultPath) throws IOException {


        //TODO: This needs to be fixed
        File directory = new File("/tmp/join/");

        if(!directory.exists()){
            directory.mkdir();
        }
        directory = new File("/tmp/results/");

        if(!directory.exists()){
            directory.mkdir();
        }
        //TODO: what to do if the join is with the same table but different columns
        //TODO: what to do with the fixed bucket size
        GRACEHashArray tableR = orcToMap(pathR, columnR, 100);
        GRACEHashArray tableS = orcToMap(pathS, columnS, 100);


        ExecutorService pool = Executors.newFixedThreadPool(10);
        //TODO: select the one with least size to be the map
        ArrayList<String> files = new ArrayList<>();
        String singleFile = "";
        UUID id = UUID.randomUUID();
        for (int i = 0; i < 100; i++) {
            singleFile = "/tmp/finishedJoin" + i + "_" + id;
            files.add(singleFile);
            Join tmp = new Join(tableR.readRecords(i), tableS.getFileBuckets(i), singleFile);
            pool.execute(tmp);
        }
        pool.shutdown();

        mergeJSONFiles(files, resultPath);


    }

    public static void mergeJSONFiles(ArrayList<String> jsonFiles, String result){
        JSONArray array = new JSONArray();
        FileWriter fr = null;
        JSONParser parser = new JSONParser();

        try{
            for (int i = 0; i < jsonFiles.size(); ++i){
                Object obj = parser.parse(new FileReader(jsonFiles.get(i) + ".json"));
                JSONObject jsonObject = (JSONObject)obj;
                JSONArray jsonArray2 = (JSONArray) jsonObject.get("values");
                array.addAll(jsonArray2);
            }

            JSONObject obj = new JSONObject();
            obj.put("values", array);
            fr = new FileWriter(result);
            fr.write(obj.toJSONString());
            fr.flush();
            fr.close();

        } catch (Exception e) {
            e.printStackTrace();

        }
    }
    public static GRACEHashArray orcToMap(String path, String column, int buckets) throws IOException {
        Configuration conf = new Configuration();

        Reader reader = OrcFile.createReader(new Path(path), OrcFile.readerOptions(conf));
//        TypeDescription schema = reader.getSchema();
        RecordReaderImpl records = (RecordReaderImpl) reader.rows(reader.options());
        VectorizedRowBatch batch = reader.getSchema().createRowBatch();
        int joinKey = reader.getSchema().getFieldNames().indexOf(column);
        // TODO: size needs to be calculated, make a formula for that.
        GRACEHashArray table = new GRACEHashArray(buckets, 100);

        //TODO: Generate this key
        //TODO: change string
        AES hashing = new AES("helohelohelohelo");
        while (records.nextBatch(batch)) {
            for(int r=0; r < batch.size; ++r) {
                StringBuilder temp = new StringBuilder();
                StringBuilder key = new StringBuilder();
                for (int j = 0; j < batch.cols.length; j++) {
                    if(j == joinKey){
                        batch.cols[j].stringifyValue(key, r);
                        batch.cols[j].stringifyValue(temp, r);
                    } else {
                        batch.cols[j].stringifyValue(temp, r);
                    }
                    if(j+1 != batch.cols.length){
                        temp.append(",");
                    }
                }
                // TODO: create better hashfunction
                String removeMark = temp.toString();
                String keygroup = key.toString();
                table.addRecord(hashFunction(keygroup, hashing), removeMark);
            }
        }
        records.close();
        batch.reset();
        table.flushRemainders();

        return table;
    }

    public static long hashFunction(String s, AES hashing){
        long result = hashing.encrypt(s);
        return result;
    }

}

