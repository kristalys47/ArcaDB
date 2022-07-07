package coordinator;

import com.google.gson.*;
import org.json.JSONObject;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import static spark.Spark.*;

public class Coordinator {
    public static void main(String[] arg) {
        port(7271);
        //TODO: Create insert function
        get("/hello", (req, res)->"Hello, world");
        get("/database/query", (request, response) -> {

            JSONObject json  = new JSONObject(request.body());
            System.out.println(json.get("query"));

            ArrayList<String> result = Controller.handleRequest((String) json.get("query"));

            Gson gson = new Gson();

            // create a reader
            Reader reader = Files.newBufferedReader(Paths.get(result.get(0)));
            JsonObject results = gson.fromJson(reader, JsonObject.class);

            return "results";

        });
    }
}