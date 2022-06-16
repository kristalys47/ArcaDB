package coordinator;

import com.google.gson.stream.JsonReader;
import org.apache.cassandra.streaming.StreamOut;
import org.json.JSONObject;

import static spark.Spark.*;

public class Coordinator {
    public static void main(String[] arg) {
        port(7271);
        get("/hello", (req, res)->"Hello, world");
        get("/database/query", (request, response) -> {
            System.out.println("Here?");
            JSONObject json  = new JSONObject(request.body());
            System.out.println(json.get("query"));
//            String name = request.queryParams("name");
//            System.out.println(name);

            Controller.handleRequest((String) json.get("query"));
            /*
            String names = request.queryParams("name"); // This works for query params
            String names = request.params("name"); // this work if name is in path like :name
            String names = request.body(); // if it is in the body
             */
//            return "This is it" + name + "--------\n" + json.get("name");
            return "";
        });
    }
}