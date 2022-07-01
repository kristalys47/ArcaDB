package orc;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
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

        int APP_PORT  = 7272;

//        JSONArray array = new JSONArray();
//        array.add(0, "scan");
//        array.add(1, "/mytable/hello.orc");
//        array.add(2, "id, name");
//        array.add(3, "(id>4)");
//        array.add(4, "/mytable/hellor.orc");
//        JSONObject obj = new JSONObject();
//        obj.put("plan", array);

//        JSONArray array = new JSONArray();
//        array.add(0, "insert");
//        array.add(1, "/tmp/data.json");
//        array.add(2, "/mytable/hello.orc");
//        array.add(3, "struct<id:int,name:string,email:string,address:string>");
//        JSONObject obj = new JSONObject();
//        obj.put("plan", array);

//        System.out.println(array);

        JsonArray garray = new JsonArray();
        garray.add("insert");
        garray.add("/tmp/data.json");
        garray.add("/mytable/hello.orc");
        garray.add("struct<id:int,name:string,email:string,address:string>");
        System.out.println(garray.toString());
        JsonObject gobj = new JsonObject();
        gobj.add("plan", garray);



        Socket socket = new Socket("c1", APP_PORT);

        OutputStream out = socket.getOutputStream();
        InputStream in = socket.getInputStream();


        String args = gobj.toString();
        System.out.println(args);
        out.write(args.getBytes(StandardCharsets.UTF_8));
        out.flush();

        String received = "";
        byte[] response;
        response = in.readAllBytes();
        received = new String(response);

    }
}
