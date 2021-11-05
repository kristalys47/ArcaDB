package orc.nodes;

public class LogicANDNode extends Node{
    public LogicANDNode(int level, String expression, boolean isLeaf, int inorderIndex) {
        super(level, expression, false, inorderIndex);
    }

    @Override
    public boolean evaluate(boolean left, boolean right) {
        return left&&right;
    }

    @Override
    public boolean evaluate(Object value) throws Exception {
            throw new Exception("Parent node cannot compare one value");
    }
}
