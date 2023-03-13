package orc.helper.classes;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class TestingUtils {

    // "struct<id:int,name:string,last:string,score:float,isFemale:int>"
    public static void generateDataA(String file){
        int numRows = 200;
        String abecedario = "abcdefghijklmnopqrstuvwxyz";
        String[] names = new String[numRows];
        String[] lastname = new String[numRows];
        for (int i = 0; i < numRows; i++) {
            names[i] = "";
            lastname[i] = "";
            for (int j = 0; j < 3; j++) {
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
        FileWriter fr;
        try {
            // Constructs a FileWriter given a file name, using the platform's default charset
            fr = new FileWriter(file);
            fr.write(obj.toJSONString());
            fr.flush();
            fr.close();

        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    //schema: "struct<id:int,name:string,last:string,score:float,isFemale:int,fk:int>"
    public static void generateDataB(String file){
        int numRows = 20000;
        String abecedario = "abcdefghijklmnopqrstuvwxyz";
        String[] names = new String[numRows];
        String[] lastname = new String[numRows];
        for (int i = 0; i < numRows; i++) {
            names[i] = "";
            lastname[i] = "";
            for (int j = 0; j < 5; j++) {
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
            instance.add(5, Math.floorMod((int) (Math.random()*2000), 160));
            array.add(instance);
        }

        obj.put("values", array);
        FileWriter fr;
        try {
            // Constructs a FileWriter given a file name, using the platform's default charset
            fr = new FileWriter(file);
            fr.write(obj.toJSONString());
            fr.flush();
            fr.close();

        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}
