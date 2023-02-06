package coordinator.plan;

import org.json.JSONObject;
import redis.clients.jedis.*;

import java.io.*;
import java.net.Socket;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static coordinator.Commons.*;

public abstract class BinaryTreeNode implements Runnable{
    public BinaryTreeNode inner;
    public BinaryTreeNode outer;
    public BinaryTreeNode parent;
    public NodeType type;
    public boolean isLeaf;
    public boolean done;
    public List<String> resultFile;
    public int buckets;


    public enum NodeType {PARALLELJOIN, JOIN, SCAN};

    public BinaryTreeNode(NodeType type, BinaryTreeNode parent, BinaryTreeNode inner, BinaryTreeNode outer, int buckets){
        this.type = type;
        this.parent = parent;
        this.outer = outer;
        this.inner = inner;
        this.isLeaf = false;
        this.done = false;
        this.buckets = buckets;
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

    @Override
    public void run(){
        switch (MODE) {
            case "queue":
                executeWithQueue();
                break;
            default:
                execute();
                break;
        }
    }

    public abstract void execute();
    public abstract void executeWithQueue();

    public boolean isSimpleScan(BinaryTreeNode node) {
        if (node.inner == null && node.outer == null && node.type == NodeType.SCAN) {
            return true;
        }
        return false;
    }

    public void connectionWithContainers(String args, String containerIP){
        String received = "";
        try {
            Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT);
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
            Socket socket = new Socket(siteIP, WORKER_APP_PORT);
            System.out.println("Connected to Server");

            OutputStream outR = socket.getOutputStream();
            InputStream inR = socket.getInputStream();

            PrintStream out = new PrintStream(outR);
            BufferedReader in = new BufferedReader(new InputStreamReader(inR));

            out.println(args + "/EOF");

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

        this.setDone(true);
    }


    static public BinaryTreeNode getNodeWithType(JSONObject object, Statement cursor, BinaryTreeNode parent, BinaryTreeNode inner, BinaryTreeNode outer, Integer aCase, Integer buckets){
        NodeType node_type= getType(object.getString("Node Type"));
//        TODO: depends on the mode we can change this. IMPORTANT
        switch (node_type){
            case JOIN:
                switch (aCase) {
                    case 1:
                        return new HashJoinBinaryTreeNode(object, cursor, parent, inner, outer, buckets);
                    default:
                        return new ParallelHashJoinBinaryTreeNode(object, cursor, parent, inner, outer, aCase, buckets);
                }

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
