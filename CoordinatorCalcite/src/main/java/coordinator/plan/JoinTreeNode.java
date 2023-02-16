package coordinator.plan;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import coordinator.Utils.ContainerManager;
import org.apache.calcite.rel.RelNode;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static coordinator.Commons.REDIS_HOST;
import static coordinator.Commons.REDIS_PORT;

public class JoinTreeNode extends BinaryTreeNode{
    public List<String> OuterTableFiles;
    public List<String> InnerTableFiles;
    public String OuterColumnName;
    public String InnerColumnName;
    private String InnerRelation;
    private String OuterRelation;

    public JoinTreeNode(RelNode info, BinaryTreeNode parent, BinaryTreeNode inner, BinaryTreeNode outer, int buckets) {
        //TODO: send a query to catalog to get the files and everything
        super(NodeType.JOIN, parent, inner, outer, buckets);
//        if (info.has("Hash Cond")){
//            String[] columns = info.getString("Hash Cond").replaceAll("\\(", "")
//                    .replaceAll("\\)", "").split(" = ");
//            String[] outCol = columns[0].split("\\.");
//            String[] inCol = columns[1].split("\\.");
//            this.OuterColumnName = outCol[1];
//            this.InnerColumnName = inCol[1];
//            this.OuterRelation = outCol[0];
//            this.InnerRelation = inCol[0];
//        }
    }


    @Override
    public void run() {
        TableScanTreeNode relationA = (TableScanTreeNode) this.outer;
        TableScanTreeNode relationB = (TableScanTreeNode) this.inner;
        JsonArray array = new JsonArray();
        array.add("join1");
        JsonArray arrayOuter = new JsonArray();
        for (String tableFile : relationA.TableFiles) {
            arrayOuter.add(tableFile);
        }
        array.add(this.OuterColumnName);
        array.add(this.OuterRelation);
        JsonArray arrayInner = new JsonArray();
        for (String tableFile : relationB.TableFiles) {
            arrayInner.add(tableFile);
        }
        array.add(this.InnerColumnName);
        array.add(this.InnerRelation);
        array.add(this.buckets);
        //TODO: make request for resources and return the node to execute on
        JsonObject obj = new JsonObject();
        obj.add("plan", array);
        obj.add("outer", arrayOuter);
        obj.add("inner", arrayInner);
        Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT);
        jedis.rpush("task", obj.toString());

        List<String> element = jedis.blpop(0, "done");
        if(!element.get(1).contains("Successful")){
            System.out.println("A container failed" + element.get(1));
        }
    }
}
