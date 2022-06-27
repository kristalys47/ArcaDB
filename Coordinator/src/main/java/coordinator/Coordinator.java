package coordinator;

import com.google.gson.stream.JsonReader;
import org.json.JSONObject;

import static spark.Spark.*;

public class Coordinator {
    public static void main(String[] arg) {
        port(7271);
        get("/hello", (req, res)->"Hello, world");
        get("/database/query", (request, response) -> {

            JSONObject json  = new JSONObject(request.body());
            System.out.println(json.get("query"));

            Controller.handleRequest((String) json.get("query"));
            return "";
        });
    }
}