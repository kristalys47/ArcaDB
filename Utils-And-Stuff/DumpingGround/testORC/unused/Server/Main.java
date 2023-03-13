

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] arg) throws IOException {
        server();
    }

    public static void server() {
        System.out.println("Started");
        int portNumber = 7172;
        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);
            Socket socket = serverSocket.accept();
            System.out.println("Connected Server");

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String recieved = "";
            while (true) {
                try {
                    out.println("Nop, Not alive.");
                    recieved = in.readLine();
                    System.out.println(recieved);
                    if (recieved.equals("Kristalys")) {
                        out.println("Yep, She still alive.");
                        break;
                    }
                } catch (Exception s) {
                    System.out.println(s);
                }

            }

            socket.close();
            serverSocket.close();

        } catch (Exception s) {
            System.out.println(s);

        }
    }

}
