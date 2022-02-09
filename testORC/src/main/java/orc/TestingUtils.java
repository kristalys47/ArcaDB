package orc;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class TestingUtils {
    static public void generateData(FileWriter file){
        int numRows = 300;
        String abecedario = "abcdefghijklmnopqrstuvwxyz";
        String[] names = new String[numRows];
        String[] lastname = new String[numRows];
        for (int i = 0; i < numRows; i++) {
            names[i] = "";
            lastname[i] = "";
            for (int j = 0; j < 15; j++) {
                names[i] += abecedario.charAt(new Random().nextInt(abecedario.length()));
                lastname[i] += abecedario.charAt(new Random().nextInt(abecedario.length()));
            }

        }
        JSONObject obj = new JSONObject();
        JSONArray array = new JSONArray();
        for (int i = 0; i < numRows; i++) {
            JSONArray instance = new JSONArray();
            instance.add(0, i);
            instance.add(1, names[i]);
            instance.add(2, lastname[i]);
            instance.add(3, Math.random()*2000);
            instance.add(4, Math.random()*2000%256 > 128? 1: 0);
            array.add(instance);
        }

        obj.put("values", array);

        try {
            // Constructs a FileWriter given a file name, using the platform's default charset
            file = new FileWriter("/JavaCode/rewriting31.json");
            file.write(obj.toJSONString());

        } catch (IOException e) {
            e.printStackTrace();

        } finally {

            try {
                file.flush();
                file.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
