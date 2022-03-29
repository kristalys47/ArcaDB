import org.json.JSONObject;

import java.sql.Statement;
import java.util.ArrayList;

public class HashBinaryTreeNode extends BinaryTreeNode{

    public HashBinaryTreeNode(JSONObject info, Statement cursor, BinaryTreeNode parent, BinaryTreeNode inner, BinaryTreeNode outer) {
        //TODO: send a query to catalog to get the files and everything
        super(NodeType.HASH, parent, inner, outer);
    }

    @Override
    public void run() {
        this.outer.run();
        this.resultFile = this.outer.resultFile;
        this.setDone(true);
    }
}
