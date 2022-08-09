package coordinator;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import static coordinator.Commons.*;
import static spark.Spark.*;

public class Coordinator {
    private static final Logger logger = LogManager.getLogger(Coordinator.class);
    public static void main(String[] arg) {
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