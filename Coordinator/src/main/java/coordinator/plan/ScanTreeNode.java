package coordinator.plan;

import coordinator.Utils.Commons;
import coordinator.Utils.Catalog;
import org.json.*;
import redis.clients.jedis.Jedis;

import java.sql.Statement;
import java.util.List;

import static coordinator.Utils.Commons.*;
import static coordinator.Utils.Commons.REDIS_PORT;


public class ScanTreeNode extends BinaryTreeNode {
    public List<String> TableFiles;
    public String selection = "";
    public String projection = "";
    public String alias = "";
    public JSONObject jsonObject = null;
    public List<String> ResultFiles;
    public DataType typeData;
    public String relation = "";


    public ScanTreeNode(JSONObject info, Statement cursor, BinaryTreeNode parent, BinaryTreeNode inner, BinaryTreeNode outer, int buckets) {
        //TODO: send a query to catalog to get the files and everything
        //TODO: Check bucket stuff (Create it for the parent class special constructor. Overload it)
        super(NodeType.SCAN, parent, inner, outer, buckets);
        //TODO: you can get the relation from here to send to the hash join make it a variable;
        if (info.has("Relation Name")) {
            this.relation = info.getString("Relation Name");
            this.jsonObject = Catalog.getTableMetadata(this.relation);
            this.TableFiles = Catalog.filesForTable(this.jsonObject);
        }
        switch (this.jsonObject.getString("type")) {
            case "structured":
                this.typeData = DataType.STRUCTURED;
                break;
            case "semistructured":
                this.typeData = DataType.SEMISTRUCTURED;
                break;
            default:
                this.typeData = DataType.UNSTRUCTURED;
        }

        if (info.has("Alias"))
            this.alias = info.getString("Alias");
        if (info.has("Recheck Cond"))
            this.selection = transformSelection(info.getString("Recheck Cond"));
        if (info.has("Filter"))
            this.selection = transformSelection(info.getString("Filter"));


    }

    public String transformSelection(String conditions) {
        String cleaned = conditions.replaceAll(" ", "").replaceAll("\\)AND\\(", "\\)&\\(")
                .replaceAll("\\)OR\\(", "\\)|\\(").replaceAll("::numeric", "").replaceAll("::text", "")
                .replaceAll("\\'", "");
        return cleaned;
    }

    public int scanScheduleStructured(String column, String relationName){
        Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT);
        for (int i = 0; i < this.TableFiles.size(); i++) {
            JSONObject planJAVA = new JSONObject();
            planJAVA.put("planType", "scan");
            planJAVA.put("files", new JSONArray().put(this.TableFiles.get(i)));
            planJAVA.put("relation", relationName);
            planJAVA.put("filter", this.selection);
            jedis.rpush("structured", planJAVA.toString());
        }
        return this.TableFiles.size();
    }

    public int scanScheduleSemistructured(String column, String relationName){
        List<String> files = this.TableFiles;
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
            planJAVA.put("relation", relationName);
            planJAVA.put("filter", this.selection);
            jedis.rpush("semistructured", planJAVA.toString());
        }
        return numberOfGroups;
    }

    @Override
    public void run() {
        Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT);
        if (this.typeData == DataType.STRUCTURED) {
            for (int i = 0; i < this.TableFiles.size(); ++i) {
                JSONObject planJAVA = new JSONObject();
                planJAVA.append("planType", "scan");
                planJAVA.append("files", new JSONArray().put(this.TableFiles.get(i)));
                planJAVA.append("filter", this.selection);
                planJAVA.append("project", this.projection);
                planJAVA.append("relation", this.relation);
                //TODO: make request for resources
                jedis.rpush("task", planJAVA.toString());
            }

            for (int i = 0; i < this.TableFiles.size(); i++) {
                List<String> element = jedis.blpop(0, "done");
                if (!element.get(1).contains("Successful")) {
                    System.out.println("A container failed" + element.get(1));
                    return;
                }
                System.out.println(element.get(1));
            }

            this.setDone(true);
            //resquest for a node to perform this
            //Assign The files from the catalog
        }
        if(this.typeData == DataType.SEMISTRUCTURED){
            List<String> files = this.TableFiles;
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
            jedis = new Jedis(Commons.REDIS_HOST, Commons.REDIS_PORT);
            for (int i = 0; i < partitions.length; i++) {
                JSONObject planJAVA = new JSONObject();
                planJAVA.put("planType", "inference");
                planJAVA.put("files", partitions[i]);
                planJAVA.put("relation", relation);
                planJAVA.put("filter", this.selection);
                System.out.println(planJAVA.toString());
                jedis.rpush("semistructured", planJAVA.toString());
            }
            for (int i1 = 0; i1 < numberOfGroups; i1++) {
                List<String> element = jedis.blpop(0, "done");
                if(!element.get(1).contains("Successful")){
                    System.out.println("A container failed" + element.get(1));
                    return;
                }
                System.out.println(element.get(1));
            }
        }


    }
}
