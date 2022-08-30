package coordinator;

import coordinator.plan.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.sql.*;
import java.util.List;

import static coordinator.Commons.postgresConnect;

public class Controller {
    private static final Logger logger = LogManager.getLogger(Controller.class);
    public static boolean handleRequest(String query, Integer aCase, int buckets) throws Exception {
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
        System.out.println("test 1");
        BinaryTreePlan btp = new BinaryTreePlan(obj, cursor, aCase, buckets);
        System.out.println("test 2");
        btp.headNode.run();
        System.out.println("test 3");
        return true;
    }

    private static void createTree(JSONObject obj) {
    }
}
