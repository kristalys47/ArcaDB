package orc.helperClasses.nodes;

import org.apache.hadoop.hive.ql.exec.vector.BytesColumnVector;

import java.nio.charset.StandardCharsets;

public class StringNode extends Node{
    public byte[] value;
    public String stringValue;

    //TODO: Consider creating different nodes instance for each kind of comparator (!=, =) so on for the other types.

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
        this.value = this.stringValue.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public int[] evaluateArray(int[] left, int[] right) throws Exception {
        throw new Exception("Leaf node cannot compare arrays");
    }

    @Override
    public int[] evaluateArray(Object value) throws Exception {
        BytesColumnVector cv = (BytesColumnVector) value;
        int[] result = new int[cv.vector.length];
        int start = 0;
        for (int i = 0; i < cv.vector.length; i++) {
            result[i] = evaluate(cv.vector[i], start, start+cv.length[i])? 1: 0;
            start += cv.length[i];
        }
        return result;
    }

    @Override
    public boolean evaluate(boolean left, boolean right) throws Exception {
        throw new Exception("Leaf node cannot compare two values");
    }

    @Override
    public boolean evaluate(Object value) throws Exception {
        throw new Exception("Leaf node cannot compare two values");
    }

    public boolean evaluate(Object value, int start, int end) throws Exception {
        if(value != null) {
            byte[] eval = (byte[]) value;
            if(end-start != this.value.length && this.compare == 0){
                return false;
            } else if (end-start != this.value.length && this.compare == 2) {
                return true;
            }else {
                for (int i = 0; i < this.value.length; i++) {
                    if (Byte.compare(this.value[i], eval[i+start]) != 0) {
                        return this.compare == 2? true: false;
                    }
                }
                return this.compare == 0 ? true : false;
            }
        }
        else
            return false;
    }
}
