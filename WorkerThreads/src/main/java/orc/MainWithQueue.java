package orc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.lettuce.core.KeyValue;
import io.lettuce.core.api.sync.RedisCommands;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static orc.Commons.*;

public class MainWithQueue implements Runnable{

    @Override
    public void run() {
        //TODO: need a better logger when this is working
        //TODO: custom port
        RedisCommands<String, String> jedisControl;
        while (true) {
            jedisControl = newJedisConnection();
            KeyValue<String,String> task = jedisControl.blpop(0, "task");
            System.out.println("Connected - - - - - -");
            long start = System.currentTimeMillis();
            String message = task.getValue();

            System.out.println(message);

            JsonObject gobj = JsonParser.parseString(message).getAsJsonObject();
            JsonArray rows = gobj.getAsJsonArray("plan");
            String[] args = new String[rows.size()];
            for (int i = 0; i < args.length; i++) {
                args[i] = rows.get(i).getAsString();
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
//            Jedis jedisResult = new Jedis(REDIS_HOST_TIMES, REDIS_PORT_TIMES);
//            jedisResult.rpush("times", "Container " + ip + " " + start + " " + end + " " + (end - start));
//            jedisResult.close();
//            if (args[0].contains("joinPartition")) {
            System.out.println(end + " " + "TIME_LOG: Container " + ip + Thread.currentThread().getId() + " " + start + " " + end + " " + (end - start));
//            }else{
//                System.out.println("TIME_LOG2: Container " + ip + " " + start + " " + end + " " + (end - start));
//            }
            //Let see what happens
            System.gc();
        }
    }
}
