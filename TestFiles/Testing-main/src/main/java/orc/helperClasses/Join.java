package orc.helperClasses;

import java.io.*;
import java.util.LinkedList;
import java.util.Map;

public class Join implements Runnable{
    Map<String, HashNode<String>> R;
    LinkedList<File> S;
    String jsonFile;


    public Join(Map<String, HashNode<String>> R, LinkedList<File> S, String jsonFile){
        this.R = R;
        this.S = S;
        this.jsonFile = jsonFile;
    }

    @Override
    public void run() {
        FileWriter fr = null;
        try {
            fr = new FileWriter(this.jsonFile + ".json");
            fr.write("{\"values\":[");
            boolean first = true;

            for (int i = 0; i < S.size(); i++) {
                FileInputStream reader = new FileInputStream(S.get(i));
                java.util.Scanner scanner = new java.util.Scanner(reader).useDelimiter("\n");
                String theString = null;

                while (scanner.hasNext()) {
                    theString = scanner.next();
                    String[] read = theString.split(",", 2);
                    if(R.containsKey(read[0])){
                        HashNode<String> current = R.get(read[0]);
                        do {
                            //TODO: Save them correctly on a file.
                            if(!first){
                                fr.write(",\n");
                            } else{
                                first = false;
                            }
                            fr.write("[" + read[1] +  "," + current.getElement() + "]");
                            current = current.getNext();
                        }while(current != null);
                    }
                }
                scanner.close();

            }
            fr.write("]}");
            fr.flush();
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
