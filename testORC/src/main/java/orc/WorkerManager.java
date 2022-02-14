package orc;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;

public class WorkerManager {
    static void dbms(String[] arg)  throws Exception {

        if(arg[0].indexOf("insert")>-1) {
            String data = "";
            JSONParser parser = new JSONParser();
            try {
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
        else{
            ORCManager.reader(arg[1], arg[2], arg[3]);
        }

    }
}
