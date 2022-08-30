package coordinator.plan;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Statement;
import java.util.Stack;

public class BinaryTreePlan {

    public BinaryTreeNode headNode = null;

    protected class StackNode {
        public JSONObject object;
        public int count = 0;
        public BinaryTreeNode node;

        public StackNode(JSONObject object, int count, BinaryTreeNode node) {
            this.object = object;
            this.count = count;
            this.node = node;
        }
    }

    public BinaryTreePlan(JSONObject plan, Statement cursor, Integer aCase, Integer buckets){
        JSONObject content = plan.getJSONObject("Plan");
        Stack<StackNode> stack = new Stack<>();
        headNode = BinaryTreeNode.getNodeWithType(content, cursor, null, null, null, aCase, buckets);
        stack.push(new StackNode(content, 0, headNode));
        BinaryTreeNode stackNode = headNode;
        while(!stack.isEmpty()){
            StackNode peekNode = stack.peek();
            JSONObject peek = peekNode.object;
            if(peek.has("Plans")){
                JSONArray array = peek.getJSONArray("Plans");

                switch(peekNode.count) {
                    case 0:
                        peekNode.count++;
                        JSONObject out = array.getJSONObject(0);
                        String nodeType = out.getString("Node Type");
                        if(nodeType.equals("Bitmap Heap Scan") ||
                                nodeType.equals("Hash")){
                            peekNode.count++;
                        }
                        stackNode.setOuter(BinaryTreeNode.getNodeWithType(out, cursor, stackNode, null, null, aCase, buckets));
                        stackNode = stackNode.getOuter();
                        stack.push(new StackNode(out, 0, stackNode));
                        break;
                    case 1:
                        peekNode.count++;
                        JSONObject in = array.getJSONObject(1);;
                        nodeType = in.getString("Node Type");
                        if(nodeType.equals("Bitmap Heap Scan") ||
                                nodeType.equals("Hash")){
                            if(in.has("Plans")){
                                in = in.getJSONArray("Plans").getJSONObject(0);
                            }
                        }
                        stackNode.setInner(BinaryTreeNode.getNodeWithType(in, cursor, stackNode, null, null, aCase, buckets));
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
