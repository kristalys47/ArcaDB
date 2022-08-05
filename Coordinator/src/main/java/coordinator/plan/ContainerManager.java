package coordinator.plan;

import coordinator.Coordinator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Set;

import static coordinator.Commons.*;


public class ContainerManager extends Thread{
    private String containerIP;
    private String plan;

    private static final Logger logger = LogManager.getLogger(ContainerManager.class);
    public ContainerManager (String plan, String containerIP){
        this.containerIP = containerIP;
        this.plan = plan;
    }

    @Override
    public void run() {
        String received = "";
        try {
            Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT);
            String siteIP = "";
            while(siteIP.equals("")) {
                Set<String> nodes = jedis.smembers("node");
                for (String node : nodes) {
                    String status = jedis.get(node);
                    if (status.equals("available")) {
                        siteIP = node;
                        jedis.set(node, "Down");
                        break;
                    }
                }
            }

            System.out.println("Site: " + containerIP);
            System.out.println("Connected - - - - - -");
            Socket socket = new Socket(siteIP, WORKER_APP_PORT);
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

        } catch (Exception e) {
            logger.error("ERROR", e);
            e.printStackTrace();
        }
    }
}
