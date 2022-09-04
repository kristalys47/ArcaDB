package orc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import redis.clients.jedis.Jedis;

import java.util.List;

import static orc.Commons.*;

public class MainWithQueue {
    static public void main(String[] arg) throws Exception {

        Jedis jedisControl = new Jedis(REDIS_HOST, REDIS_PORT);
        Jedis jedisResult = new Jedis(REDIS_HOST_TIMES, REDIS_PORT_TIMES);

        //TODO: need a better logger when this is working
        //TODO: custom port

        while (true) {
            List<String> task = jedisControl.blpop(0, "task");
            System.out.println("Connected - - - - - -");
            long start = System.currentTimeMillis();
            String message = task.get(1);

            System.out.println(message);

            JsonObject gobj = JsonParser.parseString(message).getAsJsonObject();
            JsonArray rows = gobj.getAsJsonArray("plan");
            String[] args = new String[rows.size()];

            for (int i = 0; i < args.length; i++) {
                args[i] = rows.get(i).getAsString();
                System.out.println(args[i]);
            }

            try {
                if (gobj.has("outer") && gobj.has("inner")) {
                    WorkerManager.dbms(args, gobj);
                } else {
                    WorkerManager.dbms(args);
                }
                jedisControl.rpush("done", ip + "\nSuccessful: " + message);
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
                jedisControl.rpush("done", ip + "\nSomething failed in container: " + message + " " + e);
            }
            long end = System.currentTimeMillis();
            jedisResult.rpush("times", "Container " + ip + " " + start + " " + end + " " + (end - start));
        }
    }
}
