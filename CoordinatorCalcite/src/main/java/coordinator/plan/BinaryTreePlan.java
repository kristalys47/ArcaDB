package coordinator.plan;

import org.apache.calcite.rel.RelNode;

import java.util.Stack;

public class BinaryTreePlan {

    public BinaryTreeNode headNode = null;

    protected class StackNode {
        public RelNode object;
        public int count = 0;
        public BinaryTreeNode node;

        public StackNode(RelNode object, int count, BinaryTreeNode node) {
            this.object = object;
            this.count = count;
            this.node = node;
        }
    }

    public BinaryTreePlan(RelNode plan, Integer aCase, Integer buckets) throws Exception {
        Stack<StackNode> stack = new Stack<>();
        headNode = BinaryTreeNode.getNodeWithType(plan, null, null, null, buckets);
        stack.push(new StackNode(plan, 0, headNode));
        BinaryTreeNode stackNode = headNode;
        while(!stack.isEmpty()){
            StackNode peekNode = stack.peek();
            RelNode peek = peekNode.object;
            if(peek.getInputs().size()>0){
                switch(peekNode.count) {
                    case 0:
                        peekNode.count++;
                        RelNode out = peek.getInputs().get(0);

                        if(!peek.getRelTypeName().contains("Join")){
                            peekNode.count++;
                        }
                        stackNode.setOuter(BinaryTreeNode.getNodeWithType(out, stackNode, null, null, buckets));
                        stackNode = stackNode.getOuter();
                        stack.push(new StackNode(out, 0, stackNode));
                        break;
                    case 1:
                        peekNode.count++;
                        RelNode in = peek.getInputs().get(1);
                        stackNode.setInner(BinaryTreeNode.getNodeWithType(in, stackNode, null, null, buckets));
                        stackNode = stackNode.getInner();
                        stack.push(new StackNode(in, 0, stackNode));
                        break;
                    case 2:
                        StackNode delete = stack.pop();
                        stackNode = delete.node.getParent();
                        break;
                }
            } else {
                StackNode delete = stack.pop();
                stackNode = delete.node.getParent();
            }
        }
    }
}
