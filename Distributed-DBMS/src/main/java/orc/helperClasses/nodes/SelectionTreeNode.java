package orc.helperClasses.nodes;

public abstract class SelectionTreeNode<T> {
    public int level;
    public String expression;
    public boolean isLeaf;
    public String columnName = "";
    public SelectionTreeNode parent = null;
    public SelectionTreeNode left = null;
    public SelectionTreeNode right = null;
    protected int compare;
    public int inorderIndex;

    SelectionTreeNode(int level, String expression, boolean isLeaf, int inorderIndex) {
        this.level = level;
        this.expression = expression;
        this.isLeaf = isLeaf;
        this.inorderIndex = inorderIndex;
    }

    public SelectionTreeNode() {

    }

    public abstract int[] evaluateArray(int[] left, int[] right) throws Exception;
    public abstract int[] evaluateArray(T value) throws Exception;

    public abstract boolean evaluate(boolean left, boolean right) throws Exception;
    public abstract boolean evaluate(T value) throws Exception;

}
