package orc.helperClasses.nodes;

import org.apache.hadoop.hive.ql.exec.vector.DecimalColumnVector;

public class DecimalNode extends Node{
    public float value;

    public DecimalNode(int level, String expression, boolean isLeaf, int inorderIndex) {
        super(level, expression, isLeaf, inorderIndex);
        int index = 0;
        //assume the left side is always the row value on memory
        if(expression.contains(">")){
            this.columnName = expression.substring(0, expression.indexOf(">"));
            this.compare = -1;
            index = expression.indexOf(">");
        } else if (expression.contains("=")){
            this.columnName = expression.substring(0, expression.indexOf("="));
            this.compare = 0;
            index = expression.indexOf("=");
        } else if (expression.contains("<")){
            this.columnName = expression.substring(0, expression.indexOf("<"));
            this.compare = 1;
            index = expression.indexOf("<");
        } else if (expression.contains("!=")){
            this.columnName = expression.substring(0, expression.indexOf("<"));
            this.compare = 2;
        }
        this.value = Float.valueOf(expression.substring(index+1));
    }

    @Override
    public int[] evaluateArray(int[] left, int[] right) throws Exception {
        throw new Exception("Leaf node cannot compare arrays");
    }

    @Override
    public int[] evaluateArray(Object value) throws Exception {
        DecimalColumnVector cv = (DecimalColumnVector) value;
        int[] result = new int[cv.vector.length];
        for (int i = 0; i < cv.vector.length; i++) {
            result[i] = evaluate(cv.vector[i])? 1: 0;
        }
        return result;
    }

    @Override
    public boolean evaluate(boolean left, boolean right) throws Exception {
        throw new Exception("Leaf node cannot compare two values");
    }

    @Override
    public boolean evaluate(Object value) throws Exception {
        float eval = (float) value;
        switch (this.compare){
            case -1:
                return eval>this.value;
            case 0:
                return eval==this.value;
            case 1:
                return eval<this.value;
            case 2:
                return eval!=this.value;
        }
        throw new Exception("Comparation was incorrectly innitialized.");
    }
}
