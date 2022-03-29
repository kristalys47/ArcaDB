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
        super(NodeType.HASH, parent, inner, outer);
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
            JSONArray array = new JSONArray();
            array.put(0, "join");
            array.put(1, this.TableAfiles.get(i));
            array.put(2, this.joinColumnTableA);
            array.put(3, this.TableBfiles.get(i));
            array.put(4, this.joinColumnTableB);
            this.resultFile.add("/tmp/QUERY_RESULTS/" + this.hashCode());
            array.put(5, this.resultFile.get(i));
            //TODO: make request for resources
            JSONObject obj = new JSONObject();
            obj.put("plan", array);
            connectionWithContainers(obj.toString(), "join");
        }



    }
}
