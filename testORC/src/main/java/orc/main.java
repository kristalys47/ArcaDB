package orc;

import org.apache.orc.TypeDescription;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.lang.model.element.Name;
import java.lang.Math;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import static java.lang.Math.floor;
import static java.lang.Math.random;


public class main {
    //insert /JavaCode/testORC struct<id:int,name:string,last:string,score:decimal,isFemale:boolean> {\"values\":[[\"Kristal\",\"3\"],[\"al\",\"4\"],[\"bob\",\"17\"],[\"Bi\",\"34\"],[\"col\",\"6\"],[\"Jil\",\"4\"],[\"sam\",\"3\"],[\"Dead\",\"0\"]]}
    //read /JavaCode/testORC name (((name="Kristal")|(val<-10))&(val>0))
    static public void main(String[] arg) throws Exception {

//        String test = "(((name=\"Kristal\")|((name=\"b\")|(val<10)))&(val>0))";
//        String test = "(((name=\"Kristal\")|(val<-10))&(val>0))";
//        String test = "(((name=\"Kristal\")|(val<10))&(val>0))";
        String test = "(name=\"al\")";

//        String test = "(val<11)";
//        String test = "(((val<10)|(val<-10))&(val>0))";

        String projection = "name,val";

        String mmm = generateData();
        String schema = "struct<id:int,name:string,last:string,score:float,isFemale:int>";

//        TypeDescription lll = new TypeDescription(TypeDescription.Category.STRUCT);
//        lll.addField("id", new TypeDescription(TypeDescription.Category.INT));
//        lll.addField("sdf", new TypeDescription(TypeDescription.Category.STRING));
//        lll.addField("asdv", new TypeDescription(TypeDescription.Category.DECIMAL));
//        lll.addField("assdv", new TypeDescription(TypeDescription.Category.BOOLEAN));
//        lll.addField("assdsv", new TypeDescription(TypeDescription.Category.DOUBLE));
//        System.out.printf(lll.toString());

        if(arg[0].equals("insert")) {

            ORCManager.writer("/JavaCode/rewriting", schema, mmm);
            ORCManager.readerPrint("/JavaCode/rewriting");
        }
        else{
            for (int i = 0; i<arg.length; i++){
                System.out.println(arg[i]);
            }

//            ORCManager.readerExplicit(arg[1], arg[2]);
            ORCManager.reader(arg[1], arg[2], test);
            ORCManager.readerPrint("/JavaCode/results0");
        }

    }


    static public String generateData(){
        byte[][] values = new byte[1000][20];
        byte[][] value2 = new byte[1000][20];
        String abecedario = "abcdefghijklmnopqrstuvwxyz";
        String[] names = new String[1000];
        String[] lastname = new String[1000];
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
        for (int i = 0; i < 500; i++) {
            JSONArray instance = new JSONArray();
            instance.add(0, i);
            instance.add(1, names[i]);
            instance.add(2, lastname[i]);
            instance.add(3, Math.random()*2000);
            instance.add(4, Math.random()*2000%256 > 128? 1: 0);
            array.add(instance);
        }

        obj.put("values", array);
//
//        System.out.printf(String.valueOf(obj));

        return obj.toJSONString();
    }


}
