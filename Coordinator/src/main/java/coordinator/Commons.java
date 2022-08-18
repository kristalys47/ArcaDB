package coordinator;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Commons {

    static public int WORKER_APP_PORT;
    static public int COORDINATOR_APP_PORT;
    static public String REDIS_HOST;
    static public int REDIS_PORT;
    static public String REDIS_HOST_TIMES;
    static public int REDIS_PORT_TIMES;
    static public String S3_BUCKET;
    static public String AWS_S3_ACCESS_KEY;
    static public String AWS_S3_SECRET_KEY;
    static public String POSTGRES_PASSWORD;
    static public String POSTGRES_USERNAME;
    static public String POSTGRES_HOST;
    static public int POSTGRES_PORT;
    static public String POSTGRES_DB_NAME;
    static public String POSTGRES_JDBC;
    static public String MODE;

    static public Statement postgresConnect() throws Exception {
        Connection c = null;
        Statement cursor = null;
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection(POSTGRES_JDBC, POSTGRES_USERNAME, POSTGRES_PASSWORD);
            cursor = c.createStatement();
        } catch (Exception e) {
            System.err.println(e);
            throw new Exception("Database did not connect");
        }
        System.out.println("Opened database successfully");
        return cursor;
    }
}
