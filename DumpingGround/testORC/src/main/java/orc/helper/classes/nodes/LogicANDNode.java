package orc.helper.classes.nodes;

public class LogicANDNode extends Node{
    public LogicANDNode(int level, String expression, boolean isLeaf, int inorderIndex) {
        super(level, expression, false, inorderIndex);
    }

    @Override
    public int[] evaluateArray(int[] left, int[] right) throws Exception {
        int[] result = new int[left.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (left[i]+right[i]) == 2? 1: 0;
        }
        return result;
    }

    @Override
    public int[] evaluateArray(Object value) throws Exception {
        throw new Exception("Parent node cannot compare values");
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
