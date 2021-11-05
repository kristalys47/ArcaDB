package orc.nodes;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public abstract class Node {
    public int level;
    public String expression;
    public boolean isLeaf;
    public String columnName = "";
    public Node parent = null;
    public Node left = null;
    public Node right = null;
    protected int compare;
    public int inorderIndex;

    Node(int level, String expression, boolean isLeaf, int inorderIndex) {
        this.level = level;
        this.expression = expression;
        this.isLeaf = isLeaf;
        this.inorderIndex = inorderIndex;
        if(isLeaf){
            int index = 0;
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
                this.compare = -2;
            }

            if(expression.contains("\"")){
                this.value = expression.substring(index+2, expression.length()-1).getBytes(StandardCharsets.UTF_8);
            }else{
                this.value = ByteBuffer.allocate(8).putLong(Long.valueOf(expression.substring(index+1, expression.length()))).array();
            }
        } else{
            this.compare = expression.contains("|")? -1: 1;
        }
    }

    public boolean evaluateLeaf(byte[] eval) throws Exception {
        if(this.isLeaf) {
            for (int i = 0; i < this.value.length; i++) {
                if (!(Byte.compare(this.value[i], eval[i]) == 0)) {
                    if(this.compare == -2) {
                        return true;
                    } else {
                        switch (Byte.compare(this.value[i], eval[i])) {
                            case -1:
                                return this.compare == -1? true: false;
                            case 0:
                                return false;
                            case 1:
                                return this.compare == 1? true: false;
                        }
                    }
                }
            }
            return this.compare == 0 ? true : false;
        }
        else{
            throw new Exception("This evaluation can not be executed in a Node that is not a leaf");
        }
    }

    public boolean evaluateParent(boolean left, boolean right) throws Exception {
        if(!isLeaf){
            return this.compare==1? left&&right: left||right;
        }
        else{
            throw new Exception("This evaluation can not be executed in a Node that is not a leaf");
        }
    }

    @Override
    public String toString() {
        return "Node{" +
                "level=" + level +
                ", expression='" + expression + '\'' +
                ", isLeaf=" + isLeaf +
                ", columnName='" + columnName + '\'' +
                ", compare=" + compare +
                ", value=" + Arrays.toString(value) +
                ", inorderIndex=" + inorderIndex +
//                    ", valVal=" + valVal +
                '}';
    }
}
