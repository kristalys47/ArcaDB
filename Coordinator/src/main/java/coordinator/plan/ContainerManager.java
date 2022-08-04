package coordinator.plan;

import java.io.*;
import java.net.Socket;
import java.util.Set;

import static coordinator.CommonVariables.*;


public class ContainerManager extends Thread{
    private String containerIP;
    private String plan;

    public ContainerManager (String plan, String containerIP){
        this.containerIP = containerIP;
        this.plan = plan;
    }

    @Override
    public void run() {
        String received = "";
        try {
            Set<String> nodes = jedis.smembers("node");
            String siteIP = "";
            for (String node: nodes) {
                String status = jedis.get(node);
                if (status.equals("available")) {
                    siteIP = node;
                    jedis.set(node, "Down");
                    break;
                }
            }

            System.out.println("Site: " + containerIP);
            System.out.println("Connected - - - - - -");
            Socket socket = new Socket(siteIP, APP_PORT);
            System.out.println("Connected to Server");

            OutputStream outR = socket.getOutputStream();
            InputStream inR = socket.getInputStream();

            PrintStream out = new PrintStream(outR);
            BufferedReader in = new BufferedReader(new InputStreamReader(inR));

            out.println(plan + "/EOF");

            String line = null;
            String message = "";
            while((line = in.readLine()) != null)
            {
                message += line;
                if(line.contains("Successful")) //have to decide the ending string
                    break;
                else
                    throw new Exception("There was an error in the container " + siteIP + " " + message);
            }

            socket.close();

        } catch (Exception s) {
            System.out.println(s);
            throw new RuntimeException("Not working" + s);
        }
    }
}
