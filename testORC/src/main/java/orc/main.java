package orc;

import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;


public class main {
    //insert /JavaCode/testORC struct<name:byte,val:byte> {\"values\":[[\"Kristal\",\"3\"],[\"al\",\"4\"],[\"bob\",\"17\"],[\"Bi\",\"34\"],[\"col\",\"6\"],[\"Jil\",\"4\"],[\"sam\",\"3\"],[\"Dead\",\"0\"]]}
    //read /JavaCode/testORC name {"conditions": [["name", "0", "13"], ["val", "-1", "3"]]}
    static public void main(String[] arg) throws Exception {




        if(arg[0].equals("insert"))

            ORC.writer(arg[1], arg[2], arg[3]);
        else{
            for (int i = 0; i<arg.length; i++){
                System.out.println(arg[i]);
            }
            ORC.reader(arg[1], arg[2]);
            ORC.readerPrint("/JavaCode/results0");
        }

    }


}
