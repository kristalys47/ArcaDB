package orc;

import orc.helperClasses.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.exec.vector.*;
import org.apache.hadoop.hive.serde2.io.HiveDecimalWritable;
import org.apache.orc.*;
import org.apache.orc.impl.RecordReaderImpl;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class ORCManager {
    static final int BATCH_SIZE = 500000;

    //TODO: handle the Batch Size, shards and other stuff related to the limit of the ORC FILE:
    //TODO: Handle booleans with bytes to save space;
    //TODO: parse JSON outside.
    //TODO: Manager uses string of fixed length.

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

    public static boolean reader(String path, String selection, String projections, String result) throws Exception {
        Configuration conf = new Configuration();
        OrcConf.READER_USE_SELECTED.setBoolean(conf, true);

        Reader reader = OrcFile.createReader(new Path(path), OrcFile.readerOptions(conf));

        TypeDescription schema = reader.getSchema();
        ArrayList<String> names = new  ArrayList<String>(schema.getFieldNames());

        ArrayList<String> project = new ArrayList<String>(Arrays.asList(projections.split(",")));

        // Projection
        TypeDescription td = new TypeDescription(TypeDescription.Category.STRUCT);

        ProjectionTree op = new ProjectionTree(schema);
        op.treeBuilder(selection);

        boolean[] finalProjection = new boolean[schema.getChildren().size()+1];
        boolean[] projectionForSelection = new boolean[schema.getChildren().size()+1];
        finalProjection[0] = true;
        projectionForSelection[0] = true;
        for(int i = 0; i< names.size(); i++) {

            if(project.get(0).equals("") ||  project.contains(names.get(i))){
                td.addField(names.get(i), new TypeDescription(schema.getChildren().get(i).getCategory()));
                finalProjection[i+1] = true;
                projectionForSelection[i+1] = true;
            }

            if(op.columns.contains(names.get(i))){
                projectionForSelection[i+1] = true;
            }
        }

        // TODO: create the new Struct for the new writable ORC

        Reader.Options readOptions = reader.options().include(projectionForSelection);
        RecordReaderImpl records = (RecordReaderImpl) reader.rows(readOptions);
        VectorizedRowBatch batch = reader.getSchema().createRowBatch(BATCH_SIZE);

        int t = 0;
        int index;
        while (records.nextBatch(batch)) {
            index = 0;
            Map<String, ColumnVector> row = new HashMap<>();
            VectorizedRowBatch vv = td.createRowBatch(BATCH_SIZE);
            for (int i = 0; i < batch.numCols; i++) {
                row.put(names.get(i), batch.cols[i]);
                if(finalProjection[i+1]){
                    vv.cols[index++] = batch.cols[i];
                }
            }
            //TODO: make parent node count the occurances in the same loop it evaluates if it should be selected.

            if(op.root != null) {
                int[] selected = op.treeEvaluation(row);
                int include = 0;
                for (int i = 0; i < batch.size; i++) {
                    if (selected[i] == 1)
                        include++;
                }
                vv.setFilterContext(true, selected, include);
                for (int i = 0; i < vv.numCols; i++) {
                    vv.cols[i].flatten(true, selected, include);
                }
            } else {
                vv.size = batch.size;
            }

            OrcFile.WriterOptions options = OrcFile.writerOptions(conf).overwrite(true).setSchema(td);
            if(vv.count()>0) {
                //TODO: user folders inteads of files to save intermidiate results
//                Path pathO = new Path(result + "-" + t);
                Path pathO = new Path(result);
                t++;
                Writer writer = OrcFile.createWriter(pathO, options);
                writer.addRowBatch(vv);
                writer.close();
            }
        }
        records.close();
        batch.reset();
        if(t>1){

            List<Path> filesToMerge = new ArrayList<>();
            for (int i = 0; i < t; i++) {
                filesToMerge.add(new Path(result + "-" + i));
            }
            OrcFile.mergeFiles(new Path(result), OrcFile.writerOptions(conf).overwrite(true).setSchema(td), filesToMerge);

        }
        return true;
    }

    public static void writer(String path, String schemaStruct, String values) throws IOException, ParseException {
        Configuration conf = new Configuration();

        TypeDescription schema = TypeDescription.fromString(schemaStruct);
        OrcFile.WriterOptions options = OrcFile.writerOptions(conf).overwrite(true).setSchema(schema);
        Path pathO = new Path(path);
        Writer writer = OrcFile.createWriter(pathO, options);

        ArrayList<TypeDescription> types = new ArrayList<>(schema.getChildren());

        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject) parser.parse(values.replaceAll(" ", ""));
        JSONArray rows = (JSONArray) obj.get("data");

        VectorizedRowBatch batch = schema.createRowBatch(rows.size());

        for(int i = 0; i < types.size(); i++){
            //TODO: The casting of the type is important in this case. (check boolean, decimal, bytes and String)
            switch (Utils.getTypeFromTypeCategory(types.get(i).getCategory())){
                case LONG:
                    LongColumnVector cvl = (LongColumnVector) batch.cols[i];
                    for(int r = 0; r < rows.size(); r++){
                        JSONArray row =(JSONArray) rows.get(r);
                        cvl.vector[r] = (long) row.get(i);
                    }
                    break;
                case BYTES:
                    BytesColumnVector cvb = (BytesColumnVector) batch.cols[i];
                    for(int r = 0; r < rows.size(); r++){
                        JSONArray row =(JSONArray) rows.get(r);
                        cvb.setVal(r, ((String) row.get(i)).getBytes(StandardCharsets.UTF_8), 0, ((String) row.get(i)).getBytes().length);
                    }
                    break;
                case DECIMAL:
                    DecimalColumnVector cvd = (DecimalColumnVector) batch.cols[i];
                    for(int r = 0; r < rows.size(); r++){
                        JSONArray row =(JSONArray) rows.get(r);
                        cvd.vector[r] = new HiveDecimalWritable(row.get(i).toString());
                    }
                    break;
                case DOUBLE:
                    DoubleColumnVector cvD = (DoubleColumnVector) batch.cols[i];
                    for(int r = 0; r < rows.size(); r++){
                        JSONArray row =(JSONArray) rows.get(r);
                        cvD.vector[r] = (double) row.get(i);
                    }
                    break;
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