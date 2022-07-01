package orc.helperClasses.nodes;

public abstract class Node <T> {
    public int level;
    public String expression;
    public boolean isLeaf;
    public String columnName = "";
    public Node parent = null;
    public Node left = null;
    public Node right = null;
    protected int compare;
    public int inorderIndex;

    Node(int level, String expression, boolean isLeaf, int inorderIndex) {
        this.level = level;
        this.expression = expression;
        this.isLeaf = isLeaf;
        this.inorderIndex = inorderIndex;
    }

    public Node() {

    }

    public abstract int[] evaluateArray(int[] left, int[] right) throws Exception;
    public abstract int[] evaluateArray(T value) throws Exception;

    public abstract boolean evaluate(boolean left, boolean right) throws Exception;
    public abstract boolean evaluate(T value) throws Exception;

}
