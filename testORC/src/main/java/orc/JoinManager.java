package orc;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.exec.vector.BytesColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.LongColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.VectorizedRowBatch;
import org.apache.orc.*;
import org.apache.orc.impl.RecordReaderImpl;

import java.io.IOException;
import java.util.*;
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
    public static void join(String pathR, String columnR, String pathS, String columnS) throws IOException {

        //TODO: what to do if the join is with the same table but different columns
        GRACEHashArray tableR = orcToMap(pathR, columnR);
        GRACEHashArray tableS = orcToMap(pathS, columnS);

        ExecutorService pool = Executors.newFixedThreadPool(10);
        //TODO: select the one with least size to be the map
        for (int i = 0; i < 40; i++) {
            Join tmp = new Join(tableR.readRecords(i), tableS.getFileBuckets(i));
            pool.execute(tmp);
        }

        pool.shutdown();

    }
    public static GRACEHashArray orcToMap(String path, String column) throws IOException {
        Configuration conf = new Configuration();

        Reader reader = OrcFile.createReader(new Path(path), OrcFile.readerOptions(conf));
//        TypeDescription schema = reader.getSchema();
        RecordReaderImpl records = (RecordReaderImpl) reader.rows(reader.options());
        VectorizedRowBatch batch = reader.getSchema().createRowBatch();
        int joinKey = reader.getSchema().getFieldNames().indexOf(column);
        // TODO: size needs to be calculated, make a formula for that.
        GRACEHashArray table = new GRACEHashArray(40, 100);

        Pattern pattern = Pattern.compile(":::(.*):::");

        while (records.nextBatch(batch)) {
            for(int r=0; r < batch.size; ++r) {
                StringBuilder temp = new StringBuilder();
                for (int j = 0; j < batch.cols.length; j++) {
                    if(j == joinKey){
                        temp.append(":::");
                        batch.cols[j].stringifyValue(temp, r);
                        temp.append(":::");
                    } else {
                        batch.cols[j].stringifyValue(temp, r);
                    }
                    if(j+1 != batch.cols.length){
                        temp.append(",");
                    }
                }
                // TODO: create better hashfunction
                Matcher matcher = pattern.matcher(temp.toString());
                matcher.find();
                String removeMark = temp.toString().replace(":::", "");
                String keygroup = matcher.group(1);
                table.addRecord(hashFunction(keygroup), removeMark);
            }
        }
        records.close();
        batch.reset();
        table.flushRemainders();
//        table.readRecords(0);

        return table;
    }

    public static int hashFunction(String s){
        int hash = s.charAt(s.length()-1);
        for (int i = 0; i < s.length(); i++) {
            hash = hash*2 + s.charAt(i);
        }
        return hash;
    }

}

