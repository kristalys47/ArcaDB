package coordinator.plan;

import org.apache.calcite.rel.RelNode;

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

    public boolean isSimpleScan(BinaryTreeNode node) {
        if (node.inner == null && node.outer == null && node.type == NodeType.SCAN) {
            return true;
        }
        return false;
    }


    static public BinaryTreeNode getNodeWithType(RelNode object, BinaryTreeNode parent, BinaryTreeNode inner, BinaryTreeNode outer, Integer buckets) throws Exception {
        NodeType node_type= getType(object.getRelTypeName());
//        TODO: depends on the mode we can change this. IMPORTANT
        switch (node_type){
            case JOIN:
                return new JoinTreeNode(object, parent, inner, outer, buckets);
            case FILTER:
                return new FilterTreeNode(object, parent, inner, outer, buckets);
            default:
                return new TableScanTreeNode(object, parent, inner, outer);
        }
    }

    static private BinaryTreeNode.NodeType getType(String node_type) throws Exception {
        if(node_type.contains("Join")){
            return NodeType.JOIN;
        } else if(node_type.contains("Filter")){
            return NodeType.FILTER;
        } else if(node_type.contains("Scan")){
            return NodeType.SCAN;
        } else{
            throw new Exception("Type " + node_type + " not recognized");
        }
    }
}
