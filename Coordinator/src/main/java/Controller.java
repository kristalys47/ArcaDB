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
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
        System.out.println("Opened database successfully");

        String query = "explain (format json) select * from table_name inner join table_name3 using(column_1) where table_name.column_1 > 20;";
        cursor.executeQuery(query);
        /*
        stmt.executeUpdate(sql); // insert update delete
        ResultSet rs = stmt.executeQuery( "SELECT * FROM COMPANY;" ); // query
        */

        ResultSet set = cursor.executeQuery(query);

        while(set.next()){
            String a = set.getString("QUERY PLAN");
            System.out.println(a);
        }

    }
}
