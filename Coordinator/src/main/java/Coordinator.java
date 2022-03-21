
import com.google.gson.stream.JsonReader;
import org.json.JSONObject;

import static spark.Spark.*;

public class Coordinator {
    public static void main(String[] arg) {
        port(7271);
        get("/database/query", (request, response) -> {
            JSONObject json  = new JSONObject(request.body());
            String name = request.queryParams("name");
            Controller.handleRequest();
            /*
            String names = request.queryParams("name"); // This works for query params
            String names = request.params("name"); // this work if name is in path like :name
            String names = request.body(); // if it is in the body
             */
            return "This is it" + name + "--------\n" + json.get("name");
        });
    }
}