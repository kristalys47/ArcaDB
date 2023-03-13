package orc;

import org.json.JSONObject;
import redis.clients.jedis.Jedis;

import java.util.List;

import static orc.Commons.*;

public class main {
    static public void main(String[] arg) throws Exception {
      S3_BUCKET = System.getProperty("S3_BUCKET");
      AWS_S3_ACCESS_KEY = System.getProperty("AWS_S3_ACCESS_KEY");
      AWS_S3_SECRET_KEY = System.getProperty("AWS_S3_SECRET_KEY");
      REDIS_HOST = System.getProperty("REDIS_HOST");
      REDIS_PORT = Integer.parseInt(System.getProperty("REDIS_PORT"));
      REDIS_HOST_TIMES = System.getProperty("REDIS_HOST_TIMES");
      REDIS_PORT_TIMES = Integer.parseInt(System.getProperty("REDIS_PORT_TIMES"));
      WORKER_APP_PORT = Integer.parseInt(System.getProperty("WORKER_APP_PORT"));
      COORDINATOR_APP_PORT = Integer.parseInt(System.getProperty("COORDINATOR_APP_PORT"));

      POSTGRES_PASSWORD = System.getProperty("POSTGRES_PASSWORD");
      POSTGRES_USERNAME = System.getProperty("POSTGRES_USERNAME");
      POSTGRES_HOST = System.getProperty("POSTGRES_HOST");
      POSTGRES_PORT = Integer.parseInt(System.getProperty("POSTGRES_PORT"));
      POSTGRES_DB_NAME = System.getProperty("POSTGRES_DB_NAME");
      MODE = System.getProperty("MODE");

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
