package orc;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;

public class WorkerManager {
    static boolean dbms(String[] arg)  throws Exception {

        //TODO: check that folder does exist
        //TODO: check that if empty it projects all the columns

        for (String s : arg) {
            System.out.println(s);
        }
        System.out.println("worked manager class");
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
        else if(arg[0].equals("join")){
            JoinManager.join( arg[1], arg[2], arg[3], arg[4], arg[5]);
        }
        else if (arg[0].equals("joinPartition")) {
            System.out.println("calls function");
            JoinManager.joinPartition(arg[1], arg[2], arg[3], arg[4]);
        }
        else if (arg[0].equals("joinProbing")) {
            JoinManager.joinProbing(arg[1], arg[2], arg[3]);
        }
        else{
            return ORCManager.reader(arg[1], arg[2], arg[3], arg[4]);
        }
        return true;
    }
}
