import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;

public class Controller {
    public static void handleRequest() throws SQLException {
        Connection c = null;
        Statement cursor = null;
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection("jdbc:postgresql://localhost:5433/test",
                            "myusername", "mypassword");
            cursor = c.createStatement();
        } catch (Exception e) {
            System.err.println(e);
        }
        System.out.println("Opened database successfully");

        String query = "explain (format json, timing false, costs false) select * from table_name inner join table_name3 using(column_1) where table_name.column_1 > 20;";
        cursor.executeQuery(query);
        /*
        stmt.executeUpdate(sql); // insert update delete
        ResultSet rs = stmt.executeQuery( "SELECT * FROM COMPANY;" ); // query
        */

        ResultSet set = cursor.executeQuery(query);
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

    }

    private static void createTree(JSONObject obj) {
    }
}
