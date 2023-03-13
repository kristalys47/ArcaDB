package coordinator.plan;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import coordinator.Utils.Commons;
import org.json.JSONArray;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static coordinator.Utils.Commons.*;
import static coordinator.plan.BinaryTreeNode.DataType.STRUCTURED;

public class JoinTreeNode extends BinaryTreeNode {
    public List<String> OuterTableFiles;
    public List<String> InnerTableFiles;
    public String OuterColumnName;
    public String InnerColumnName;
    private String InnerRelation;
    private String OuterRelation;

    public JoinTreeNode(JSONObject info, Statement cursor, BinaryTreeNode parent, BinaryTreeNode inner, BinaryTreeNode outer, int buckets) {
        //TODO: send a query to catalog to get the files and everything
        super(NodeType.JOIN, parent, inner, outer, buckets);
        if (info.has("Hash Cond")) {
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

    public boolean isSimpleScan(BinaryTreeNode node) {
        if (node.inner == null && node.outer == null && node.type == NodeType.SCAN) {
            return true;
        }
        return false;
    }

    public int scanScheduleStructured(ScanTreeNode relation, String column, String relationName){
        Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT);
        for (int i = 0; i < relation.TableFiles.size(); i++) {
            JSONObject planJAVA = new JSONObject();
            planJAVA.put("planType", "joinPartition");
            planJAVA.put("files", new JSONArray().put(relation.TableFiles.get(i)));
            planJAVA.put("joinColumn", column);
            planJAVA.put("relation", relationName);
            planJAVA.put("filter", relation.selection);
            planJAVA.put("buckets", this.buckets);
            jedis.rpush("structured", planJAVA.toString());
        }
        return relation.TableFiles.size();
    }

    public int scanScheduleSemistructured(ScanTreeNode relation, String column, String relationName){
        List<String> files = relation.TableFiles;
        int numberOfGroups = (files.size()/PICTURE_PARTITION) + 1;

        JSONArray[] partitions = new JSONArray[numberOfGroups];

        for (int i = 0; i < partitions.length; i++) {
            partitions[i] = new JSONArray(PICTURE_PARTITION);
            int portion = i*(PICTURE_PARTITION);
            for (int j = 0; j < PICTURE_PARTITION; j++) {
                int index = portion + j;
                if(index < files.size())
                    partitions[i].put(files.get(portion + j));
                else{
                    break;
                }
            }
        }
        Jedis jedis = new Jedis(Commons.REDIS_HOST, Commons.REDIS_PORT);
        for (int i = 0; i < partitions.length; i++) {
            JSONObject planJAVA = new JSONObject();
            planJAVA.put("planType", "inference");
            planJAVA.put("files", partitions[i]);
            planJAVA.put("joinColumn", column);
            planJAVA.put("relation", relationName);
            planJAVA.put("filter", relation.selection);
            planJAVA.put("buckets", this.buckets);
            jedis.rpush("semistructured", planJAVA.toString());
        }
        return numberOfGroups;
    }

    @Override
    public void run() {
        if(((ScanTreeNode) inner).alias != ""){
            this.InnerRelation = ((ScanTreeNode) inner).relation;
        }
        if(((ScanTreeNode) outer).alias != ""){
            this.OuterRelation = ((ScanTreeNode) outer).relation;
        }

        //TODO: What if the join is in the same table?
        if (isSimpleScan(this.outer) && isSimpleScan(this.outer)) {
            //initiante partition
            ScanTreeNode relationOuter = (ScanTreeNode) this.outer;
            ScanTreeNode relationInner = (ScanTreeNode) this.inner;
            int inner_count = 0;
            int outer_count = 0;

            switch (relationOuter.typeData){
                case STRUCTURED:
                    outer_count = scanScheduleStructured(relationOuter, this.OuterColumnName, this.OuterRelation);
                    break;
                case SEMISTRUCTURED:
                    outer_count = scanScheduleSemistructured(relationOuter, this.OuterColumnName, this.OuterRelation);
                    break;
            }

            switch (relationInner.typeData){
                case STRUCTURED:
                    inner_count = scanScheduleStructured(relationInner, this.InnerColumnName, this.InnerRelation);
                    break;
                case SEMISTRUCTURED:
                    inner_count = scanScheduleSemistructured(relationInner, this.InnerColumnName, this.InnerRelation);
                    break;
            }

            Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT);

            for (int i1 = 0; i1 < inner_count+outer_count; i1++) {
                List<String> element = jedis.blpop(0, "done");
                if(!element.get(1).contains("Successful")){
                    System.out.println("A container failed" + element.get(1));
                    return;
                }
                System.out.println(element.get(1));
            }

            System.out.println("PROBING STAGE--------------");

            for (int i1 = 0; i1 < this.buckets; i1++) {

                JSONObject planJAVA = new JSONObject();
                planJAVA.put("planType", "joinProbing");
                planJAVA.put("outer", "/join/" + i1 + "/" + this.OuterRelation + "/");
                planJAVA.put("inner", "/join/" + i1 + "/" + this.InnerRelation + "/");
                jedis.rpush("structured", planJAVA.toString());
            }

            for (int i1 = 0; i1 < this.buckets; i1++) {
                List<String> element = jedis.blpop(0, "done");
                if(!element.get(1).contains("Successful")){
                    System.out.println("A container failed" + element.get(1));
                    return;
                }
                System.out.println(element.get(1));
            }

        } else {
            System.out.println("This case has not been set yet because the leaves are not directly " +
                    "connected to both the relations");
        }

    }
}
