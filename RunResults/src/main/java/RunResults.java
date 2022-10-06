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

    static public void main(String[] arg) throws IOException {

        String inputFile = "logs.log";
        String outputFile = "output.txt";

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

        List<List<String>> results = new ArrayList<List<String>>(3);
        int reps = 3;
        for (int k = 0; k < reps; k++) {
            results.add(new ArrayList<>());
        }

        int size = lineList.size();
        int parts = size/reps;
        int index = 0;
        int count = 0;
        while (index<reps) {

            if(count == parts) {
                count = 0;
                index++;
                if(index == reps){
                    break;
                }
                System.out.println(lineList.size());
            }
            results.get(index).add(lineList.remove(0).split("TIME_LOG: ")[1]);
            count++;

        }

        for (String s : results.get(1)) {
            System.out.println(s);
        }


        for (int j = 0; j <reps; j++) {
            String finalOutput = "results-c25-b25-n6-c_n5-v" + (j+1) + ".csv";
            OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(finalOutput), "UTF-8");
            BufferedWriter bufWriter = new BufferedWriter(writer);
            Map<String, String> maps = new HashMap<>();
            boolean probing = true;
            for (int i = 0; i < results.get(j).size(); i++) {
                String str = results.get(j).get(i);
                String[] strarr = str.split(" ");
                if (str.contains("Partition (Read File)")) {
                    maps.put(strarr[3], strarr[6]);
                } else if (str.contains("Partition (Tuples to Buckets)")) {
                    maps.put(strarr[4], maps.get(strarr[4]) + "," + strarr[7]);
                } else if (str.contains("Container")) {
                    bufWriter.write(maps.get(strarr[1]) + "," + strarr[4] + "\n");
                    maps.remove(strarr[1]);
                } else if (str.contains("Probing (Create Hash)")) {
                    if (probing) {
                        probing = false;
                        bufWriter.write("-Probing-" + "\n");
                    }
                    maps.put(strarr[3], strarr[6]);
                } else if (str.contains("Probing (Join)")) {
                    maps.put(strarr[2], maps.get(strarr[2]) + "," + strarr[5]);
                }
            }
            bufWriter.flush();
            bufWriter.close();
        }
    }

}
