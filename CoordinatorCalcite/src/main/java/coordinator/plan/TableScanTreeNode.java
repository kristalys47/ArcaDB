package coordinator.plan;

import org.apache.calcite.rel.RelNode;
import com.google.gson.*;

import java.util.List;


public class TableScanTreeNode extends BinaryTreeNode{
    public List<String> TableFiles;
    public String selection = "";
    public String projection = "";


    public TableScanTreeNode(RelNode info, BinaryTreeNode parent, BinaryTreeNode inner, BinaryTreeNode outer) {
        //TODO: send a query to catalog to get the files and everything
        //TODO: Check bucket stuff (Create it for the parent class special constructor. Overload it)
        super(NodeType.SCAN, parent, null, null, -1);
        System.out.println("Here is a scan");
        //TODO: you can get the relation from here to send to the hash join make it a variable;
//        info.
//        if (info.has("Relation Name"))
//            this.TableFiles = Catalog.filesForTable(info.getString("Relation Name"));
//        if (info.has("Recheck Cond"))
//            this.selection = transformSelection(info.getString("Recheck Cond"));
//        if (info.has("Filter"))
//            this.selection = transformSelection(info.getString("Filter"));
    }

    public String transformSelection(String conditions){
        String cleaned = conditions.replaceAll(" ", "").replaceAll("\\)AND\\(", "\\)&\\(")
                .replaceAll("\\)OR\\(", "\\)|\\(").replaceAll("::numeric", "").replaceAll("::text", "")
                .replaceAll("\\'", "");
        return cleaned;
    }

    @Override
    public void run() {
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
            //TODO:IMPLEMENT SCAN WITH QUEUE
        }

        this.setDone(true);
        //resquest for a node to perform this
        //Assign The files from the catalog

    }

}
