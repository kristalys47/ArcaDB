package orc.nodes;

import java.nio.charset.StandardCharsets;

public class StringNode extends Node{
    public byte[] value;
    public String stringValue;

    //TODO: Consider creating different nodes intances for each kind if comparator (!=, =) so on for the other types.

    public StringNode(int level, String expression, boolean isLeaf, int inorderIndex) {
        super(level, expression, isLeaf, inorderIndex);
        //TODO: this if could be better;
        if (expression.contains("=")){
            this.columnName = expression.substring(0, expression.indexOf("="));
            this.compare = 0;
        } else if (expression.contains("!=")){
            this.columnName = expression.substring(0, expression.indexOf("!"));
            this.compare = 2;
        }
        int index = expression.indexOf("=");
        this.stringValue = expression.substring(index+2, expression.length()-1);
    }

    @Override
    public boolean evaluate(boolean left, boolean right) throws Exception {
        throw new Exception("Leaf node cannot compare two values");
    }

    @Override
    public boolean evaluate(Object value) throws Exception {
        byte[] eval = (byte[]) value;
        for (int i = 0; i < this.value.length; i++) {
            if (Byte.compare(this.value[i], eval[i]) != 0 && this.compare == 2) {
                    return true;
            }
        }
        return this.compare == 0 ? true : false;
    }
}
