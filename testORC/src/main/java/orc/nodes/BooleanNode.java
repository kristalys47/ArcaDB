package orc.nodes;

//TODO: Implement BOOLEAN just in case

public class BooleanNode extends Node{

    public BooleanNode(int level, String expression, boolean isLeaf, int inorderIndex) {
        super(level, expression, isLeaf, inorderIndex);
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
