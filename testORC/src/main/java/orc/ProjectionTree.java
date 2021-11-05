package orc;

import orc.nodes.LogicANDNode;
import orc.nodes.LogicORNode;
import orc.nodes.Node;

import java.util.*;

public class ProjectionTree {

    public Node root;
    public ArrayList<String> columns;

    //Using order of operation
    ProjectionTree(){
        this.root = null;
        this.columns = new ArrayList<>();
    }

    public void getNodeType(int level, String expression, boolean isLeaf, int inorderIndex, Dictionary<String, String> dic){

//        return new Node<>
    }
    public void treeBuilder(String test) {
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
//                    order.add(new Node(parsingLogicBooleanTree.size(), test.substring(start, i), true, order.size()+1));
                }
                parsingLogicBooleanTree.pop();
            } else if (test.charAt(i) == '|') {
                order.add(new LogicORNode(parsingLogicBooleanTree.size(), "|", false, order.size()+1));
            } else if (test.charAt(i) == '&') {
                order.add(new LogicANDNode(parsingLogicBooleanTree.size(), "&", false, order.size()+1));
            }
        }

        for (int i = 0; i < order.size(); i++) {
            if(order.get(i).isLeaf){
                this.columns.add(order.get(i).columnName);
            }
            if(order.get(i).level == 1){
                this.root = order.get(i);
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

        System.out.println(max);
        order.forEach(s -> System.out.println(s));

    }

    public boolean treeEvaluation(Map<String, Object> map) throws Exception {
        return recursiveEval(this.root, map);
    }

    public boolean recursiveEval(Node n, Map<String, Object> map) throws Exception {
        if(n.isLeaf){
            return n.evaluate(map.get(n.columnName));
        } else{
            return n.evaluate(recursiveEval(n.left, map), recursiveEval(n.right, map));
        }

    }


//    public void projection(OrcFilterContext batch) {
//        BytesColumnVector cv = (BytesColumnVector) batch.findColumnVector("val")[0];
//            int index = 0;
//            int[] selected = batch.getSelected();
//
//            for (int i = 0; i < cv.vector.length; i++) {
//                if(cv.vector[i] < 9){ // Condition
//                    selected[index] = i;
//                    index++;
////                }
//            }
//            batch.setSelectedInUse(true);
//            batch.setSelected(selected);
//            batch.setSelectedSize(index);
//    }
//
//    public static void projections(OrcFilterContext gr) {
//        LongColumnVector cv = (LongColumnVector) gr.findColumnVector("val")[0];
//        int index = 0;
//        int[] selected = gr.getSelected();
//        for (int i = 0; i < cv.vector.length; i++) {
//            if (cv.vector[i] < 9) { // Condition
//                selected[index] = i;
//                index++;
//            }
//        }
//        gr.setSelectedInUse(true);
//        gr.setSelected(selected);
//        gr.setSelectedSize(index);
//    }
//
//    Consumer<OrcFilterContext> consumer = gr ->
//    {
//        System.out.printf("m");
//        LongColumnVector cv = (LongColumnVector) gr.findColumnVector("val")[0];
//        int index = 0;
//        int[] selected = gr.getSelected();
//        for (int i = 0; i < cv.vector.length; i++) {
//            if(cv.vector[i] < 9){ // Condition
//                selected[index] = i;
//                index++;
//            }
//        }
//        gr.setSelectedInUse(true);
//        gr.setSelected(selected);
//        gr.setSelectedSize(index);
//    };

}
