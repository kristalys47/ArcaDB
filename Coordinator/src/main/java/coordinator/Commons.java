package coordinator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Commons {
    static public final String REDIS_HOST = "172.28.28.11"; //redis
    static public final int REDIS_PORT = 6379;

//    static public final String IGNITE_HOST = "172.28.28.11"; //136.145.116.98
//    static public final int IGNITE_PORT = 10800;
//    static public final String IGNITE_HOST_PORT = IGNITE_HOST + ":" + IGNITE_PORT;

    static public final String S3_BUCKET= "testingjoin";
    static public final String AWS_S3_ACCESS_KEY = "AKIA6E4TYZ3JLKC2LPFR";
    static public final String AWS_S3_SECRET_KEY = "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";

    static public final int WORKER_APP_PORT = 7272;
    static public final int COORDINATOR_APP_PORT = 7271;

    static public final String POSTGRES_PASSWORD = "mypassword";
    static public final String POSTGRES_USERNAME = "myusername";
    static public final String POSTGRES_HOST = REDIS_HOST; //postgresql ****REMEMBER THAT THIS MAY BE DIFFERENT
    static public final int POSTGRES_PORT = 5434;
    static public final String POSTGRES_DB_NAME = "test";
    static public final String POSTGRES_JDBC = "jdbc:postgresql://" + POSTGRES_HOST + ":" + POSTGRES_PORT + "/" + POSTGRES_DB_NAME;


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
