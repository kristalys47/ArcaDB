package coordinator;

import redis.clients.jedis.Jedis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class CommonVariables {

    static public Jedis jedis = new Jedis("redis", 6379);

    static public final int APP_PORT = 7272;

    static public Statement postgresConnect() throws Exception {
        Connection c = null;
        Statement cursor = null;
        try {
            Class.forName("org.postgresql.Driver");

            // local test
//            c = DriverManager.getConnection("jdbc:postgresql://localhost:5433/postgres",
            c = DriverManager.getConnection("jdbc:postgresql://postgresql:5432/test",
                    "myusername", "mypassword");
            cursor = c.createStatement();
        } catch (Exception e) {
            System.err.println(e);
            throw new Exception("Database did not connect");
        }
        System.out.println("Opened database successfully");
        return cursor;
    }
}
