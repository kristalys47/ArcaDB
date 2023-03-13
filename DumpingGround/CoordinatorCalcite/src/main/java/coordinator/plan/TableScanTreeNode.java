package coordinator.plan;

import coordinator.CalciteOptimizer;
import coordinator.Commons;
import coordinator.Utils.Catalog;
import org.apache.calcite.adapter.jdbc.JdbcTableScan;
import org.apache.calcite.rel.RelNode;
import com.google.gson.*;
import redis.clients.jedis.Jedis;

import java.util.List;


public class TableScanTreeNode extends BinaryTreeNode{
    public List<String> TableFiles;
    public String selection = "";
    public String projection = "";
    public String tableName = "";


    public TableScanTreeNode(RelNode info, BinaryTreeNode parent, BinaryTreeNode inner, BinaryTreeNode outer, CalciteOptimizer plan) {
        //TODO: send a query to catalog to get the files and everything
        //TODO: Check bucket stuff (Create it for the parent class special constructor. Overload it)
        super(NodeType.SCAN, parent, null, null, -1);
        JdbcTableScan node = (JdbcTableScan) info;
        this.tableName = node.jdbcTable.jdbcTableName;
        this.TableFiles = Catalog.filesForTable(tableName);
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
            Jedis jedis = new Jedis(Commons.REDIS_HOST, Commons.REDIS_PORT);

            jedis.rpush("task", obj.toString());
            //TODO:IMPLEMENT SCAN WITH QUEUE
        }

        this.setDone(true);
        //resquest for a node to perform this
        //Assign The files from the catalog

    }

}
