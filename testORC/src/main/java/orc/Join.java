package orc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Map;

public class Join implements Runnable{
    Map<String, Node<String>> R;
    LinkedList<File> S;

    public Join(Map<String, Node<String>> R, LinkedList<File> S){
        this.R = R;
        this.S = S;
    }

    @Override
    public void run() {
        for (int i = 0; i < S.size(); i++) {
            try {
                FileInputStream reader = new FileInputStream(S.get(i));
                java.util.Scanner scanner = new java.util.Scanner(reader, "UTF-8").useDelimiter("\n");
                String theString = null;

                while (scanner.hasNext()) {
                    theString = scanner.next();
                    System.out.println(theString);
                    String[] read = theString.split(",", 2);
                    if(R.containsKey(read[0])){
                        Node<String> current = R.get(read[0]);
                        do {
                            //TODO: Save them correctly on a file.
                            System.out.println(read[1] + current.getElement());
                            current = current.getNext();

                        }while(current != null);
                    }
                }
                scanner.close();

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }
}
