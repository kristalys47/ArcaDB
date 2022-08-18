package coordinator;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import static coordinator.Commons.*;
import static spark.Spark.*;

public class Coordinator {
    private static final Logger logger = LogManager.getLogger(Coordinator.class);
    public static void main(String[] arg) {
        S3_BUCKET = arg[0];
        AWS_S3_ACCESS_KEY = arg[1];
        AWS_S3_SECRET_KEY = arg[2];
        REDIS_HOST = arg[3];
        REDIS_PORT = Integer.valueOf(arg[4]);
        REDIS_HOST_TIMES = arg[5];
        REDIS_PORT_TIMES = Integer.valueOf(arg[6]);
        WORKER_APP_PORT = Integer.valueOf(arg[7]);
        COORDINATOR_APP_PORT = Integer.valueOf(arg[8]);

        POSTGRES_PASSWORD = arg[9];
        POSTGRES_USERNAME = arg[10];
        POSTGRES_HOST = arg[11];
        POSTGRES_PORT = Integer.valueOf(arg[12]);
        POSTGRES_DB_NAME = arg[13];
        MODE = arg[14];

        POSTGRES_JDBC = "jdbc:postgresql://" + POSTGRES_HOST + ":" + POSTGRES_PORT + "/" + POSTGRES_DB_NAME;

        port(COORDINATOR_APP_PORT);
        //TODO: Create insert function
        get("/database/query", (request, response) -> {
            JSONObject json  = new JSONObject(request.body());
//            JsonObject results = new JsonObject();
//            System.out.println(json.get("query"));
            boolean result = false;
            try {
                result = Controller.handleRequest((String) json.get("query"), (Integer) json.get("mode"));
            } catch (Exception e){
                e.printStackTrace();
                return e.getMessage();
            }
            return result? "Done": "Failed";
        });
    }
}