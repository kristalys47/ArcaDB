package orc;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import redis.clients.jedis.Jedis;

import java.io.FileReader;

import static orc.Commons.*;
import static orc.Commons.REDIS_PORT;

public class WorkerManager {

    static boolean dbms(JSONObject arg)  throws Exception {

        //TODO: check that folder does exist
        //TODO: check that if empty it projects all the columns
        //TODO: CHECK INSERT
        Jedis jedisControl = newJedisConnection(REDIS_HOST, REDIS_PORT);
        switch (arg.get("planType").toString()){
            case "insert":
//                String data = "";
//                JSONParser parser = new JSONParser();
//                try {
//                    System.out.println(arg[1]);
//                    Object obj = parser.parse(new FileReader(arg[1]));
//                    JSONObject jsonObject = (JSONObject)obj;
//                    data = jsonObject.toJSONString();
//
//                } catch(Exception e) {
//                    e.printStackTrace();
//                }
//                ORCManager.writer(arg[2], arg[3], data);
                break;
            case "insertRead":
//                String data = "";
//                JSONParser parser = new JSONParser();
//                try {
//                    System.out.println(arg[1]);
//                    Object obj = parser.parse(new FileReader(arg[1]));
//                    JSONObject jsonObject = (JSONObject)obj;
//                    data = jsonObject.toJSONString();
//
//                } catch(Exception e) {
//                    e.printStackTrace();
//                }
//                ORCManager.writer(arg[2], arg[3], data);
//                ORCManager.readerPrint(arg[1]);
                break;
            case "inference":
                FileAttributesManager.semistructuredID(arg.getJSONArray("result"), arg.getInt("buckets"), arg.getString("relation"));
                jedisControl.rpush("semistructuredDONE", ip + "\nSuccessful: " + arg);
            case "joinPartition":
                String path = "/" + arg.getString("relation") + "/" + arg.getJSONArray("relation").getString(0);
                String column = arg.getString("joinColumn");
                String relation = arg.getString("relation");
                String buckets = arg.getString("buckets");
                JoinManager.joinPartition(path, column, relation, buckets);
                jedisControl.rpush("done", ip + "\nSuccessful: " + arg);
                break;
            case "joinProbing":
                String outer = "/" + arg.get("outer").toString() + "/" + ((JSONArray) arg.get("relation")).get(0).toString();
                String inner = arg.get("inner").toString();
                JoinManager.joinProbing(outer, inner);
                jedisControl.rpush("done", ip + "\nSuccessful: " + arg);
            default:
//                return ORCManager.reader(arg[1], arg[2], arg[3], arg[4]);
        }
        return true;
    }
}
