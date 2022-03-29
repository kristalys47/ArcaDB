import org.json.JSONArray;
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
        if (info.has("Recheck Cond"))
            this.selection = info.getString("Recheck Cond");
    }

    @Override
    public void run() {
        //TODO: This part should definitely have threads  or different nodes to perform each part of the result
        for(int i = 0 ; i < resultFile.size(); ++i){
            JSONArray array = new JSONArray();
            array.put(0, "scan");
            array.put(1, this.TableFiles.get(i));
            array.put(2, this.projection);
            array.put(3, this.selection);
            this.resultFile.add("/tmp/QUERY_RESULTS/" + this.hashCode());
            array.put(5, this.resultFile.get(i));
            //TODO: make request for resources
            JSONObject obj = new JSONObject();
            obj.put("plan", array);
            connectionWithContainers(obj.toString(), "join");
        }

        this.outer.run();
        this.resultFile = this.outer.resultFile;
        this.setDone(true);
        //resquest for a node to perform this
        //Assign The files from the catalog

    }
}
