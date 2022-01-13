package orc;

import alluxio.AlluxioURI;
import alluxio.client.file.FileInStream;
import alluxio.client.file.FileOutStream;
import alluxio.client.file.FileSystem;
import alluxio.conf.InstancedConfiguration;
import alluxio.conf.PropertyKey;
import alluxio.exception.AlluxioException;
import com.google.protobuf.ByteString;
import org.apache.orc.TypeDescription;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.lang.model.element.Name;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Math;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import static java.lang.Math.floor;
import static java.lang.Math.random;


public class main {
    //insert /JavaCode/testORC struct<id:int,name:string,last:string,score:decimal,isFemale:boolean> {\"values\":[[\"Kristal\",\"3\"],[\"al\",\"4\"],[\"bob\",\"17\"],[\"Bi\",\"34\"],[\"col\",\"6\"],[\"Jil\",\"4\"],[\"sam\",\"3\"],[\"Dead\",\"0\"]]}
    //read /JavaCode/testORC name (((name="Kristal")|(val<-10))&(val>0))
    static public void main(String[] arg) throws IOException, AlluxioException {
        InstancedConfiguration conf = InstancedConfiguration.defaults();
        conf.set(PropertyKey.MASTER_HOSTNAME, "localhost");
        conf.set(PropertyKey.MASTER_WEB_PORT, 19999);
        FileSystem fs = FileSystem.Factory.create(conf);
        System.out.println(fs);
        AlluxioURI path = new AlluxioURI("/Hello");
// Create a file and get its output stream
        FileOutStream out = fs.createFile(path);
// Write data
        out.write(19);
// Close and complete file
        out.close();


    }




    void dbms(String[] arg)  throws Exception {
        String test = "((name=\"qftrjyivexdeikecdhbf\")|(id<3))";
        String projection = "last,isFemale";


        if(arg[0].equals("insert")) {
            String data = generateData();
            String schema = "struct<id:int,name:string,last:string,score:float,isFemale:int>";

            ORCManager.writer("/JavaCode/rewriting", schema, data);
            ORCManager.readerPrint("/JavaCode/rewriting");
        }
        else{
            ORCManager.reader("/JavaCode/rewriting", projection, test);
            ORCManager.readerPrint("/JavaCode/results0");
        }

    }

    static public String generateData(){
        byte[][] values = new byte[20000][20];
        byte[][] value2 = new byte[20000][20];
        String abecedario = "abcdefghijklmnopqrstuvwxyz";
        String[] names = new String[20000];
        String[] lastname = new String[20000];
        for (int i = 0; i < values.length; i++) {
            names[i] = "";
            lastname[i] = "";
            for (int j = 0; j < 20; j++) {
                names[i] += abecedario.charAt(new Random().nextInt(abecedario.length()));
                lastname[i] += abecedario.charAt(new Random().nextInt(abecedario.length()));
            }

        }
        JSONObject obj = new JSONObject();
        JSONArray array = new JSONArray();
        for (int i = 0; i < 20000; i++) {
            JSONArray instance = new JSONArray();
            instance.add(0, i);
            instance.add(1, names[i]);
            instance.add(2, lastname[i]);
            instance.add(3, Math.random()*2000);
            instance.add(4, Math.random()*2000%256 > 128? 1: 0);
            array.add(instance);
        }

        obj.put("values", array);

        return obj.toJSONString();
    }


}
