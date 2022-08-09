package orc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static orc.Commons.REDIS_HOST;
import static orc.Commons.REDIS_PORT;

public class main {

    static public void main(String[] arg) throws Exception {


        String ip = InetAddress.getLocalHost().getHostAddress();
        Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT);
        jedis.rpush("node", ip);

        //TODO: need a better logger when this is working
        //TODO: custom port

        System.out.println("Started");

        int socketPortNumber = 7272;

        ServerSocket serverSocket = new ServerSocket(socketPortNumber);
        Socket client = serverSocket.accept();
        System.out.println("Connected - - - - - -");

        OutputStream outR = client.getOutputStream();
        InputStream inR = client.getInputStream();

        PrintStream out = new PrintStream(outR);
        BufferedReader in = new BufferedReader(new InputStreamReader(inR));

        String line = null;
        String message = "";
        while((line = in.readLine()) != null)
        {
            message += line;
            if(line.contains("/EOF")) //have to decide the ending string
                message = message.substring(0, message.length()-4);
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

        try{
            if(gobj.has("outer") && gobj.has("inner")){
                WorkerManager.dbms(args, gobj);
            } else {
                WorkerManager.dbms(args);
            }
        }catch (Exception e){
            System.out.println(e);
            e.printStackTrace();
            out.println("Something failed: " + e);
        } finally {
            out.println("Successful");
        }

        out.close();
        in.close();
        client.close();
    }
}
