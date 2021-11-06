package orc;

import org.apache.orc.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.exec.vector.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.apache.orc.impl.RecordReaderImpl;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.apache.hadoop.hive.ql.exec.vector.VectorizedRowBatch;



public class ORCManager {
    static final int BATCH_SIZE = 100;

    //TODO: hacer matching de los types del schema al type del batch y crear un diccionario a base de esto por consiguiente va a provocar que tengas que cambiar tanto el tree builder como wl writer y el reader.

    public static void readerPrint(String path)throws IOException {
        Configuration conf = new Configuration();
        Reader reader = OrcFile.createReader(new Path(path), OrcFile.readerOptions(conf));

        RecordReader records = reader.rows();
        VectorizedRowBatch batch = reader.getSchema().createRowBatch();

        while (records.nextBatch(batch)) {
            System.out.println(batch.toString());
        }
        records.close();
    }

    public static void reader(String path, String projections, String selection) throws Exception {
        Configuration conf = new Configuration();
        OrcConf.READER_USE_SELECTED.setBoolean(conf, true);
        Reader reader = OrcFile.createReader(new Path(path), OrcFile.readerOptions(conf));

        TypeDescription schema = reader.getSchema();
        ArrayList<String> names = new  ArrayList<String>(schema.getFieldNames());
        ArrayList<String> project = new ArrayList<String>(Arrays.asList(projections.split(",")));

        // Projection
        TypeDescription td = new TypeDescription(TypeDescription.Category.STRUCT);
        boolean[] finalProjection = new boolean[names.size()+1];
        finalProjection[0] = true;
        int countCol = 0;
        for(int i = 0; i< names.size(); i++) {
            if(project.contains(names.get(i))){
                td.addField(names.get(i), new TypeDescription(schema.getChildren().get(i).getCategory()));
                finalProjection[i+1] = true;
                countCol++;
            } else{
                finalProjection[i+1] = false;
            }
        }

        System.out.printf(td.toString());

        ProjectionTree op = new ProjectionTree(schema);
        op.treeBuilder(selection);


        // TODO: create the new Struct for the new writable ORC

        Reader.Options readOptions = reader.options().include(finalProjection);
        RecordReaderImpl records = (RecordReaderImpl) reader.rows(readOptions);
        VectorizedRowBatch batch = reader.getSchema().createRowBatchV2();


        int t = 0;
        int index;
        while (records.nextBatch(batch)) {
            index = 0;
            Map<String, ColumnVector> row = new HashMap<>();
            for (int i = 0; i < batch.numCols; i++) {
                row.put(names.get(i), batch.cols[i]);
            }
            int[] selected = op.treeEvaluation(row);
            VectorizedRowBatch vv = new VectorizedRowBatch(countCol);
//            OrcConf.INCLUDE_COLUMNS.setInt();
            vv.setFilterContext(true, selected, 5);
            vv.cols = batch.cols;
            vv.projectedColumns = batch.projectedColumns;
//            vv.
//            vv.getPartitionColumnCount() =
            OrcFile.WriterOptions options = OrcFile.writerOptions(conf).overwrite(true).setSchema(td);
            Path pathO = new Path("/JavaCode/results" + t);
            t++;
            Writer writer = OrcFile.createWriter(pathO, options);
            writer.addRowBatch(vv);
            writer.close();
        }
        records.close();
        batch.reset();
    }

    public static void writer(String path, String schemaStruct, String values) throws IOException, URISyntaxException, ParseException {
        Configuration conf = new Configuration();

        TypeDescription schema = TypeDescription.fromString(schemaStruct);
        OrcFile.WriterOptions options = OrcFile.writerOptions(conf).overwrite(true).setSchema(schema);
        Path pathO = new Path(path);
        Writer writer = OrcFile.createWriter(pathO, options);

        ArrayList<TypeDescription> types = new  ArrayList<TypeDescription>(schema.getChildren());

        VectorizedRowBatch batch = schema.createRowBatch(20);

        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject) parser.parse(values.replaceAll(" ", ""));
        JSONArray rows = (JSONArray) obj.get("values");

//  addRow(row, types, batch, r);

        for(int i = 0; i < types.size(); i++){
            if(types.get(i).compareTo(new TypeDescription(TypeDescription.Category.STRING)) == 0){
                BytesColumnVector v = (BytesColumnVector) batch.cols[i];
                for(int r = 0; r < rows.size(); r++){
                    JSONArray row =(JSONArray) rows.get(r);
                    v.setVal(r, ((String) row.get(i)).getBytes(StandardCharsets.UTF_8), 0, ((String) row.get(i)).getBytes().length);
                }
            } else if (types.get(i).compareTo(new TypeDescription(TypeDescription.Category.INT)) == 0){
                BytesColumnVector l = (BytesColumnVector) batch.cols[i];
                for(int r = 0; r < rows.size(); r++) {
                    JSONArray row =(JSONArray) rows.get(r);
//                    v.setVal = Integer.valueOf((String) row.get(i));
                }
            }
        }

        batch.size = rows.size();
        writer.addRowBatch(batch);

        batch.reset();
        writer.close();
        System.out.println("It has been successfully saved in path " + path);

    }

    private static void addRow(JSONArray row, ArrayList<TypeDescription> types, VectorizedRowBatch batch, int place) {
        for(int i = 0; i < row.size(); i++){
            switch (types.get(i).getId()){
                case 7: //TypeDescription.Category.STRING.ordinal()
                    BytesColumnVector v = (BytesColumnVector) batch.cols[i];
                    v.vector[place] = ((String)row.get(i)).getBytes(StandardCharsets.UTF_8);
                    break;
                case 3: //TypeDescription.Category.INT.ordinal()
                    LongColumnVector l = (LongColumnVector) batch.cols[i];
                    l.vector[place] = Integer.valueOf((String) row.get(i));
                    break;
            }
        }
    }
}