package coordinator;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import static coordinator.Commons.*;
import static spark.Spark.*;

public class Coordinator {
    private static final Logger logger = LogManager.getLogger(Coordinator.class);
    public static void main(String[] arg) {
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

        port(COORDINATOR_APP_PORT);
        //TODO: Create insert function
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