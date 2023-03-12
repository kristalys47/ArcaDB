package coordinator.plan;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static coordinator.Utils.Commons.REDIS_HOST;
import static coordinator.Utils.Commons.REDIS_PORT;

public class FilterTreeNode extends BinaryTreeNode {
    private String InnerRelation;
    private String OuterRelation;
    public ArrayList<String> OuterTableFiles;
    public ArrayList<String> InnerTableFiles;
    public String OuterColumnName;
    public String InnerColumnName;
    public int mode;

    public FilterTreeNode(JSONObject info, BinaryTreeNode parent, BinaryTreeNode inner, BinaryTreeNode outer, int bucket) {
        //TODO: send a query to catalog to get the files and everything
        super(NodeType.JOIN, parent, inner, outer, bucket);
//        if (info.has("Hash Cond")) {
//            String[] columns = info.getString("Hash Cond").replaceAll("\\(", "")
//                    .replaceAll("\\)", "").split(" = ");
//            String[] outCol = columns[0].split("\\.");
//            String[] inCol = columns[1].split("\\.");
//            this.OuterRelation = outCol[0];
//            this.InnerRelation = inCol[0];
//            this.OuterColumnName = outCol[1];
//            this.InnerColumnName = inCol[1];
//        }
        this.mode = mode;
    }

    public void run() {
    }
}