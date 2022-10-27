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
        maincorridas(arg);
    }

    static public void mainreg(String[] arg) throws IOException {

        String inputFile = "logs.log";
        String outputFile = "output.txt";
        String id = "c60-b60-n10-c_n6-al";
        int reps = 3;
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
        FileWriter fileWriter2 = new FileWriter(id + ".backup");
        PrintWriter out = new PrintWriter(fileWriter);
        PrintWriter out2 = new PrintWriter(fileWriter2);
        for (String outputLine : lineList) {
            out.println(outputLine);
            out2.println(outputLine);

        }
        out.flush();
        out.close();
        fileWriter.close();
        out2.flush();
        out2.close();
        fileWriter2.close();

        List<List<String>> results = new ArrayList<List<String>>(reps);

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

        for (String s : results.get(0)) {
            System.out.println(s);
        }

        for (int j = 0; j <reps; j++) {
//            String finalOutput = "results-c30-b30-n6-c_n5-v" + (j+1) + "-alluxio.csv";
            String finalOutput = id + (j+1) + ".csv";
            OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(finalOutput), "UTF-8");
            BufferedWriter bufWriter = new BufferedWriter(writer);
            Map<String, String> maps = new HashMap<>();
            boolean probing = true;

            for (int i = 0; i < results.get(j).size(); i++) {
                String str = results.get(j).get(i);
                String[] strarr = str.split(" ");
                if (str.contains("RESPONSE TIME")) {
                    System.out.println(id+" " +j);
                    System.out.println("Respose time " + j + " : " + str );
                }
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
    static public void maincorridas(String[] arg) throws IOException {

        String inputFile = "logs.log";
        String outputFile = "output.txt";
        String id = "mej";
        int reps = 6;
        FileReader fileReader = new FileReader(inputFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String inputLine;
        List<String> lineList = new ArrayList<String>();
        while ((inputLine = bufferedReader.readLine()) != null) {
//            if ((inputLine.contains("TIME_LOG") && !inputLine.contains("RESPONSE TIME")) || inputLine.contains("Opened database")) {
            if (inputLine.contains("TIME_LOG") || inputLine.contains("Opened database")) {
                lineList.add(inputLine);
            }
        }
        fileReader.close();

        Collections.sort(lineList);

        FileWriter fileWriter = new FileWriter(outputFile + ".backup");
        PrintWriter out = new PrintWriter(fileWriter);
        for (String outputLine : lineList) {
            out.println(outputLine);

        }
        out.flush();
        out.close();
        fileWriter.close();


//        int [] numbers_of_lines = new int[reps];
//        int start = 70;
//        for (int i = 0; i < numbers_of_lines.length; i++) {
//            numbers_of_lines[i] = (start+10*i + 132 + 3) * 3;
//
//        }

        List<List<String>> results = new ArrayList<List<String>>(reps*3);

        for (int k = 0; k < reps*3; k++) {
            results.add(new ArrayList<>());
        }

//        int size = lineList.size();
//        int index = 0;
//        for (int i = 0; i < numbers_of_lines.length; i++) {
//            for (int j = 0; j < 3; j++) {
//                System.out.println(i + " " + numbers_of_lines[i] + " " + j);
//                for (int k = 0; k < numbers_of_lines[i]; k++) {
//                    results.get(index).add(lineList.remove(0).split("TIME_LOG: ")[1]);
//                }
//                index++;
//            }
//        }
        int index=-1;
        do{
            String line = lineList.remove(0);
            if(line.contains("Opened")){
                index++;
            } else {
                line = line.split("TIME_LOG: ")[1];
                results.get(index).add(line);
            }
        }while(lineList.size()>0);


        int start = 120;
        int index2 = -1;
        for (int j = 0; j <reps; j++) {
//            String finalOutput = "results-c30-b30-n6-c_n5-v" + (j+1) + "-alluxio.csv";
            for (int k = 0; k < 3; k++) {

//                start + 60 * j + " " + (k + 1) +
                String finalOutput =  j+ " "+ k + ".csv";
                OutputStreamWriter writer = new OutputStreamWriter(
                        new FileOutputStream(finalOutput), "UTF-8");
                BufferedWriter bufWriter = new BufferedWriter(writer);
                Map<String, String> maps = new HashMap<>();
                boolean probing = true;

                index2++;
                for (int i = 0; i < results.get(index2).size(); i++) {
                    String str = results.get(index2).get(i);
                    String[] strarr = str.split(" ");
                    if (str.contains("RESPONSE TIME")) {
                        System.out.println("Respose time " + finalOutput + " " + str);
                    }
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

}
