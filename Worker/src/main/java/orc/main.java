package orc;

import com.google.gson.*;
import redis.clients.jedis.*;

import java.io.*;
import java.net.*;


public class main {

    static public void main(String[] arg) throws Exception {

        Jedis jedis = new Jedis("redis", 6379);
        String ip = InetAddress.getLocalHost().getHostAddress();
        jedis.sadd("node", ip);
        jedis.set(ip, "available");

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
            WorkerManager.dbms(args);
        }catch (Exception e){
            System.out.println(e);
            out.println("Something failed: " + e);
        } finally {
            out.println("Successful");
        }

        out.close();
        in.close();
        client.close();


    }
}
