package coordinator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;

public class Controller {
    public static void handleRequest(String query) throws SQLException {
        Connection c = null;
        Statement cursor = null;
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection("jdbc:postgresql://postgresql:5432/postgres",
                            "myusername", "mypassword");
            cursor = c.createStatement();
        } catch (Exception e) {
            System.err.println(e);
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
        JSONArray array = new JSONArray();


        BinaryTreePlan btp = new BinaryTreePlan(obj, cursor);
        System.out.println("mm");


        btp.headNode.run();


    }

    private static void createTree(JSONObject obj) {
    }
}
