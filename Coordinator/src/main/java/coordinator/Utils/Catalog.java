package coordinator.Utils;

import alluxio.AlluxioURI;
import alluxio.client.file.FileInStream;
import alluxio.conf.PropertyKey;
import alluxio.exception.AlluxioException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import alluxio.client.file.FileSystem;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class Catalog {
    public static JSONObject getTableMetadata(String tableName){
        String path = tableName;
        alluxio.conf.Configuration.set(PropertyKey.MASTER_HOSTNAME, "136.145.77.107");
        alluxio.conf.Configuration.set(PropertyKey.SECURITY_LOGIN_USERNAME, "root");
        FileSystem fs = FileSystem.Factory.get();
        if (Commons.TABLES == null) {

            String alluxioPath = "alluxio://136.145.77.107:19998/tables.json";
            AlluxioURI pathAlluxio = new AlluxioURI(alluxioPath);
            FileInStream in = null;
            try {
                in = fs.openFile(pathAlluxio);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (AlluxioException e) {
                e.printStackTrace();
            }
            String jsonTxt = null;
            try {
                jsonTxt = IOUtils.toString(in, "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
            Commons.TABLES = new JSONObject(jsonTxt);
        }
        String table_path = ((JSONObject) Commons.TABLES.get("tables_dir")).getString("tableName");
        String alluxioPath = "alluxio://136.145.77.107:19998" + table_path + "/metadata.json";
        AlluxioURI pathAlluxio = new AlluxioURI(alluxioPath);
        FileInStream in = null;
        try {
            in = fs.openFile(pathAlluxio);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AlluxioException e) {
            e.printStackTrace();
        }
        String jsonTxt = null;
        try {
            jsonTxt = IOUtils.toString(in, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new JSONObject(jsonTxt);
    }
    public static List<String> filesForTable(JSONObject jsonObject){
        JSONArray list = jsonObject.getJSONArray("files");
        List<String> result = new ArrayList<String>();
        for (int i = 0; i < list.length(); i++) {
            result.add(list.getString(i));
        }
        return result;
    }

}
