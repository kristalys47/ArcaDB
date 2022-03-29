import org.json.JSONObject;

import java.sql.Statement;
import java.util.ArrayList;

public class ScanBinaryTreeNode extends BinaryTreeNode{
    public ArrayList<String> TableFiles;
    public String selection = "";
    public String projection = "";


    public ScanBinaryTreeNode(JSONObject info, Statement cursor, BinaryTreeNode parent, BinaryTreeNode inner, BinaryTreeNode outer) {
        //TODO: send a query to catalog to get the files and everything
        super(NodeType.PROJECTION, parent, inner, outer);
        this.selection = info.getString("Recheck Cond");
    }

    @Override
    public void run() {
        //TODO: This part should definitely have threads  or different nodes to perform each part of the result
        for(int i = 0 ; i < resultFile.size(); ++i){
            this.outer.run();
            this.resultFile = this.outer.resultFile;
            this.setDone(true);
        }
        //resquest for a node to perform this
        //Assign The files from the catalog

    }
}
