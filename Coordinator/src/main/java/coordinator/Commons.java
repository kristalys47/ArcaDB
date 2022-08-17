package coordinator;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Commons {

    static public final int WORKER_APP_PORT;
    static public final int COORDINATOR_APP_PORT;
    static public final String REDIS_HOST;
    static public final int REDIS_PORT;
    static public final String REDIS_HOST_TIMES;
    static public final int REDIS_PORT_TIMES;
    static public final String S3_BUCKET;
    static public final String AWS_S3_ACCESS_KEY;
    static public final String AWS_S3_SECRET_KEY;
    static public final String POSTGRES_PASSWORD;
    static public final String POSTGRES_USERNAME;
    static public final String POSTGRES_HOST;
    static public final int POSTGRES_PORT;
    static public final String POSTGRES_DB_NAME;
    static public final String POSTGRES_JDBC;

    static {
        Gson gson = new Gson();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/env.json"));
        } catch (Exception e){
            System.out.println("Error with loading file: ");
            e.printStackTrace();
        }
        JsonObject credentials = gson.fromJson(br, JsonObject.class);

        WORKER_APP_PORT = credentials.get("WORKER_APP_PORT").getAsInt();
        COORDINATOR_APP_PORT = credentials.get("COORDINATOR_APP_PORT").getAsInt();
        REDIS_HOST = credentials.get("REDIS_HOST").getAsString();
        REDIS_PORT = credentials.get("REDIS_PORT").getAsInt();
        REDIS_HOST_TIMES = credentials.get("REDIS_HOST_TIMES").getAsString();
        REDIS_PORT_TIMES = credentials.get("REDIS_PORT_TIMES").getAsInt();
        S3_BUCKET = credentials.get("S3_BUCKET").getAsString();
        AWS_S3_ACCESS_KEY = credentials.get("AWS_S3_ACCESS_KEY").getAsString();
        AWS_S3_SECRET_KEY = credentials.get("AWS_S3_SECRET_KEY").getAsString();
        POSTGRES_PASSWORD = credentials.get("POSTGRES_PASSWORD").getAsString();
        POSTGRES_USERNAME = credentials.get("POSTGRES_USERNAME").getAsString();
        POSTGRES_HOST = credentials.get("POSTGRES_HOST").getAsString();
        POSTGRES_PORT = credentials.get("POSTGRES_PORT").getAsInt();
        POSTGRES_DB_NAME = credentials.get("POSTGRES_DB_NAME").getAsString();
        POSTGRES_JDBC = "jdbc:postgresql://" + POSTGRES_HOST + ":" + POSTGRES_PORT + "/" + POSTGRES_DB_NAME;
    }


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
