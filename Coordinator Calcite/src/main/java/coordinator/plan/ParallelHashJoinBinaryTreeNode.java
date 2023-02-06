package coordinator.plan;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static coordinator.Commons.REDIS_HOST;
import static coordinator.Commons.REDIS_PORT;

public class ParallelHashJoinBinaryTreeNode extends BinaryTreeNode {
    private String InnerRelation;
    private String OuterRelation;
    public ArrayList<String> OuterTableFiles;
    public ArrayList<String> InnerTableFiles;
    public String OuterColumnName;
    public String InnerColumnName;
    public int mode;

    public ParallelHashJoinBinaryTreeNode(JSONObject info, Statement cursor, BinaryTreeNode parent, BinaryTreeNode inner, BinaryTreeNode outer, int mode, int bucket) {
        //TODO: send a query to catalog to get the files and everything
        super(NodeType.PARALLELJOIN, parent, inner, outer, bucket);
        if (info.has("Hash Cond")) {
            String[] columns = info.getString("Hash Cond").replaceAll("\\(", "")
                    .replaceAll("\\)", "").split(" = ");
            String[] outCol = columns[0].split("\\.");
            String[] inCol = columns[1].split("\\.");
            this.OuterRelation = outCol[0];
            this.InnerRelation = inCol[0];
            this.OuterColumnName = outCol[1];
            this.InnerColumnName = inCol[1];
        }
        this.mode = mode;
    }

    public void executeWithQueue() {
        //TODO: What if the join is in the same table?
        if (isSimpleScan(this.outer) && isSimpleScan(this.outer)) {
            //initiante patition
            ScanBinaryTreeNode relationA = (ScanBinaryTreeNode) this.outer;
            ScanBinaryTreeNode relationB = (ScanBinaryTreeNode) this.inner;
            Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT);
            int i = 0;
            int tindex = 0;
            while (i < relationA.TableFiles.size() || i < relationB.TableFiles.size()) {
                if (i < relationA.TableFiles.size()) {
                    JsonArray array = new JsonArray();
                    if(this.mode == 2){
                        array.add("joinPartition2");
                    } else{
                        array.add("joinPartition3");
                    }
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
                    if(this.mode == 2){
                        array.add("joinPartition2");
                    } else{
                        array.add("joinPartition3");
                    }
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

            ExecutorService threadPoolProbing = Executors.newWorkStealingPool(this.buckets);
            for (int i1 = 0; i1 < this.buckets; i1++) {
                JsonArray array = new JsonArray();
                if(this.mode == 2){
                    array.add("joinProbing2");
                } else{
                    array.add("joinProbing3");
                }
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
    public void execute(){
        //TODO: What if the join is in the same table?
        if (isSimpleScan(this.outer) && isSimpleScan(this.outer)) {
            //initiante patition
            ScanBinaryTreeNode relationA = (ScanBinaryTreeNode) this.outer;
            ScanBinaryTreeNode relationB = (ScanBinaryTreeNode) this.inner;
            ExecutorService threadPool = Executors.newWorkStealingPool(relationA.TableFiles.size() + relationB.TableFiles.size());
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            JedisPool jedisPool = new JedisPool(poolConfig, REDIS_HOST, REDIS_PORT);
            int i = 0;
            int tindex = 0;
            while (i < relationA.TableFiles.size() || i < relationB.TableFiles.size()) {
                if (i < relationA.TableFiles.size()) {
                    JsonArray array = new JsonArray();
                    if(this.mode == 2){
                        array.add("joinPartition2");
                    } else{
                        array.add("joinPartition3");
                    }
                    array.add(relationA.TableFiles.get(i));
                    array.add(this.OuterColumnName);
                    array.add(this.OuterRelation);
                    array.add(this.buckets);
                    JsonObject obj = new JsonObject();
                    obj.add("plan", array);
                    threadPool.execute( new ContainerManager(obj.toString(), "worker", jedisPool));
                }
                if (i < relationB.TableFiles.size()) {
                    JsonArray array = new JsonArray();
                    if(this.mode == 2){
                        array.add("joinPartition2");
                    } else{
                        array.add("joinPartition3");
                    }
                    array.add(relationB.TableFiles.get(i));
                    array.add(this.InnerColumnName);
                    array.add(this.InnerRelation);
                    array.add(this.buckets);
                    JsonObject obj = new JsonObject();
                    obj.add("plan", array);
                    threadPool.execute( new ContainerManager(obj.toString(), "worker", jedisPool));
                    tindex++;
                }
                i++;
            }

            threadPool.shutdown();
            while(!threadPool.isTerminated());

            System.out.println("PROBING STAGE--------------");

            ExecutorService threadPoolProbing = Executors.newWorkStealingPool(this.buckets);
            for (int i1 = 0; i1 < this.buckets; i1++) {
                JsonArray array = new JsonArray();
                if(this.mode == 2){
                    array.add("joinProbing2");
                } else{
                    array.add("joinProbing3");
                }
                //TODO: choose who is inner and who is outer
                array.add("/join/" + i1 + "/" + OuterRelation + "/");
                array.add("/join/" + i1 + "/" + InnerRelation + "/");
                array.add(i1);
                JsonObject obj = new JsonObject();
                obj.add("plan", array);
                threadPoolProbing.execute(new ContainerManager(obj.toString(), "worker", jedisPool));
            }

            threadPoolProbing.shutdown();
            while(!threadPoolProbing.isTerminated());

        } else {
            System.out.println("This case has not been set yet because the leaves are not directly " +
                    "connected to both the relations");
        }

    }
}
