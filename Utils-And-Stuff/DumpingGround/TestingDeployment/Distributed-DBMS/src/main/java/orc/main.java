package orc;

import com.google.gson.*;
import redis.clients.jedis.*;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class main {

    static public void main(String[] arg) throws Exception {
        Jedis jedis = new Jedis("redis", 6379);
        String ip = InetAddress.getLocalHost().getHostAddress();
        String nodes;
        if(jedis.exists("node")){
            nodes = jedis.get("node");
            nodes = nodes + "," + ip + "-available";
        } else {
            nodes = ip + "-available";
        }

        jedis.set("node", nodes);
        System.out.println("Started");

        ServerSocket serverSocket = new ServerSocket(7272);
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
            if(line.equals("blip--"))
                break;
        }

        System.out.println(message);

        message += "blop-- ip: " + ip;
        out.println(message);

        out.close();
        in.close();
        client.close();
    }
}
