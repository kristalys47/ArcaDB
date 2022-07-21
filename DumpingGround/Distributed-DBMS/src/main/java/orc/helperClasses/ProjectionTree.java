package orc.helperClasses;

import orc.helperClasses.nodes.*;
import org.apache.hadoop.hive.ql.exec.vector.ColumnVector;
import org.apache.orc.TypeDescription;

import java.util.ArrayList;
import java.util.Map;
import java.util.Stack;

import static orc.helperClasses.Utils.getTypeFromTypeCategory;

public class ProjectionTree {

    public SelectionTreeNode root;
    public ArrayList<String> columns;
    public TypeDescription schema;

    //Using order of operation
    public ProjectionTree(TypeDescription schema){
        this.root = null;
        this.columns = new ArrayList<>();
        this.schema = schema;
    }

    public SelectionTreeNode getNodeType(int level, String expression, boolean isLeaf, int inorderIndex){
        int index = 0;
        if(expression.contains(">")){
            index = expression.indexOf(">");
        } else if (expression.contains("=")){
            index = expression.indexOf("=");
        } else if (expression.contains("<")){
            index = expression.indexOf("<");
        } else if (expression.contains("!")){
            index = expression.indexOf("!");
        }

        int i = schema.getFieldNames().indexOf(expression.substring(0,index));

        switch (getTypeFromTypeCategory(schema.getChildren().get(i).getCategory())){
            case DECIMAL:
                return new DecimalSelectionTreeNode(level, expression, isLeaf, inorderIndex);
            case DOUBLE:
                return new DoubleSelectionTreeNode(level, expression, isLeaf, inorderIndex);
            case LONG:
                return new LongSelectionTreeNode(level, expression, isLeaf, inorderIndex);
            default:
                return new StringSelectionTreeNode(level, expression, isLeaf, inorderIndex);
        }

    }
    public void treeBuilder(String logicExpression) {

        //Creates a tree
        Stack<String> parsingLogicBooleanTree = new Stack();
        ArrayList<SelectionTreeNode> order = new ArrayList<>();

        int start = 0;
        int max = 0;
        for (int i = 0; i < logicExpression.length(); i++) {
            if (logicExpression.charAt(i) == '(') {
                parsingLogicBooleanTree.push("(");
                start = i + 1;
                if (parsingLogicBooleanTree.size() > max) {
                    max = parsingLogicBooleanTree.size();
                }
            } else if (logicExpression.charAt(i) == ')') {
                if (!(logicExpression.charAt(i - 1) == ')')) {
                    order.add(getNodeType(parsingLogicBooleanTree.size(), logicExpression.substring(start, i), true, order.size()+1));
                }
                parsingLogicBooleanTree.pop();
            } else if (logicExpression.charAt(i) == '|') {
                order.add(new LogicORSelectionTreeNode(parsingLogicBooleanTree.size(), "|", false, order.size()+1));
            } else if (logicExpression.charAt(i) == '&') {
                order.add(new LogicANDSelectionTreeNode(parsingLogicBooleanTree.size(), "&", false, order.size()+1));
            }
        }

        //Creates
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
    }

    public int[] treeEvaluation(Map<String, ColumnVector> map) throws Exception {
        return recursiveEval(this.root, map);
    }

    private int[] recursiveEval(SelectionTreeNode n, Map<String, ColumnVector> map) throws Exception {
        if(n.isLeaf){
            return n.evaluateArray(map.get(n.columnName));
        } else{
            return n.evaluateArray(recursiveEval(n.left, map), recursiveEval(n.right, map));
        }
    }
}
