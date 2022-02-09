package orc;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.exec.vector.BytesColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.ColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.LongColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.VectorizedRowBatch;
import org.apache.orc.*;
import org.apache.orc.impl.RecordReaderImpl;

import java.io.IOException;
import java.util.*;

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
}
