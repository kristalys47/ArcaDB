package coordinator.plan;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import coordinator.CalciteOptimizer;
import org.apache.calcite.adapter.jdbc.JdbcRules;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rex.RexCall;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.sql.*;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static coordinator.Commons.REDIS_HOST;
import static coordinator.Commons.REDIS_PORT;

public class JoinTreeNode extends BinaryTreeNode{
    public List<String> OuterTableFiles;
    public List<String> InnerTableFiles;
    public String OuterColumnName;
    public String InnerColumnName;
    private String InnerRelation;
    private String OuterRelation;
    private CalciteOptimizer plan;

    public JoinTreeNode(RelNode info, BinaryTreeNode parent, BinaryTreeNode inner, BinaryTreeNode outer, int buckets, CalciteOptimizer plan) {
        //TODO: send a query to catalog to get the files and everything
        super(NodeType.JOIN, parent, inner, outer, buckets);
        JdbcRules.JdbcJoin node = (JdbcRules.JdbcJoin) info;
        this.plan = plan;
        getMappedValue((RexCall)node.getCondition());
    }

    private void getMappedValue(RexCall key) {
        List<RexNode> tables = key.getOperands();

        RexInputRef columnA = (RexInputRef) tables.get(0);
        RexInputRef columnB = (RexInputRef) tables.get(1);
        SqlSelect node = (SqlSelect) plan.sqlNode;
        List<SqlNode> list = node.getSelectList().getList();
        int columnAIndex = columnA.getIndex();
        int columnBIndex = columnB.getIndex();

        if (list.get(columnBIndex).getKind() == SqlKind.IDENTIFIER){
            this.OuterColumnName = ((SqlIdentifier) list.get(columnBIndex)).names.get(0);
            this.OuterRelation = ((SqlIdentifier) list.get(columnBIndex)).names.get(1);
        } else{
            List<SqlNode> listA = ((SqlBasicCall) list.get(columnBIndex)).getOperandList();
            this.OuterColumnName = ((SqlIdentifier) listA.get(0)).names.get(0);
            this.OuterRelation = ((SqlIdentifier) listA.get(0)).names.get(1);
            //The index 1 in list A has the renaming info.
        }
        if (list.get(columnAIndex).getKind() == SqlKind.IDENTIFIER){
            this.InnerColumnName = ((SqlIdentifier) list.get(columnAIndex)).names.get(0);
            this.InnerRelation = ((SqlIdentifier) list.get(columnAIndex)).names.get(1);
        } else{
            List<SqlNode> listB = ((SqlBasicCall) list.get(columnAIndex)).getOperandList();
            this.InnerColumnName = ((SqlIdentifier) listB.get(0)).names.get(0);
            this.InnerRelation = ((SqlIdentifier) listB.get(0)).names.get(1);
            //The index 1 in list A has the renaming info.
        }

    }


    @Override
    public void run() {

        //TODO: What if the join is in the same table?
        if (isSimpleScan(this.outer) && isSimpleScan(this.outer)) {
            //initiante patition
            TableScanTreeNode relationA = (TableScanTreeNode) this.outer;
            TableScanTreeNode relationB = (TableScanTreeNode) this.inner;
            Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT);
            int i = 0;
            int tindex = 0;
            while (i < relationA.TableFiles.size() || i < relationB.TableFiles.size()) {
                if (i < relationA.TableFiles.size()) {
                    JsonArray array = new JsonArray();
                    array.add("joinPartition3");
                    array.add(relationA.TableFiles.get(i));
                    array.add(this.OuterColumnName);
                    array.add(this.OuterRelation);
                    array.add(this.buckets);
                    JsonObject obj = new JsonObject();
                    obj.add("plan", array);
                    jedis.rpush("task", obj.toString());
                    tindex++;
                }
                if (i < relationB.TableFiles.size()) {
                    JsonArray array = new JsonArray();
                    array.add("joinPartition3");
                    array.add(relationB.TableFiles.get(i));
                    array.add(this.InnerColumnName);
                    array.add(this.InnerRelation);
                    array.add(this.buckets);
                    JsonObject obj = new JsonObject();
                    obj.add("plan", array);
                    jedis.rpush("task", obj.toString());
                    tindex++;
                }
                i++;
            }

            for (int i1 = 0; i1 < tindex; i1++) {
                List<String> element = jedis.blpop(0, "done");
                if(!element.get(1).contains("Successful")){
                    System.out.println("A container failed" + element.get(1));
                    return;
                }
                System.out.println(element.get(1));
            }

            System.out.println("PROBING STAGE--------------");

            for (int i1 = 0; i1 < this.buckets; i1++) {
                JsonArray array = new JsonArray();
                array.add("joinProbing3");
                
                //TODO: choose who is inner and who is outer
                array.add("/join/" + i1 + "/" + OuterRelation + "/");
                array.add("/join/" + i1 + "/" + InnerRelation + "/");
                array.add(i1);
                JsonObject obj = new JsonObject();
                obj.add("plan", array);
                jedis.rpush("task", obj.toString());
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
