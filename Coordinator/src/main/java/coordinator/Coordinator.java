package coordinator;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.awt.print.Printable;

import static coordinator.Utils.Commons.*;
import static spark.Spark.*;

public class Coordinator {
    private static final Logger logger = LogManager.getLogger(Coordinator.class);
    public static void main(String[] arg) {
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

        port(COORDINATOR_APP_PORT);
        //TODO: Create insert function
        System.out.println("----------------------------");
        System.out.println("ArcaDB is open for Business!");
        System.out.println("----------------------------");
        get("/database/query", (request, response) -> {
            long start = System.currentTimeMillis();
            JSONObject json  = new JSONObject(request.body());
//            JsonObject results = new JsonObject();
//            System.out.println(json.get("query"));
            boolean result = false;
            try {
                result = Controller.handleRequest((String) json.get("query"), (Integer) json.get("mode"), (Integer) json.get("buckets"));
            } catch (Exception e){
                e.printStackTrace();
                return e.getMessage();
            }
            long end = System.currentTimeMillis();
            System.out.println("TIME_LOG: RESPONSE TIME " + start + " " + end + " " + (end - start));
            return result? "Done": "Failed";

        });
    }
}