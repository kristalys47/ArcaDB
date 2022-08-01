package coordinator.plan;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.json.JSONObject;

import java.sql.Statement;
import java.util.ArrayList;

public class ParallelHashJoinBinaryTreeNode extends BinaryTreeNode{
    private  String InnerRelation;
    private  String OuterRelation;
    public ArrayList<String> OuterTableFiles;
    public ArrayList<String> InnerTableFiles;
    public String OuterColumnName;
    public String InnerColumnName;

    public ParallelHashJoinBinaryTreeNode(JSONObject info, Statement cursor, BinaryTreeNode parent, BinaryTreeNode inner, BinaryTreeNode outer) {
        //TODO: send a query to catalog to get the files and everything
        super(NodeType.PARALLELJOIN, parent, inner, outer);
        if (info.has("Hash Cond")){
            String[] columns = info.getString("Hash Cond").replaceAll("\\(", "")
                    .replaceAll("\\)", "").split(" = ");
            String[] outCol = columns[0].split("\\.");
            String[] inCol = columns[1].split("\\.");
            this.OuterRelation = outCol[0];
            this.InnerRelation = inCol[0];
            this.OuterColumnName = outCol[1];
            this.InnerColumnName = inCol[1];
        }
    }

    private boolean isSimpleScan(BinaryTreeNode node) {
        if(node.inner == null && node.outer == null && node.type == NodeType.SCAN)
        {
            return true;
        }
        return false;
    }

    @Override
    public void run() {
        if(isSimpleScan(this.outer)) {
            //initiante patition
            ScanBinaryTreeNode relationA = (ScanBinaryTreeNode) this.outer;

            for (int i = 0; i < relationA.TableFiles.size(); i++) {
                JsonArray array = new JsonArray();
                array.add("joinPartition");
                array.add(relationA.TableFiles.get(i));
                array.add(this.OuterRelation);
                array.add("100");
                JsonObject obj = new JsonObject();
                obj.add("plan", array);
                //TODO: HERE I left
                connectionWithContainers(obj.toString(), "worker");
            }



        }
        if(isSimpleScan(this.inner)) {
            //initiante patition

        }

        if(this.outer != null){
            this.outer.run();
            this.OuterTableFiles = this.outer.resultFile;
        }
        if(this.inner != null){
            this.inner.run();
            this.InnerTableFiles = this.inner.resultFile;
        }

        for (int i = 0; i < this.OuterTableFiles.size(); i++) {
            JsonArray array = new JsonArray();
            array.add("join");
            array.add(this.OuterTableFiles.get(i));
            array.add(this.OuterColumnName);
            array.add(this.InnerTableFiles.get(i));
            array.add(this.InnerColumnName);
            this.resultFile.add("/nfs/QUERY_RESULTS/" + this.hashCode() + ".json");
            array.add(this.resultFile.get(i));
            //TODO: make request for resources and return the node to execute on
            JsonObject obj = new JsonObject();
            obj.add("plan", array);
            connectionWithContainers(obj.toString(), "worker");
        }
        this.setDone(true);
    }


}
