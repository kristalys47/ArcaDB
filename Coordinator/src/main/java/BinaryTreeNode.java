public class BinaryTreeNode {
    private BinaryTreeNode inner;
    private BinaryTreeNode outer;
    private BinaryTreeNode parent;
    private NodeType type;
    boolean isLeaf;

    public enum NodeType {JOIN, SELECTION, PROJECTION, HASH, NULL};

    public BinaryTreeNode(NodeType type, BinaryTreeNode parent, BinaryTreeNode inner, BinaryTreeNode outer){
        this.type = type;
        this.parent = parent;
        this.outer = outer;
        this.inner = inner;
        this.isLeaf = false;
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
}
