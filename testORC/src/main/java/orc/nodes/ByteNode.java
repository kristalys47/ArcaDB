package orc.nodes;

//TODO: Implement Bytes just in case

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ByteNode extends Node{
    public byte[] value;
    public String stringValue;


    public ByteNode(int level, String expression, boolean isLeaf, int inorderIndex) {
        super(level, expression, isLeaf, inorderIndex);
        stringValue= expression.substring(2, expression.length()-1);
        int index = 0;
        if (expression.contains("=")){
            this.columnName = expression.substring(0, expression.indexOf("="));
            this.compare = 0;
            index = expression.indexOf("=");
//            this.value = expression.substring(index+2, expression.length()-1);
        } else if (expression.contains("!=")){
            this.columnName = expression.substring(0, expression.indexOf("!"));
            this.compare = 1;
        }

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
