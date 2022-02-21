package orc;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;


public class client {

    static public void main(String[] arg) throws Exception {

        System.out.println("Started");

        String host = "127.0.0.1";
        int socketPortNumber = 7172;

        AsynchronousSocketChannel client = AsynchronousSocketChannel.open();
        InetSocketAddress hostAddress = new InetSocketAddress(host, socketPortNumber);
        Future future = client.connect(hostAddress);
        future.get();

        String nodePlan = "join /tmp/tableA.orc id /tmp/tableB.orc fk";

        ByteBuffer buffer = ByteBuffer.wrap(nodePlan.getBytes(StandardCharsets.UTF_8));
        String message = new String(buffer.array()).trim();

        client.write(buffer);
        buffer.clear();

        client.close();


    }
}
