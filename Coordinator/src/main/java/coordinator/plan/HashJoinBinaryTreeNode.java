package coordinator.plan;

import com.google.gson.JsonArray;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Statement;
import java.util.ArrayList;

public class HashJoinBinaryTreeNode extends BinaryTreeNode{
    public ArrayList<String> TableAfiles;
    public ArrayList<String> TableBfiles;
    public String joinColumnTableA;
    public String joinColumnTableB;



    public HashJoinBinaryTreeNode(JSONObject info, Statement cursor, BinaryTreeNode parent, BinaryTreeNode inner, BinaryTreeNode outer) {
        //TODO: send a query to catalog to get the files and everything
        super(NodeType.JOIN, parent, inner, outer);
    }

    @Override
    public void run() {
        if(this.outer != null){
            this.outer.run();
            this.TableAfiles = this.outer.resultFile;
        }
        if(this.inner != null){
            this.inner.run();
            this.TableBfiles = this.inner.resultFile;
        }

        for (int i = 0; i < resultFile.size(); i++) {
            JsonArray array = new JsonArray();
            array.add("join");
            array.add(this.TableAfiles.get(i));
            array.add(this.joinColumnTableA);
            array.add(this.TableBfiles.get(i));
            array.add(this.joinColumnTableB);
            this.resultFile.add("/tmp/QUERY_RESULTS/" + this.hashCode());
            array.add(this.resultFile.get(i));
            //TODO: make request for resources and return the node to execute on
            JSONObject obj = new JSONObject();
            obj.put("plan", array);
            connectionWithContainers(obj.toString(), "join");
        }
    }
}
