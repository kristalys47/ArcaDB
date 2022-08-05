package coordinator;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

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
            List<String> result;
            try {
                result = Controller.handleRequest((String) json.get("query"));
//                Gson gson = new Gson();
                // create a reader
//                Reader reader = Files.newBufferedReader(Paths.get(result.get(0)));
//                results = gson.fromJson(reader, JsonObject.class);
            } catch (Exception e){
                e.printStackTrace();
                return e.getMessage();
            }
            if(result.get(0).contains("orc")){
                return "java -jar /nfs/QUERY_RESULTS/orc-tools-1.7.5-uber.jar data " + result.get(0);
            } else {
                JsonReader gson = new JsonReader(new FileReader(result.get(0)));
                JsonObject jsonObject = JsonParser.parseReader(gson).getAsJsonObject();
                return jsonObject.getAsString();
            }
        });
    }
}