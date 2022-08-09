package coordinator.plan;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.json.JSONObject;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.sql.Statement;
import java.util.ArrayList;

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

    public ParallelHashJoinBinaryTreeNode(JSONObject info, Statement cursor, BinaryTreeNode parent, BinaryTreeNode inner, BinaryTreeNode outer, int mode) {
        //TODO: send a query to catalog to get the files and everything
        super(NodeType.PARALLELJOIN, parent, inner, outer);
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


    public void execute(){
        //TODO: What if the join is in the same table?
        if (isSimpleScan(this.outer) && isSimpleScan(this.outer)) {
            //initiante patition
            int buckets = 5;
            ScanBinaryTreeNode relationA = (ScanBinaryTreeNode) this.outer;
            ScanBinaryTreeNode relationB = (ScanBinaryTreeNode) this.inner;
            ContainerManager[] threads = new ContainerManager[relationA.TableFiles.size() + relationB.TableFiles.size()];
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(10);
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
                    array.add(buckets);
                    JsonObject obj = new JsonObject();
                    obj.add("plan", array);
                    threads[tindex] = new ContainerManager(obj.toString(), "worker", jedisPool);
                    threads[tindex].start();
                    tindex++;
                }
                if (i < relationB.TableFiles.size()) {
                    JsonArray array = new JsonArray();
                    array.add("joinPartition");
                    array.add(relationB.TableFiles.get(i));
                    array.add(this.InnerColumnName);
                    array.add(this.InnerRelation);
                    array.add(buckets);
                    JsonObject obj = new JsonObject();
                    obj.add("plan", array);
                    threads[tindex] = new ContainerManager(obj.toString(), "worker", jedisPool);
                    threads[tindex].start();
                    tindex++;
                }
                i++;
            }

            for (int i1 = 0; i1 < threads.length; i1++) {
                //TODO: Make into a thread pool?
                try {
                        threads[i1].join();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("PROBING STAGE--------------");

            ContainerManager[] probing = new ContainerManager[buckets];
            for (int i1 = 0; i1 < probing.length; i1++) {
                JsonArray array = new JsonArray();
                if(this.mode == 2){
                    array.add("joinProbing2");
                } else{
                    array.add("joinProbing3");
                }
                //TODO: choose who is inner and who is outer
                array.add("/join/" + i1 + "/" + InnerRelation + "/");
                array.add("/join/" + i1 + "/" + OuterRelation + "/");
                array.add(i1);
                JsonObject obj = new JsonObject();
                obj.add("plan", array);
                probing[i1] = new ContainerManager(obj.toString(), "worker", jedisPool);
                probing[i1].start();
            }

            for (int i1 = 0; i1 < probing.length; i1++) {
                try {
                        probing[i1].join();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } else {
            System.out.println("This case has not been set yet because the leaves are not directly " +
                    "connected to both the relations");
        }

    }
}
