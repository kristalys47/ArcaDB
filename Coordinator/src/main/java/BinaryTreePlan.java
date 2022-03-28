import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Stack;

public class BinaryTreePlan {

    BinaryTreeNode headNode = null;

    private class StackNode {
        public JSONObject object;
        public int count = 0;
        public BinaryTreeNode node;

        public StackNode(JSONObject object, int count, BinaryTreeNode node) {
            this.object = object;
            this.count = count;
            this.node = node;
        }
    }

    public BinaryTreePlan(JSONObject plan){
        JSONObject content = plan.getJSONObject("Plan");
        Stack<StackNode> stack = new Stack<>();
        headNode = new BinaryTreeNode(getType(content.getString("Node Type")), null, null, null);
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
                        if(out.getString("Node Type").equals("Bitmap Heap Scan") || out.getString("Node Type").equals("Hash")){
                            peekNode.count++;
                        }
                        stackNode.setOuter(new BinaryTreeNode(getType(out.getString("Node Type")), stackNode, null, null));
                        stackNode = stackNode.getOuter();
                        stack.push(new StackNode(out, 0, stackNode));
                        break;
                    case 1:
                        peekNode.count++;
                        JSONObject in = array.getJSONObject(1);
                        stackNode.setInner(new BinaryTreeNode(getType(in.getString("Node Type")), stackNode, null, null));
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

    private BinaryTreeNode.NodeType getType(String node_type) {
        switch (node_type){
            case "Hash Join":
                return BinaryTreeNode.NodeType.JOIN;
            case "Seq Scan":
                return BinaryTreeNode.NodeType.SELECTION;
            case "Hash":
                return BinaryTreeNode.NodeType.HASH;
            case "Bitmap Heap Scan":
                return BinaryTreeNode.NodeType.SELECTION;
            default:
                return BinaryTreeNode.NodeType.NULL;

        }
    }

}
