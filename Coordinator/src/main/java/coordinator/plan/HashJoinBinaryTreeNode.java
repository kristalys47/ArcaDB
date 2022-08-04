package coordinator.plan;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.json.JSONObject;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class HashJoinBinaryTreeNode extends BinaryTreeNode{
    public List<String> OuterTableFiles;
    public List<String> InnerTableFiles;
    public String OuterColumnName;
    public String InnerColumnName;

    public HashJoinBinaryTreeNode(JSONObject info, Statement cursor, BinaryTreeNode parent, BinaryTreeNode inner, BinaryTreeNode outer) {
        //TODO: send a query to catalog to get the files and everything
        super(NodeType.JOIN, parent, inner, outer);
        if (info.has("Hash Cond")){
            String[] columns = info.getString("Hash Cond").replaceAll("\\(", "")
                    .replaceAll("\\)", "").split(" = ");
            String[] outCol = columns[0].split("\\.");
            String[] inCol = columns[1].split("\\.");
            this.OuterColumnName = outCol[1];
            this.InnerColumnName = inCol[1];
        }
    }

    @Override
    public void execute() {
        if(this.outer != null){
            this.outer.run();
            this.OuterTableFiles = this.outer.resultFile;
        }
        if(this.inner != null){
            this.inner.run();
            this.InnerTableFiles = this.inner.resultFile;
        }
        ArrayList<String> result = new ArrayList<>();

        for (int i = 0; i < this.OuterTableFiles.size(); i++) {
            JsonArray array = new JsonArray();
            array.add("join");
            array.add(this.OuterTableFiles.get(i));
            array.add(this.OuterColumnName);
            array.add(this.InnerTableFiles.get(i));
            array.add(this.InnerColumnName);
            result.add("/nfs/QUERY_RESULTS/" + this.hashCode() + ".json");
            array.add(this.resultFile.get(i));
            //TODO: make request for resources and return the node to execute on
            JsonObject obj = new JsonObject();
            obj.add("plan", array);
            this.resultFile = result;
            connectionWithContainers(obj.toString(), "worker");
        }
        this.setDone(true);
    }
}
