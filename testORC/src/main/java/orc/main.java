package orc;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class main {
    private static FileWriter file;

    //insert /JavaCode/testORC struct<id:int,name:string,last:string,score:decimal,isFemale:boolean> {\"values\":[[\"Kristal\",\"3\"],[\"al\",\"4\"],[\"bob\",\"17\"],[\"Bi\",\"34\"],[\"col\",\"6\"],[\"Jil\",\"4\"],[\"sam\",\"3\"],[\"Dead\",\"0\"]]}
    //read /JavaCode/testORC name (((name="Kristal")|(val<-10))&(val>0))
    static public void main(String[] arg) throws Exception {


        System.out.println("Started");
        int portNumber = 7172;

        while (true) {
            try (AsynchronousServerSocketChannel server =
                         AsynchronousServerSocketChannel.open()) {
                server.bind(new InetSocketAddress("127.0.0.1",
                        portNumber));
                Future<AsynchronousSocketChannel> acceptCon =
                        server.accept();
                AsynchronousSocketChannel client = acceptCon.get(0,
                        TimeUnit.SECONDS);
                if ((client != null) && (client.isOpen())) {


                }
                client.close();
            } catch (Exception e) {
                e.printStackTrace();
            }


//        String host = "127.0.0.1";
//        //Using lettuce for this since it supports async calls...
//        RedisClient redisClient = new RedisClient(
//                RedisURI.create("redis://" + host + ":6379"));
//        RedisConnection<String, String> connection = redisClient.connect();
//
//        System.out.println("Connected to Redis");
//        connection.set("mmmmm", "kristal");
//        String value = connection.get("mmmmm");
//        System.out.println(value);
//
//        connection.close();
//        redisClient.shutdown();
//
//        StatefulRedisConnection<String, String> connection = client.connect();
//        RedisStringAsyncCommands<String, String> async = connection.async();
//        RedisFuture<String> set = async.set("key", "value")
//        RedisFuture<String> get = async.get("key")
//
//        async.awaitAll(set, get) == true
//
//        set.get() == "OK"
//        get.get() == "value"
//
////        JedisPooled jedis = new JedisPooled("localhost", 6379);


//        ORCManager.readerPrint("/JavaCode/insertedTest");
//        ORCManager.readerPrint("/JavaCode/results0");

            //[1136582, "neauaitvamlpcqe", "avnupgkhzbuques", 1153.75048828125, 1]
            String[] args = {"", "/JavaCode/insertedTest", "id,last,isFemale", "((name=\"neauaitvamlpcqe\")|(id<3))"};
            WorkerManager.dbms(args);
////
//        String test = "((name=\"qftrjyivexdeikecdhbf\")|(id<3))";
//        String projection = "last,isFemale";
//        ORCManager.reader("/JavaCode/insertedTest", projection, test);

        }

    }
}
