import org.apache.orc.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.exec.vector.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.apache.orc.TypeDescription.*;
import static org.apache.orc.TypeDescription.createFloat;


public class ORC {
    static public void main(String[] arg) throws IOException, URISyntaxException {

        Configuration conf = new Configuration();
//        conf.setBoolean(OrcConf.BLOCK_PADDING.getAttribute(), false);

        TypeDescription schema = TypeDescription.fromString("struct<x:int,y:int>");
//        Writer writer = OrcFile.createWriter(new Path(new URI("file:///c:/Users/Abigail/git-data/Container-DBMS/testORC/test.orc")),
////        Writer writer = OrcFile.createWriter(new Path(new URI("file:///tmp/code/Container-DBMS/testORC")),
//                OrcFile.writerOptions(conf).setSchema(schema));
//          100 MBytes
//        VectorizedRowBatch batch2 = new VectorizedRowBatch(2, 10);
//        VectorizedRowBatch batch = schema.createRowBatch();
//
//
//        LongColumnVector x = (LongColumnVector) batch.cols[0];
//        LongColumnVector y = (LongColumnVector) batch.cols[1];
//
//        for(int r=0; r < 10; r++) {
//            System.out.println("\n" +batch.toString());
//            int row = batch.size++;
//            x.vector[row] = r;
//            y.vector[row] = r * 3;
//            // If the batch is full, write it out and start over.
//            if (batch.size == batch.getMaxSize()) {
//                writer.addRowBatch(batch);
//                batch.reset();
//            }
//        }
//        if (batch.size != 0) {
//            writer.addRowBatch(batch);
//            batch.reset();
//        }
//        writer.close();
//
//        System.out.println("\n" +batch.toString());



        Reader reader = OrcFile.createReader(new Path(new URI("file:///c:/Users/Abigail/git-data/Container-DBMS/testORC/test.orc")),
                OrcFile.readerOptions(conf));
        System.out.println(reader.getSchema() + "\n" + reader.getSchema().getFieldNames() + "\n" + reader.getSchema().getChildren());



        System.out.println(reader.getStatistics().toString());
        RecordReader records = reader.rows(reader.options().include(new boolean[]{true, true, false}));

//        String[] colNames = reader.options().getColumnNames();
        VectorizedRowBatch batch = new VectorizedRowBatch(3);
                //reader.getSchema().createRowBatch(12);
        System.out.println(batch.numCols);
        while (records.nextBatch(batch)) {
            System.out.println(batch);
//            for(int r=0; r < batch.size; r++) {
//
//            }
        }
//        records.close();


    }
}
