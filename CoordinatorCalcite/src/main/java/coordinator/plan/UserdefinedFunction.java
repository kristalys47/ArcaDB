package coordinator.plan;

import alluxio.AlluxioURI;
import alluxio.client.file.FileInStream;
import alluxio.client.file.FileSystem;

import alluxio.conf.PropertyKey;
import alluxio.exception.AlluxioException;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import org.apache.commons.io.FileUtils;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import com.google.gson.JsonArray;
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
    public final int PICTURE_PARTITION = 50;
    public String tableFiles;
    public String functionName = "";
    public String tableName = "";
    public String filter = "";
    public boolean filterPass = true;
    public JsonObject metadata = null;

    public UserdefinedFunction(CalciteOptimizer plan) throws IOException, AlluxioException {
        RelNode info = plan.optimizedPlan;

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
        long start = System.currentTimeMillis();
        alluxio.conf.Configuration.set(PropertyKey.MASTER_HOSTNAME, "136.145.77.107");
        alluxio.conf.Configuration.set(PropertyKey.SECURITY_LOGIN_USERNAME, "root");

        FileSystem fs = FileSystem.Factory.get();
        String alluxioPath = "alluxio://136.145.77.107:19998"+this.tableFiles+"metadata.json";
        System.out.println(this.tableFiles);
        System.out.println(alluxioPath);
        AlluxioURI pathAlluxio = new AlluxioURI(alluxioPath);
        FileInStream in = fs.openFile(pathAlluxio);
        File f = new File("metadata.json");
        FileUtils.copyInputStreamToFile(in, f);

        // convert JSON file to map
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new FileReader("metadata.json"));
        metadata = JsonParser.parseReader(reader).getAsJsonObject();
//        f.delete();

    }

    @Override
    public void run() {

        JsonArray files = (JsonArray) metadata.get("files");
        int numberOfGroups = (files.size()/PICTURE_PARTITION) + 1;

        JsonArray[]  partitions = new JsonArray[numberOfGroups];

        for (int i = 0; i < partitions.length; i++) {
            partitions[i] = new JsonArray(PICTURE_PARTITION);
            int portion = i*(PICTURE_PARTITION);
            for (int j = 0; j < PICTURE_PARTITION; j++) {
                int index = portion + j;
                if(index < files.size())
                    partitions[i].add(files.get(portion + j));
                else{
                    break;
                }
            }
        }
        Jedis jedis = new Jedis(Commons.REDIS_HOST, Commons.REDIS_PORT);
        for (int i = 0; i < partitions.length; i++) {
            JsonObject obj = new JsonObject();
            obj.addProperty("function", "udf");
            obj.addProperty("location", this.tableFiles);
            obj.addProperty("model", this.filter);
            obj.addProperty("selection", this.filterPass);
            obj.add("files", partitions[i]);
            JsonObject objPlan = new JsonObject();
            objPlan.add("plan", obj);

            jedis.rpush("python", objPlan.toString());
        }

        jedis = new Jedis(Commons.REDIS_HOST, Commons.REDIS_PORT);
        Gson gson = new Gson();
        for (int i = 0; i < numberOfGroups; i++) {
            List<String> element = jedis.blpop(0, "donePython");
            if(!element.get(1).contains("Completed")){
                System.out.println("A container failed" + element.get(1));
                return;
            }
            JsonObject response = JsonParser.parseString(element.get(1)).getAsJsonObject();
            System.out.println(element.get(1) + " - " +  response.get("resultFile"));
        }

    }

}
