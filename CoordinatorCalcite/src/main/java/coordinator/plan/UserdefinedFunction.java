package coordinator.plan;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import coordinator.CalciteOptimizer;
import coordinator.Commons;
import coordinator.Utils.Catalog;
import org.apache.calcite.adapter.jdbc.JdbcTableScan;
import org.apache.calcite.rel.RelNode;
import redis.clients.jedis.Jedis;

import java.util.List;


public class UserdefinedFunction implements Runnable{
    public List<String> TableFiles;
    public String functionName = "";
    public String tableName = "";
    public String filter = "";
    public boolean filterPass = true;

    public UserdefinedFunction(CalciteOptimizer plan) {
        RelNode info = plan.optimizedPlan;
        JdbcTableScan node = (JdbcTableScan) info;
        this.tableName = node.jdbcTable.jdbcTableName;
        this.TableFiles = Catalog.filesForTable(tableName);
    }

    @Override
    public void run() {
        //TODO: This part should definitely have threads  or different nodes to perform each part of the result
//        for(int i = 0 ; i < this.TableFiles.size(); ++i){
//
//            JsonArray array = new JsonArray();
//            array.add("scan");
//            array.add(this.TableFiles.get(i));
//            array.add(this.selection);
//            array.add(this.projection);
//            this.resultFile.add("/nfs/QUERY_RESULTS/" + this.hashCode() + i + ".temporc");
//            array.add(this.resultFile.get(i));
//            //TODO: make request for resources
//            JsonObject obj = new JsonObject();
//            obj.add("plan", array);
//            Jedis jedis = new Jedis(Commons.REDIS_HOST, Commons.REDIS_PORT);
//
//            jedis.rpush("task", obj.toString());
//            //TODO:IMPLEMENT SCAN WITH QUEUE
//        }
//
//        this.setDone(true);
//        //resquest for a node to perform this
        //Assign The files from the catalog

    }

}
