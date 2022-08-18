package orc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static orc.Commons.*;

public class main {

    static public void main(String[] arg) throws Exception {
        S3_BUCKET = arg[0];
        AWS_S3_ACCESS_KEY = arg[1];
        AWS_S3_SECRET_KEY = arg[2];
        REDIS_HOST = arg[3];
        REDIS_PORT = Integer.valueOf(arg[4]);
        REDIS_HOST_TIMES = arg[5];
        REDIS_PORT_TIMES = Integer.valueOf(arg[6]);
        WORKER_APP_PORT = Integer.valueOf(arg[7]);
        COORDINATOR_APP_PORT = Integer.valueOf(arg[8]);

        POSTGRES_PASSWORD = arg[9];
        POSTGRES_USERNAME = arg[10];
        POSTGRES_HOST = arg[11];
        POSTGRES_PORT = Integer.valueOf(arg[12]);
        POSTGRES_DB_NAME = arg[13];
        MODE = arg[14];

        POSTGRES_JDBC = "jdbc:postgresql://" + POSTGRES_HOST + ":" + POSTGRES_PORT + "/" + POSTGRES_DB_NAME;

        if (MODE.equals("queue")) {
            MainWithQueue.main(null);
        } else {

            Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT);
            Jedis jedisr = new Jedis(REDIS_HOST_TIMES, REDIS_PORT_TIMES);

            //TODO: need a better logger when this is working
            //TODO: custom port

            System.out.println("Started");

            ServerSocket serverSocket = new ServerSocket(WORKER_APP_PORT);
            jedis.rpush("node", ip);
            Socket client = serverSocket.accept();
            System.out.println("Connected - - - - - -");

            long start = System.currentTimeMillis();

            OutputStream outR = client.getOutputStream();
            InputStream inR = client.getInputStream();

            PrintStream out = new PrintStream(outR);
            BufferedReader in = new BufferedReader(new InputStreamReader(inR));

            String line = null;
            String message = "";
            while ((line = in.readLine()) != null) {
                message += line;
                if (line.contains("/EOF")) //have to decide the ending string
                    message = message.substring(0, message.length() - 4);
                break;
            }

            System.out.println(message);

            JsonObject gobj = JsonParser.parseString(message).getAsJsonObject();
            JsonArray rows = gobj.getAsJsonArray("plan");
            String[] args = new String[rows.size()];

            for (int i = 0; i < args.length; i++) {
                args[i] = rows.get(i).getAsString();
                System.out.println(args[i]);
            }

            try {
                if (gobj.has("outer") && gobj.has("inner")) {
                    WorkerManager.dbms(args, gobj);
                } else {
                    WorkerManager.dbms(args);
                }
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
                out.println("Something failed: " + e);
            } finally {
                out.println("Successful");
            }
            long end = System.currentTimeMillis();
            jedisr.rpush("times", "Container " + ip + " " + start + " " + end + " " + (end - start));

            out.close();
            in.close();
            client.close();
        }
    }
}
