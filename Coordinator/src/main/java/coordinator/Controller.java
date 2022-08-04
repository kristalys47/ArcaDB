package coordinator;

import coordinator.plan.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static coordinator.CommonVariables.postgresConnect;

public class Controller {
    public static List<String> handleRequest(String query) throws Exception {
        Statement cursor = postgresConnect();
        String explain_query = "explain (format json, timing false, costs false) " + query;

        ResultSet set = cursor.executeQuery(explain_query);
        String a = "";
        while(set.next()){
            a = set.getString(1);
        }
        a = a.substring(1, a.length()-1);
        System.out.println(a);
        JSONObject obj = new JSONObject(a);

        BinaryTreePlan btp = new BinaryTreePlan(obj, cursor);

        btp.headNode.run();

        return btp.headNode.resultFile;
    }

    private static void createTree(JSONObject obj) {
    }
}
