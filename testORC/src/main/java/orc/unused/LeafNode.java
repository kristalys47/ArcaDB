package orc.unused;

import orc.nodes.Node;

public class LeafNode extends Node {
    public int[] evaluateArray(int[] left, int[] right) throws Exception {
        return new int[0];
    }

    @Override
    public int[] evaluateArray(Object value) throws Exception {
        return new int[0];
    }

    @Override
    public boolean evaluate(boolean left, boolean right) throws Exception {
        return false;
    }

    @Override
    public boolean evaluate(Object value) throws Exception {
        return false;
    }
}
