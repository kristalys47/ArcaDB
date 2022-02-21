package orc;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;
import com.lambdaworks.redis.RedisURI;
import com.sun.net.httpserver.Authenticator;

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
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class main {

    static public void main(String[] arg) throws Exception {

        System.out.println("Started");

        String host = "127.0.0.1";
        int socketPortNumber = 7172;
//        int RedisPortNumber = 6379;

        AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open();
        InetSocketAddress hostAddress = new InetSocketAddress("localhost", socketPortNumber);
        server.bind(hostAddress);


        Future acceptResult = server.accept();
        AsynchronousSocketChannel client = (AsynchronousSocketChannel) acceptResult.get();


        while (true) {

                if ((client != null) && (client.isOpen())) {

                    ByteBuffer buffer = ByteBuffer.allocate(16);
                    Future result = client.read(buffer);

                    while (!result.isDone()) {
                        // do nothing
                    }

                    buffer.flip();
                    String message = new String(buffer.array()).trim();

//                    RedisClient redisClient = new RedisClient(RedisURI.create("redis://" + host + ":" + RedisPortNumber));
//                    RedisConnection<String, String> connection = redisClient.connect();
//                    String[] args = connection.get(message).split(" ");

                    String[] nodePlan = message.split(" ");
                    try{
                        WorkerManager.dbms(nodePlan);
                    }catch (Exception e){
                        System.out.println(e);
                        String msg = "Something failed";
                        buffer.put(msg.getBytes(StandardCharsets.UTF_8));
                    } finally {
                        String msg = "Success";
                        buffer.put(msg.getBytes(StandardCharsets.UTF_8));
                    }
                    client.write(buffer);
                    buffer.clear();

                }
                client.close();
        }

    }
}
