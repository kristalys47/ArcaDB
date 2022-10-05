import io.restassured.*;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import jdk.jfr.ContentType;
import redis.clients.jedis.Jedis;

import java.io.*;

import com.opencsv.CSVWriter;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class RunResults {
    static public final String DBMS = "136.145.77.80";
    static public final String REDIS_HOST_TIMES = "136.145.77.83"; //redis
    static public final int REDIS_PORT_TIMES = 6380;
    static public final int REDIS_PORT = 6379;

    static public void main(String[] arg) throws IOException {

        String inputFile = "t.txt";
        String outputFile = "output.txt";
        String finalOutput = "results-c20-b20-n6-c_n5" + ".csv";

        FileReader fileReader = new FileReader(inputFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String inputLine;
        List<String> lineList = new ArrayList<String>();
        while ((inputLine = bufferedReader.readLine()) != null) {
            if (inputLine.contains("TIME_LOG")) {
                lineList.add(inputLine);
            }
        }
        fileReader.close();

        Collections.sort(lineList);

        FileWriter fileWriter = new FileWriter(outputFile);
        PrintWriter out = new PrintWriter(fileWriter);
        for (String outputLine : lineList) {
            out.println(outputLine);
        }
        out.flush();
        out.close();
        fileWriter.close();


//        Jedis jedisTime = new Jedis(REDIS_HOST_TIMES, REDIS_PORT_TIMES);
//
        OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(finalOutput), "UTF-8");
//

        File file = new File(outputFile);    //creates a new file instance
        FileReader fr = new FileReader(file);   //reads the file
        BufferedReader br = new BufferedReader(fr);  //creates a buffering character input stream
        List<String> results = new ArrayList<>();    //constructs a string buffer with no characters
        String line;
        boolean first = true;
        while ((line = br.readLine()) != null) {
            results.add(line.split("TIME_LOG: ")[1]);

        }
        for (String s : results) {
            System.out.println(s);
        }


        BufferedWriter bufWriter = new BufferedWriter(writer);
//        List<String> results = jedisTime.lrange("times", 0 , -1);
        Map<String, String> maps = new HashMap<>();
        boolean probing = true;
        for (int i = 0; i < results.size(); i++) {
            String str = results.get(i);
            String[] strarr = str.split(" ");
            if(str.contains("Partition (Read File)")){
                maps.put(strarr[3], strarr[6]);
            } else if (str.contains("Partition (Tuples to Buckets)")){
                maps.put(strarr[4], maps.get(strarr[4]) + "," + strarr[7]);
            } else if (str.contains("Container")){
                bufWriter.write(maps.get(strarr[1]) + "," + strarr[4] + "\n");
                maps.remove(strarr[1]);
            } else if (str.contains("Probing (Create Hash)")){
                if(probing){
                    probing = false;
                    bufWriter.write("-Probing-" + "\n");
                }
                maps.put(strarr[3], strarr[6]);
            } else if (str.contains("Probing (Join)")){
                maps.put(strarr[2], maps.get(strarr[2]) + "," + strarr[5]);
            }
        }
        bufWriter.flush();
        bufWriter.close();
    }

}
