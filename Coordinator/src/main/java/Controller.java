import java.sql.*;

public class Controller {
    public static void handleRequest() throws SQLException {
        Connection c = null;
        Statement cursor = null;
        try {
            c = DriverManager.getConnection("jdbc:postgresql://localhost:5433/test",
                            "myusername", "mypassword");
            cursor = c.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
        System.out.println("Opened database successfully");

        String query = "select * from table_name";
        cursor.executeQuery(query);

        ResultSet set = cursor.executeQuery(query);

        while(set.next()){
            int a = set.getInt("column_1");
            int b = set.getInt("column_2");
            System.out.println(a + " " + b);
        }

    }
}
