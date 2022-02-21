package orc.unused.Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Main {
    public static void main(String[] arg) throws IOException {
        client();
    }

    public static void client() {
        int portNumber = 7172;
        System.out.println("f");
        System.out.println("f");
        try {
            Socket socket = new Socket("server", portNumber);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

            String recieved = "";
            String send = "";
            while (true) {
                try {
                    recieved = in.readLine();
                    System.out.println(recieved);
                    send = stdIn.readLine();
                    out.println(send);
                    if (send.equals("Kristalys")) {
                        break;
                    }

                } catch (Exception s) {
                    System.out.println(s);
                }

            }

            socket.close();

        } catch (Exception s) {
            System.out.println(s);

        }

    }
}
