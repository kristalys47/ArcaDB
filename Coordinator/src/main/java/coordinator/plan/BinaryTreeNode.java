package coordinator.plan;

import org.json.JSONObject;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public abstract class BinaryTreeNode implements Runnable{
    public BinaryTreeNode inner;
    public BinaryTreeNode outer;
    public BinaryTreeNode parent;
    public NodeType type;
    public boolean isLeaf;
    public boolean done;
    public List<String> resultFile;
    public int buckets;


    public enum NodeType {FILTER, JOIN, SCAN, PROJECT};
    public enum DataType {STRUCTURED, SEMISTRUCTURED, UNSTRUCTURED}

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
    public abstract void run();

    static public BinaryTreeNode getNodeWithType(JSONObject object, Statement cursor, BinaryTreeNode parent, BinaryTreeNode inner, BinaryTreeNode outer, Integer aCase, Integer buckets){
        NodeType node_type= getType(object.getString("Node Type"));
//        TODO: depends on the mode we can change this. IMPORTANT
        switch (node_type){
            case JOIN:
                return new JoinTreeNode(object, cursor, parent, inner, outer, buckets);
            default:
                return new ScanTreeNode(object, cursor, parent, inner, outer, buckets);
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
