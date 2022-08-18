package coordinator.plan;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static coordinator.Commons.REDIS_HOST;
import static coordinator.Commons.REDIS_PORT;

public class HashJoinBinaryTreeNode extends BinaryTreeNode{
    public List<String> OuterTableFiles;
    public List<String> InnerTableFiles;
    public String OuterColumnName;
    public String InnerColumnName;
    private String InnerRelation;
    private String OuterRelation;

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
            this.OuterRelation = outCol[0];
            this.InnerRelation = inCol[0];
        }
    }

    @Override
    public void execute(){
        if (isSimpleScan(this.outer) && isSimpleScan(this.outer)) {
            //initiante patition
            int buckets = 5;
            ScanBinaryTreeNode relationA = (ScanBinaryTreeNode) this.outer;
            ScanBinaryTreeNode relationB = (ScanBinaryTreeNode) this.inner;
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
            array.add(5);
            //TODO: make request for resources and return the node to execute on
            JsonObject obj = new JsonObject();
            obj.add("plan", array);
            obj.add("outer", arrayOuter);
            obj.add("inner", arrayInner);
            ContainerManager threadRun = new ContainerManager(obj.toString(), "worker");
            threadRun.run();
        }
    }

    @Override
    public void executeWithQueue() {
        int buckets = 5;
        ScanBinaryTreeNode relationA = (ScanBinaryTreeNode) this.outer;
        ScanBinaryTreeNode relationB = (ScanBinaryTreeNode) this.inner;
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
        array.add(5);
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

    public void execute1() {
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
