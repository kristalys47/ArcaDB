package coordinator;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import static spark.Spark.*;

public class Coordinator {
    private static final Logger logger = LogManager.getLogger(Coordinator.class);
    public static void main(String[] arg) {
        Commons.S3_BUCKET = arg[0].trim();
        Commons.AWS_S3_ACCESS_KEY = arg[1].trim();
        Commons.AWS_S3_SECRET_KEY = arg[2].trim();
        Commons.REDIS_HOST = arg[3].trim();
        Commons.REDIS_PORT = Integer.parseInt(arg[4].trim());
        Commons.REDIS_HOST_TIMES = arg[5].trim();
        Commons.REDIS_PORT_TIMES = Integer.parseInt(arg[6].trim());
        Commons.WORKER_APP_PORT = Integer.parseInt(arg[7].trim());
        Commons.COORDINATOR_APP_PORT = Integer.parseInt(arg[8].trim());

        Commons.POSTGRES_PASSWORD = arg[9].trim();
        Commons.POSTGRES_USERNAME = arg[10].trim();
        Commons.POSTGRES_HOST = arg[11].trim();
        Commons.POSTGRES_PORT = Integer.parseInt(arg[12].trim());
        Commons.POSTGRES_DB_NAME = arg[13].trim();
        Commons.MODE = arg[14].trim();

        Commons.POSTGRES_JDBC = "jdbc:postgresql://" + Commons.POSTGRES_HOST + ":" + Commons.POSTGRES_PORT + "/" + Commons.POSTGRES_DB_NAME;
        port(Commons.COORDINATOR_APP_PORT);
        System.out.println("REST-API Opened for business!");
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