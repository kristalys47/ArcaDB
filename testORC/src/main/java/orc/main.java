package orc;

import com.google.protobuf.ByteString;
import org.apache.log4j.BasicConfigurator;
import org.apache.orc.TypeDescription;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.lang.model.element.Name;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Math;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import com.lambdaworks.redis.*;
import org.json.simple.parser.JSONParser;
import redis.clients.jedis.JedisPooled;

import static java.lang.Math.floor;
import static java.lang.Math.random;


public class main {
    private static FileWriter file;
    //insert /JavaCode/testORC struct<id:int,name:string,last:string,score:decimal,isFemale:boolean> {\"values\":[[\"Kristal\",\"3\"],[\"al\",\"4\"],[\"bob\",\"17\"],[\"Bi\",\"34\"],[\"col\",\"6\"],[\"Jil\",\"4\"],[\"sam\",\"3\"],[\"Dead\",\"0\"]]}
    //read /JavaCode/testORC name (((name="Kristal")|(val<-10))&(val>0))
    static public void main(String[] arg) throws Exception {

//        String host = "127.0.0.1";
//        //Using lettuce for this since it supports async calls...
//        RedisClient redisClient = new RedisClient(
//                RedisURI.create("redis://" + host + ":6379"));
//        RedisConnection<String, String> connection = redisClient.connect();
//
//        System.out.println("Connected to Redis");
//        connection.set("mmmmm", "kristal");
//        String value = connection.get("mmmmm");
//        System.out.println(value);
//
//        connection.close();
//        redisClient.shutdown();
//
//        StatefulRedisConnection<String, String> connection = client.connect();
//        RedisStringAsyncCommands<String, String> async = connection.async();
//        RedisFuture<String> set = async.set("key", "value")
//        RedisFuture<String> get = async.get("key")
//
//        async.awaitAll(set, get) == true
//
//        set.get() == "OK"
//        get.get() == "value"
//
////        JedisPooled jedis = new JedisPooled("localhost", 6379);
//        String[] args = {"inserts"};
//        generateData();

        ORCManager.readerPrint("/JavaCode/insertedTest");
        ORCManager.readerPrint("/JavaCode/results0");

//        dbms(args);
//

    }

    static void dbms(String[] arg)  throws Exception {
        String test = "((name=\"qftrjyivexdeikecdhbf\")|(id<3))";
        String projection = "last,isFemale";

        String data = "";
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader("/JavaCode/rewriting300.json"));
            JSONObject jsonObject = (JSONObject)obj;
            data = jsonObject.toJSONString();
        } catch(Exception e) {
            e.printStackTrace();
        }


        if(arg[0].equals("insert")) {
            String schema = "struct<id:int,name:string,last:string,score:float,isFemale:int>";

            ORCManager.writer("/JavaCode/insertedTest", schema, data);
//            ORCManager.readerPrint("/JavaCode/rewriting");
        }
        else if(arg[0].equals("insertRead")) {
            String schema = "struct<id:int,name:string,last:string,score:float,isFemale:int>";

            ORCManager.writer("/JavaCode/rewriting", schema, data);
//            ORCManager.readerPrint("/JavaCode/rewriting");
        }
        else{
            ORCManager.reader("/JavaCode/insertedTest", projection, test);
            ORCManager.readerPrint("/JavaCode/results0");
        }

    }

    static public void generateData(){
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
            file = new FileWriter("/JavaCode/rewriting300.json");
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
