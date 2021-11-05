package orc.unused;

import orc.nodes.Node;

public class LeafNode extends Node {

    @Override
    public boolean evaluate(boolean left, boolean right) throws Exception {
        return false;
    }

    @Override
    public boolean evaluate(Object value) throws Exception {
        return false;
    }
}
