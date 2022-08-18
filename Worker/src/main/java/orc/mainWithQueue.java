package orc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static orc.Commons.*;

public class mainWithQueue {
    static public void main(String[] arg) throws Exception {


        String ip = InetAddress.getLocalHost().getHostAddress();
        Jedis jedisControl = new Jedis(REDIS_HOST, REDIS_PORT);
        Jedis jedisResult = new Jedis(REDIS_HOST_TIMES, REDIS_PORT_TIMES);

        //TODO: need a better logger when this is working
        //TODO: custom port

        System.out.println("Connected - - - - - -");
        String message = jedisControl.blpop(0, "task");

        System.out.println(message);

        JsonObject gobj = JsonParser.parseString(message).getAsJsonObject();
        JsonArray rows = gobj.getAsJsonArray("plan");
        String[] args = new String[rows.size()];

        for (int i = 0; i < args.length; i++) {
            args[i] = rows.get(i).getAsString();
            System.out.println(args[i]);
        }

        try{
            if(gobj.has("outer") && gobj.has("inner")){
                WorkerManager.dbms(args, gobj);
            } else {
                WorkerManager.dbms(args);
            }
        }catch (Exception e){
            System.out.println(e);
            e.printStackTrace();
            out.println("Something failed: " + e);
        } finally {
            out.println("Successful");
        }
        long end = System.currentTimeMillis();
        jedisResult.rpush("times", "Container " + ip + " " + start + " " + end + " " + (end-start));

        out.close();
        in.close();
        client.close();
    }
}
