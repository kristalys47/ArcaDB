package coordinator.plan;

import org.json.JSONException;
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
        super(NodeType.SCAN, parent, inner, outer);
        try {
            this.TableFiles = Catalog.filesForTable(info.getString("Relation Name"));
        } catch (JSONException exception){
            this.TableFiles = new ArrayList<>();
            System.out.println("This node is to be eliminated");
        }
        if (info.has("Recheck Cond"))
            this.selection = transformSelection(info.getString("Recheck Cond"));
        if (info.has("Filter"))
            this.selection = transformSelection(info.getString("Filter"));

    }

    public String transformSelection(String conditions){
        String cleaned = conditions.replaceAll(" ", "").replaceAll("\\)AND\\(", "\\)&\\(")
                .replaceAll("\\)OR\\(", "\\)|\\(").replaceAll("::numeric", "")
                .replaceAll("\\'", "");
        return cleaned;
    }

    @Override
    public void run() {
        //TODO: This part should definitely have threads  or different nodes to perform each part of the result
        for(int i = 0 ; i < TableFiles.size(); ++i){
            JsonArray array = new JsonArray();
            array.add("scan");
            array.add(this.TableFiles.get(i));
            array.add(this.selection);
            array.add(this.projection);
            this.resultFile.add("/tmp/QUERY_RESULTS/" + this.hashCode() + i + ".temporc");
            array.add(this.resultFile.get(i));
            //TODO: make request for resources
            JsonObject obj = new JsonObject();
            obj.add("plan", array);
            connectionWithContainers(obj.toString(), "c1");
        }

        this.setDone(true);
        //resquest for a node to perform this
        //Assign The files from the catalog

    }
}
