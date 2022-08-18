package coordinator.plan;

import org.json.JSONException;
import org.json.JSONObject;
import com.google.gson.*;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class ScanBinaryTreeNode extends BinaryTreeNode{
    public List<String> TableFiles;
    public String selection = "";
    public String projection = "";


    public ScanBinaryTreeNode(JSONObject info, Statement cursor, BinaryTreeNode parent, BinaryTreeNode inner, BinaryTreeNode outer) {
        //TODO: send a query to catalog to get the files and everything
        super(NodeType.SCAN, parent, inner, outer);
        //TODO: you can get the relation from here to send to the hash join make it a variable;
        if (info.has("Relation Name"))
            this.TableFiles = Catalog.filesForTable(info.getString("Relation Name"));
        if (info.has("Recheck Cond"))
            this.selection = transformSelection(info.getString("Recheck Cond"));
        if (info.has("Filter"))
            this.selection = transformSelection(info.getString("Filter"));

    }

    public String transformSelection(String conditions){
        String cleaned = conditions.replaceAll(" ", "").replaceAll("\\)AND\\(", "\\)&\\(")
                .replaceAll("\\)OR\\(", "\\)|\\(").replaceAll("::numeric", "").replaceAll("::text", "")
                .replaceAll("\\'", "");
        return cleaned;
    }

    @Override
    public void execute() {
        //TODO: This part should definitely have threads  or different nodes to perform each part of the result
        for(int i = 0 ; i < this.TableFiles.size(); ++i){
            JsonArray array = new JsonArray();
            array.add("scan");
            array.add(this.TableFiles.get(i));
            array.add(this.selection);
            array.add(this.projection);
            this.resultFile.add("/nfs/QUERY_RESULTS/" + this.hashCode() + i + ".temporc");
            array.add(this.resultFile.get(i));
            //TODO: make request for resources
            JsonObject obj = new JsonObject();
            obj.add("plan", array);
            connectionWithContainers(obj.toString(), "worker");
        }

        this.setDone(true);
        //resquest for a node to perform this
        //Assign The files from the catalog

    }

    @Override
    public void executeWithQueue() {
        //TODO: Complete this method
    }
}
