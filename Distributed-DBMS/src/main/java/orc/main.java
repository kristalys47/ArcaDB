package orc;

import com.google.gson.*;

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

        //TODO: need a better logger when this is working
        System.out.println("Started");

        int socketPortNumber = 7272;

        ServerSocket serverSocket = new ServerSocket(socketPortNumber);
        Socket client = serverSocket.accept();

        OutputStream out = client.getOutputStream();
        InputStream in = client.getInputStream();

        while (true) {

                if ((client != null) && (!client.isClosed())) {

                    byte[] buffer = in.readAllBytes();
                    String message = new String(buffer).trim();


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
