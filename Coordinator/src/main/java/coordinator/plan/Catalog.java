package coordinator.plan;

import java.util.ArrayList;

public class Catalog {
    public static ArrayList<String> filesForTable(String tableName){
        ArrayList<String> files = new ArrayList<>();
        String path = "/tables/" + tableName + "/" + tableName + ".orc";
        files.add(path);
        return files;
    }
}
