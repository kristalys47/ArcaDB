package coordinator.plan;

import java.util.ArrayList;
import java.util.List;

import static coordinator.CommonVariables.*;

public class Catalog {
    public static List<String> filesForTable(String tableName){
        String path =  tableName ;
        List<String> files = jedis.lrange(path,0, -1);
        return files;
    }
}
