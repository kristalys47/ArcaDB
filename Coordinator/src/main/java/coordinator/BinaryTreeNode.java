package coordinator;

import org.json.JSONObject;

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


    public enum NodeType {JOIN, SELECTION, PROJECTION, HASH, NULL};

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

    public void connectionWithContainers(String args, String containerID){
        String received = "";
        try {
            Socket socket = new Socket(containerID, APP_PORT);
            System.out.println("Connected to Server");
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

//            boolean finished = false;
//            while (!finished) {
                out.write(args.getBytes(StandardCharsets.UTF_8));
                out.flush();

                byte[] response;
                response = in.readAllBytes();
                received = new String(response);
                //TODO: Check signal for ending
//                finished = true;
//            }
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
//            case PROJECTION:
//                return new coordinator.ScanBinaryTreeNode(object, parent, inner, outer);
            case HASH:
                return new HashBinaryTreeNode(object, cursor, parent, inner, outer);
            default:
                return new ScanBinaryTreeNode(object, cursor, parent, inner, outer);

        }
    }

    static private BinaryTreeNode.NodeType getType(String node_type) {
        switch (node_type){
            case "Hash Join":
                return BinaryTreeNode.NodeType.JOIN;
            case "Seq Scan":
                return BinaryTreeNode.NodeType.PROJECTION;
            case "Hash":
                return BinaryTreeNode.NodeType.HASH;
            case "Bitmap Heap Scan":
                return BinaryTreeNode.NodeType.PROJECTION;
            default:
                return BinaryTreeNode.NodeType.PROJECTION;

        }
    }
}
