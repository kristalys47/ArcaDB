package coordinator;

import coordinator.plan.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;
import java.util.ArrayList;

public class Controller {
    public static ArrayList<String> handleRequest(String query) throws Exception {
        Connection c = null;
        Statement cursor = null;
        try {
            Class.forName("org.postgresql.Driver");
            // local test
//            c = DriverManager.getConnection("jdbc:postgresql://localhost:5434/test",
            c = DriverManager.getConnection("jdbc:postgresql://postgresql:5432/test",
                            "myusername", "mypassword");
            cursor = c.createStatement();
        } catch (Exception e) {
            System.err.println(e);
            throw new Exception("Database did not connect");
        }
        System.out.println("Opened database successfully");

        String explain_query = "explain (format json, timing false, costs false) " + query;
//        cursor.executeQuery(explain_query);

        ResultSet set = cursor.executeQuery(explain_query);
        String a = "";
        while(set.next()){
            a = set.getString(1);
        }
        a = a.substring(1, a.length()-1);
        System.out.println(a);
        JSONObject obj = new JSONObject(a);

        BinaryTreePlan btp = new BinaryTreePlan(obj, cursor);
        System.out.println("mm");

        btp.headNode.run();

        return btp.headNode.resultFile;
    }

    private static void createTree(JSONObject obj) {
    }
}
