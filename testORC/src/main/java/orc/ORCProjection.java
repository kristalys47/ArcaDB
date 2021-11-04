package orc;

import org.apache.hadoop.hive.ql.exec.vector.BytesColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.LongColumnVector;
import org.apache.orc.OrcFilterContext;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Stack;
import java.util.function.Consumer;

public class ORCProjection {

    ORCProjection(){

    }

    public class Node implements Comparable {
        public int level;
        public String expression;
        public boolean isLeaf;
        public String columnName = "";
        public Node parent = null;
        public Node left = null;
        public Node right = null;
        public int compare;
        public byte[] value;
        public int inorderIndex;
//        public String valVal;

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
                }

                if(expression.contains("\"")){
//                    this.valVal = expression.substring(index+2, expression.length()-1);
                    this.value = expression.substring(index+2, expression.length()-1).getBytes(StandardCharsets.UTF_8);
                }else{
                    byte[] longBytes = new byte[Long.BYTES];
                    this.value = ByteBuffer.allocate(8).putLong(Long.valueOf(expression.substring(index+1, expression.length()))).array();
                }
            }
        }

        public boolean evaluate(byte[] eval) {
            for (int i = 0; i < this.value.length; i++) {
                if(!(Byte.compare(this.value[i], eval[i]) == 0)) {
                    switch (Byte.compare(this.value[i], eval[i])) {
                        case -1:
                            if(this.compare == -1) {
                                return true;
                            } else {
                                return false;
                            }
                        case 1:
                            if(this.compare == 1) {
                                return true;
                            }else {
                                return false;
                            }
                    }
                }
            }
            return this.compare == 0 ? true: false;
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

        @Override
        public int compareTo(@NotNull Object o) {
            return Integer.compare(this.level, ((Node) o).level);
        }
    }

    //Using order of operation
    public Consumer<OrcFilterContext> builder(String expression) {
        String test = "(((name=\"Kristal\")|((name=\"b\")|(val<10)))&(val>0))";
//        String test = "(((name=\"Kristal\")|(val<10))&(val>0))";
//        String test = "(val<11)";

        Stack<String> parsingLogicBooleanTree = new Stack();
        int level = 0;
        ArrayList<Node> order = new ArrayList<>();

        int start = 0;
        int max = 0;
        for (int i = 0; i < test.length(); i++) {
            if (test.charAt(i) == '(') {
                parsingLogicBooleanTree.push("(");
                start = i + 1;
                if (parsingLogicBooleanTree.size() > max) {
                    max = parsingLogicBooleanTree.size();
                }
            } else if (test.charAt(i) == ')') {
                if (!(test.charAt(i - 1) == ')')) {
                    order.add(new Node(parsingLogicBooleanTree.size(), test.substring(start, i), true, order.size()+1));
                }
                parsingLogicBooleanTree.pop();
            } else if (test.charAt(i) == '|') {
                order.add(new Node(parsingLogicBooleanTree.size(), "|", false, order.size()+1));
            } else if (test.charAt(i) == '&') {
                order.add(new Node(parsingLogicBooleanTree.size(), "&", false, order.size()+1));
            }
        }
        Node root = null;
        for (int i = 0; i < order.size(); i++) {
            if(order.get(i).level == 1){
                root = order.get(i);
                System.out.println("\n" + order.get(i) + "\n");
            }
            for (int j = 0; j < order.size(); j++) {
                if(order.get(i).level+1 == order.get(j).level){
                    if(i>j){
                        order.get(i).left = order.get(j);
                    }else {
                        order.get(i).right = order.get(j);
                    }
                    order.get(j).parent = order.get(i);
                }
            }
        }

        System.out.println(root.left);

        order.forEach(s -> System.out.println(s));
        System.out.println(max);

//        Consumer<OrcFilterContext> ct = batch -> {
//            BytesColumnVector cv = (BytesColumnVector) batch.findColumnVector("val")[0];
//            int index = 0;
//            int[] selected = batch.getSelected();
//
//            for (int i = 0; i < cv.vector.length; i++) {
//                if(cv.vector[i] < 9){ // Condition
//                    selected[index] = i;
//                    index++;
//                }
//            }
//            batch.setSelectedInUse(true);
//            batch.setSelected(selected);
//            batch.setSelectedSize(index);
//        };

        return new Consumer<OrcFilterContext>() {
            @Override
            public void accept(OrcFilterContext orcFilterContext) {

            }
        };
    }


    public static void projection(OrcFilterContext batch) {
        int newSize = 0;
        int[] selected = batch.getSelected();
        for (int row = 0; row < batch.getSelectedSize(); ++row) {
            if ((row % 2) == 0) { // this condition is the one that is fitlering stuff.
                selected[newSize++] = row;
            }
        }
        batch.setSelectedInUse(true);
        batch.setSelected(selected);
        batch.setSelectedSize(newSize);
    }

    public static void projections(OrcFilterContext gr) {
        LongColumnVector cv = (LongColumnVector) gr.findColumnVector("val")[0];
        int index = 0;
        int[] selected = gr.getSelected();
        for (int i = 0; i < cv.vector.length; i++) {
            if (cv.vector[i] < 9) { // Condition
                selected[index] = i;
                index++;
            }
        }
        gr.setSelectedInUse(true);
        gr.setSelected(selected);
        gr.setSelectedSize(index);
    }
}
