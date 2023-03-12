package orc;

import org.json.JSONObject;
import redis.clients.jedis.Jedis;

import java.util.List;

import static orc.Commons.*;

public class main {
    static public void main(String[] arg) throws Exception {
        S3_BUCKET = arg[0].trim();
        AWS_S3_ACCESS_KEY = arg[1].trim();
        AWS_S3_SECRET_KEY = arg[2].trim();
        REDIS_HOST = arg[3].trim();
        REDIS_PORT = Integer.parseInt(arg[4].trim());
        REDIS_HOST_TIMES = arg[5].trim();
        REDIS_PORT_TIMES = Integer.parseInt(arg[6].trim());
        WORKER_APP_PORT = Integer.parseInt(arg[7].trim());
        COORDINATOR_APP_PORT = Integer.parseInt(arg[8].trim());

        POSTGRES_PASSWORD = arg[9].trim();
        POSTGRES_USERNAME = arg[10].trim();
        POSTGRES_HOST = arg[11].trim();
        POSTGRES_PORT = Integer.parseInt(arg[12].trim());
        POSTGRES_DB_NAME = arg[13].trim();
        MODE = arg[14].trim();

        POSTGRES_JDBC = "jdbc:postgresql://" + POSTGRES_HOST + ":" + POSTGRES_PORT + "/" + POSTGRES_DB_NAME;

        System.out.println("Started");

        Jedis jedisControl;
        while (true) {
            jedisControl = newJedisConnection(REDIS_HOST, REDIS_PORT);
            List<String> task = jedisControl.blpop(0, "structured");
            jedisControl.close();
            System.out.println("Connected - - - - - -");
            long start = System.currentTimeMillis();
            String message = task.get(1);
            System.out.println(message);

            JSONObject plan = new JSONObject(message);
            try {
                WorkerManager.dbms(plan);
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
                jedisControl.rpush("done", ip + "\nSomething failed in container: " + message + " " + e);
            }
            long end = System.currentTimeMillis();
            jedisControl.close();
            System.out.println("TIME_LOG: Container " + ip + " " + start + " " + end + " " + (end - start));
            System.gc();
        }
    }
}
