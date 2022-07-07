package coordinator;

import com.google.gson.*;
import redis.clients.jedis.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static spark.Spark.*;

public class Coordinator {
    public static void main(String[] arg) {
        port(7271);
        get("/testing", (request, response) -> {

            System.out.println("Entered Request");
            Jedis jedis = new Jedis("redis", 6379);
            Long count = jedis.incr("count");

            Long index = count % 3;
            String nodes = jedis.get("node");
            String[] site = nodes.split(",");
            String siteIP = "";
            for (int i = 0; i < site.length; i++) {
                String[] status = site[i].split("-");
                if (status[1].equals("available")) {
                    siteIP = status[0];
                    site[i] = status[0] + "-occupied";
                    break;
                }
            }
            String nodesChanged = "";
            for (int i = 0; i < site.length; i++) {
                if(i == 0){
                    nodesChanged += site[i];
                } else {
                    nodesChanged += "," + site[i];
                }
            }

            jedis.set("node", nodesChanged);

            System.out.println("Site: " + siteIP);
            Socket socket = new Socket(siteIP, 7272);
            System.out.println("Connected - - - - - -");

            OutputStream outR = socket.getOutputStream();
            InputStream inR = socket.getInputStream();

            PrintStream out = new PrintStream(outR);
            BufferedReader in = new BufferedReader(new InputStreamReader(inR));

            out.println("blip--");

            String line = null;
            String message = "";
            while((line = in.readLine()) != null)
            {
                message += line;
                if(line.contains("blop--"))
                    break;
            }

            System.out.println(message);

            out.close();
            in.close();
            socket.close();
            return "done";
        });
    }
}
