package coordinator.plan;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import coordinator.CalciteOptimizer;
import coordinator.Commons;
import coordinator.Utils.Catalog;
import org.apache.calcite.adapter.enumerable.EnumerableProject;
import org.apache.calcite.adapter.jdbc.JdbcTableScan;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.sql.*;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.List;


public class UserdefinedFunction implements Runnable{
    public String tableFiles;
    public String functionName = "";
    public String tableName = "";
    public String filter = "";
    public boolean filterPass = true;

    public UserdefinedFunction(CalciteOptimizer plan) {
        RelNode info = plan.optimizedPlan;
//        this.tableName = node.jdbcTable.jdbcTableName;


        SqlSelect node = (SqlSelect) plan.sqlNode;
        List<SqlNode> selectList = node.getSelectList().getList();
        SqlBasicCall sqlBasicCall = ((SqlBasicCall) selectList.get(0));

        List<SqlNode> operandList = sqlBasicCall.getOperandList();
        this.functionName = sqlBasicCall.getOperator().getName();
        this.tableName = ((SqlIdentifier) operandList.get(0)).names.get(0);
        this.filter = ((SqlIdentifier) operandList.get(0)).names.get(1);


        List<SqlNode> whereList = ((SqlBasicCall) node.getWhere()).getOperandList();
        this.filterPass = ((BigDecimal) ((SqlNumericLiteral) whereList.get(1)).getValue()).intValue() > 0? true: false;


        this.tableFiles = Catalog.filesForTable(this.tableName).get(0);


    }

    @Override
    public void run() {
            JsonArray array = new JsonArray();
            array.add("udf");
            array.add(this.tableFiles);
            array.add(this.functionName);
            array.add(this.filter);
            array.add(this.filterPass);
            JsonObject obj = new JsonObject();
            obj.add("plan", array);
            Jedis jedis = new Jedis(Commons.REDIS_HOST, Commons.REDIS_PORT);

            jedis.rpush("python", obj.toString());
            jedis.rpush("python", obj.toString());
            jedis.rpush("python", obj.toString());
            jedis.rpush("python", obj.toString());
            jedis.rpush("python", obj.toString());
            jedis.rpush("python", obj.toString());
            jedis.rpush("python", obj.toString());
            jedis.rpush("python", obj.toString());
            jedis.rpush("python", obj.toString());
            jedis.rpush("python", obj.toString());
            jedis.rpush("python", obj.toString());
            jedis.rpush("python", obj.toString());
            jedis.rpush("python", obj.toString());
            jedis.rpush("python", obj.toString());
            jedis.rpush("python", obj.toString());
            jedis.rpush("python", obj.toString());
            jedis.rpush("python", obj.toString());
            jedis.rpush("python", obj.toString());

            //TODO:IMPLEMENT SCAN WITH QUEUE
//            List<String> element = jedis.blpop(0, "donePython");
//            if(!element.get(1).contains("Successful")){
//                System.out.println("A container failed" + element.get(1));
//                return;
//            }

    }

}
