package orc;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;


public class main {

    static public void main(String[] arg) throws Exception {

        System.out.println("Started");

        int socketPortNumber = 7272;

        ServerSocket serverSocket = new ServerSocket(socketPortNumber);
        Socket client = serverSocket.accept();
        System.out.println("Connected Server");

        OutputStream out = client.getOutputStream();
        InputStream in = client.getInputStream();

        while (true) {

                if ((client != null) && (!client.isClosed())) {

                    byte[] buffer = in.readAllBytes();
                    String message = new String(buffer).trim();

                    JSONParser parser = new JSONParser();
                    JSONObject obj = (JSONObject) parser.parse(message.replaceAll(" ", ""));
                    JSONArray rows = (JSONArray) obj.get("plan");

                    try{
                        WorkerManager.dbms(rows.toJSONString().split(","));
                    }catch (Exception e){
                        System.out.println(e);
                        String msg = "Something failed";
                        out.write(msg.getBytes(StandardCharsets.UTF_8));
                    } finally {
                        String msg = "Success";
                        out.write(msg.getBytes(StandardCharsets.UTF_8));
                    }
                    out.flush();
                }
                client.close();
        }

    }
}
