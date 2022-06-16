package coordinator;

import org.json.JSONArray;
import org.json.JSONObject;
import com.google.gson.*;


import java.sql.Statement;
import java.util.ArrayList;

public class ScanBinaryTreeNode extends BinaryTreeNode{
    public ArrayList<String> TableFiles;
    public String selection = "";
    public String projection = "";


    public ScanBinaryTreeNode(JSONObject info, Statement cursor, BinaryTreeNode parent, BinaryTreeNode inner, BinaryTreeNode outer) {
        //TODO: send a query to catalog to get the files and everything
        super(NodeType.PROJECTION, parent, inner, outer);
        this.TableFiles = new ArrayList<>();
        this.TableFiles.add("/mytable/hello.orc");
        if (info.has("Recheck Cond"))
            this.selection = info.getString("Recheck Cond");
        if (info.has("Filter"))
            this.selection = info.getString("Filter");
    }

    @Override
    public void run() {
        //TODO: This part should definitely have threads  or different nodes to perform each part of the result
        for(int i = 0 ; i < TableFiles.size(); ++i){
            JsonArray array = new JsonArray();
            array.add("scan");
            array.add(this.TableFiles.get(i));
            array.add(this.projection);
            array.add(this.selection);
            this.resultFile.add("/tmp/QUERY_RESULTS/" + this.hashCode());
            array.add(this.resultFile.get(i));
            //TODO: make request for resources
            JsonObject obj = new JsonObject();
            obj.add("plan", array);
            connectionWithContainers(obj.toString(), "join");
        }

        this.setDone(true);
        //resquest for a node to perform this
        //Assign The files from the catalog

    }
}
