package orc;

import org.apache.orc.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.exec.vector.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ORC {

    public static void reader(String path, String projections) throws IOException {
        Configuration conf = new Configuration();
        Reader reader = OrcFile.createReader(new Path(path), OrcFile.readerOptions(conf));

        TypeDescription schema = reader.getSchema();
        ArrayList<String> names = new  ArrayList<String>(schema.getFieldNames());
        ArrayList<String> project = new ArrayList<String>(Arrays.asList(projections.split(",")));
        boolean[] finalProjection = new boolean[names.size()+1];
        finalProjection[0] = true;

        int contCol = 0;
        for(int i = 0; i< names.size(); i++) {
            if(project.contains(names.get(i))){
                finalProjection[i+1] = true;
                contCol++;
            } else{
                finalProjection[i+1] = false;
            }
        }

        Reader.Options read = new Reader.Options();
        read.include(finalProjection);
        // read.setRowFilter() <-- can do the projection requires a little bit of study
        RecordReader records = reader.rows(read);

        VectorizedRowBatch batch = reader.getSchema().createRowBatch(20);
        batch.projectionSize = contCol;
        while (records.nextBatch(batch)) {
            System.out.println(batch.toString());
        }
        records.close();
    }

    public static void writer(VectorizedRowBatch a, String path, String schemaStruct, String values) throws IOException, URISyntaxException, ParseException {
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
                LongColumnVector l = (LongColumnVector) batch.cols[i];
                for(int r = 0; r < rows.size(); r++) {
                    JSONArray row =(JSONArray) rows.get(r);
                    l.vector[r] = Integer.valueOf((String) row.get(i));
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