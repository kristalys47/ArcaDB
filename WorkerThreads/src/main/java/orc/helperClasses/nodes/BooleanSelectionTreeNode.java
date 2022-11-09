package orc.helperClasses.nodes;

//TODO: Implement BOOLEAN just in case
//TODO: IMPLEMENT FLOAT

public class BooleanSelectionTreeNode extends SelectionTreeNode {

    public BooleanSelectionTreeNode(int level, String expression, boolean isLeaf, int inorderIndex) {
        super(level, expression, isLeaf, inorderIndex);
    }

    public int[] evaluateArray(int[] left, int[] right) throws Exception {
        return new int[0];
    }

    @Override
    public int[] evaluateArray(Object value) throws Exception {
        return new int[0];
    }

    @Override
    public boolean evaluate(boolean left, boolean right) throws Exception {
        throw new Exception("Leaf node cannot compare two values");
    }

    @Override
    public boolean evaluate(Object value) {
        return false;
    }
}
