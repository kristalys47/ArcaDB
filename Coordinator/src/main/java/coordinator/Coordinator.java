package coordinator;

import com.google.gson.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.orc.tools.JsonFileDump;
import org.json.JSONObject;
import org.apache.orc.tools.*;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import static spark.Spark.*;

public class Coordinator {
    public static void main(String[] arg) {
        port(7271);
        //TODO: Create insert function
        get("/database/query", (request, response) -> {
            JSONObject json  = new JSONObject(request.body());
//            JsonObject results = new JsonObject();
//            System.out.println(json.get("query"));
            ArrayList<String> result;
            try {
                result = Controller.handleRequest((String) json.get("query"));
//                Gson gson = new Gson();
                // create a reader
//                Reader reader = Files.newBufferedReader(Paths.get(result.get(0)));
//                results = gson.fromJson(reader, JsonObject.class);
            } catch (Exception e){
                return e;
            }
            return "java -jar /nfs/QUERY_RESULTS/orc-tools-1.7.5-uber.jar data " + result.get(0);

        });
    }
}