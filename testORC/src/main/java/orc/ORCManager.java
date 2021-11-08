package orc;

import org.apache.orc.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.exec.vector.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

import org.apache.orc.impl.RecordReaderImpl;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.apache.hadoop.hive.ql.exec.vector.VectorizedRowBatch;

import static orc.Utils.getTypeFromTypeCategory;


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

        ProjectionTree op = new ProjectionTree(schema);
        op.treeBuilder(selection);

        boolean[] finalProjection = new boolean[schema.getChildren().size()+1];
        boolean[] projectionForSelection = new boolean[schema.getChildren().size()+1];
        finalProjection[0] = true;
        projectionForSelection[0] = true;
        for(int i = 0; i< names.size(); i++) {
            if(project.contains(names.get(i))){
                td.addField(names.get(i), new TypeDescription(schema.getChildren().get(i).getCategory()));
                finalProjection[i+1] = true;
                projectionForSelection[i+1] = true;
            }

            if(op.columns.contains(names.get(i))){
                projectionForSelection[i+1] = true;
            }
        }

        System.out.printf(td.toString());




        // TODO: create the new Struct for the new writable ORC

        Reader.Options readOptions = reader.options().include(projectionForSelection);
        RecordReaderImpl records = (RecordReaderImpl) reader.rows(readOptions);
        VectorizedRowBatch batch = reader.getSchema().createRowBatch(15);


        int t = 0;
        int index;
        while (records.nextBatch(batch)) {
            index = 0;
            Map<String, ColumnVector> row = new HashMap<>();
            VectorizedRowBatch vv = td.createRowBatch(15);
            for (int i = 0; i < batch.numCols; i++) {
                row.put(names.get(i), batch.cols[i]);
                if(finalProjection[i+1]){
                    vv.cols[index++] = batch.cols[i];
                }
            }
            int[] selected = op.treeEvaluation(row);
            int include = 0;
            //TODO: this iterations can be cut short if you already have the number of qualifying rows. Implement that in the root node of the tree using another method.
            for (int i = 0; i < vv.numCols; i++) {
                int k = 0;
                switch (getTypeFromTypeCategory(td.getChildren().get(i).getCategory())){
                    case LONG:
                        LongColumnVector cvl = (LongColumnVector) vv.cols[i];
                        for (int j = 0; j < cvl.vector.length; j++) {
                            if(selected[j] == 1){
                                cvl.vector[k++] = cvl.vector[j];
                                if(i == 0) {
                                    include++;
                                }
                            }
                        }
                        break;
                    case BYTES:
                        BytesColumnVector cvb = (BytesColumnVector) vv.cols[i];
                        for (int j = 0; j < cvb.vector.length; j++) {
                            if(selected[j] == 1){
                                cvb.vector[k++] = cvb.vector[j];
                                if(i == 0) {
                                    include++;
                                }
                            }
                        }
                        break;
                    case DECIMAL:
                        DecimalColumnVector cvd = (DecimalColumnVector) vv.cols[i];
                        for (int j = 0; j < cvd.vector.length; j++) {
                            if(selected[j] == 1){
                                cvd.vector[k++] = cvd.vector[j];
                                if(i == 0) {
                                    include++;
                                }
                            }
                        }
                        break;
                    case DOUBLE:
                        DoubleColumnVector cvD = (DoubleColumnVector) vv.cols[i];
                        for (int j = 0; j < cvD.vector.length; j++) {
                            if(selected[j] == 1){
                                cvD.vector[k++] = cvD.vector[j];
                                if(i == 0) {
                                    include++;
                                }
                            }
                        }
                        break;
                }


            }
            vv.size = include;
            OrcFile.WriterOptions options = OrcFile.writerOptions(conf).overwrite(true).setSchema(td);
            Path pathO = new Path("/JavaCode/results" + t);
            t++;
            System.out.println(vv.count());
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