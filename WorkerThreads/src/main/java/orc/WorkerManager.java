package orc;

import com.google.gson.JsonObject;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.io.IOException;

public class WorkerManager {
    static boolean dbms(String[] arg, JsonObject plan) throws IOException {
        if(arg[0].equals("join1")){
            JoinManager.join(plan.getAsJsonArray("outer"), arg[1], arg[2], plan.getAsJsonArray("inner"), arg[3], arg[4], arg[5], 3);
        }
        return true;
    }

    static boolean dbms(String[] arg)  throws Exception {

        //TODO: check that folder does exist
        //TODO: check that if empty it projects all the columns
        if(arg[0].indexOf("insert")>-1) {
            String data = "";
            JSONParser parser = new JSONParser();
            try {
                System.out.println(arg[1]);
                Object obj = parser.parse(new FileReader(arg[1]));
                JSONObject jsonObject = (JSONObject)obj;
                data = jsonObject.toJSONString();

            } catch(Exception e) {
                e.printStackTrace();
            }
            ORCManager.writer(arg[2], arg[3], data);
            if(arg[0].equals("insertRead"))
                ORCManager.readerPrint(arg[1]);
        }
        else if(arg[0].equals("joinPartition2")){
            JoinManager.joinPartition( arg[1], arg[2], arg[3], arg[4], 2);
        }
        else if(arg[0].equals("joinProbing2")){
            JoinManager.joinProbing( arg[1], arg[2], arg[3], 2);
        }
        else if (arg[0].equals("joinPartition3")) {
//            System.out.println("calls function");
            JoinManager.joinPartition(arg[1], arg[2], arg[3], arg[4], 3);
        }
        else if (arg[0].equals("joinProbing3")) {
            JoinManager.joinProbing(arg[1], arg[2], arg[3], 3);
        }
        else{
            return ORCManager.reader(arg[1], arg[2], arg[3], arg[4]);
        }
        return true;
    }
}
