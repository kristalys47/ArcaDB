package coordinator.plan;

import org.json.JSONObject;
import redis.clients.jedis.*;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.Statement;
import java.util.ArrayList;

public abstract class BinaryTreeNode implements Runnable{
    public static final int APP_PORT  = 7272;
    protected BinaryTreeNode inner;
    protected BinaryTreeNode outer;
    protected BinaryTreeNode parent;
    protected NodeType type;
    protected boolean isLeaf;
    protected boolean done;
    public ArrayList<String> resultFile;


    public enum NodeType {JOIN, SCAN};

    public BinaryTreeNode(NodeType type, BinaryTreeNode parent, BinaryTreeNode inner, BinaryTreeNode outer){
        this.type = type;
        this.parent = parent;
        this.outer = outer;
        this.inner = inner;
        this.isLeaf = false;
        this.done = false;
        this.resultFile = new ArrayList<>();
    }

    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public BinaryTreeNode getInner() {
        return inner;
    }

    public void setInner(BinaryTreeNode inner) {
        this.inner = inner;
    }

    public BinaryTreeNode getOuter() {
        return outer;
    }

    public void setOuter(BinaryTreeNode outer) {
        this.outer = outer;
    }

    public BinaryTreeNode getParent() {
        return parent;
    }

    public void setParent(BinaryTreeNode parent) {
        this.parent = parent;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public void setLeaf(boolean leaf) {
        isLeaf = leaf;
    }

    public abstract void run();

    public void connectionWithContainers(String args, String containerIP){
        String received = "";
        try {
            Socket socket = new Socket(containerIP, APP_PORT);
            System.out.println("Connected to Server");
            Jedis jedis = new Jedis("redis", 6379);

//            Jedis jedis = new Jedis("redis", 6379);
//
//            String nodes = jedis.get("node");
//            String[] site = nodes.split(",");
//            String siteIP = "";
//            for (int i = 0; i < site.length; i++) {
//                String[] status = site[i].split("-");
//                if (status[1].equals("available")) {
//                    siteIP = status[0];
//                    site[i] = status[0] + "-occupied";
//                    break;
//                }
//            }
//
//            String nodesChanged = "";
//            for (int i = 0; i < site.length; i++) {
//                if(i == 0){
//                    nodesChanged += site[i];
//                } else {
//                    nodesChanged += "," + site[i];
//                }
//            }
//
//            jedis.set("node", nodesChanged);

            System.out.println("Site: " + containerIP);
            System.out.println("Connected - - - - - -");

            OutputStream outR = socket.getOutputStream();
            InputStream inR = socket.getInputStream();

            PrintStream out = new PrintStream(outR);
            BufferedReader in = new BufferedReader(new InputStreamReader(inR));

            out.println(args);

            String line = null;
            String message = "";
            while((line = in.readLine()) != null)
            {
                message += line;
                if(line.contains("blip--")) //have to decide the ending string
                    break;
            }

            socket.close();

        } catch (Exception s) {
            System.out.println(s);
        }

        this.setDone(true);
    }


    static public BinaryTreeNode getNodeWithType(JSONObject object, Statement cursor, BinaryTreeNode parent, BinaryTreeNode inner, BinaryTreeNode outer){
        NodeType node_type= getType(object.getString("Node Type"));
        switch (node_type){
            case JOIN:
                return new HashJoinBinaryTreeNode(object, cursor, parent, inner, outer);
            default:
                return new ScanBinaryTreeNode(object, cursor, parent, inner, outer);

        }
    }

    static private BinaryTreeNode.NodeType getType(String node_type) {
        switch (node_type){
            case "Hash Join":
                return BinaryTreeNode.NodeType.JOIN;
            default:
                return BinaryTreeNode.NodeType.SCAN;

        }
    }
}
