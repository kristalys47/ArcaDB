package orc;


import org.apache.hadoop.hive.ql.exec.vector.VectorizedRowBatch;
import org.apache.orc.OrcFilterContext;
import org.apache.orc.impl.OrcFilterContextImpl;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Stack;
import java.util.function.Consumer;

public class main {
    //insert /JavaCode/testORC struct<name:string,val:int> {\"values\":[[\"Kristal\",\"3\"],[\"al\",\"4\"],[\"bob\",\"17\"],[\"Bi\",\"34\"],[\"col\",\"6\"],[\"Jil\",\"4\"],[\"sam\",\"3\"],[\"Dead\",\"0\"]]}
    //read /JavaCode/testORC name {"conditions": [["name", "0", "13"], ["val", "-1", "3"]]}
    static public void main(String[] arg) throws IOException, URISyntaxException, ParseException {


//        VectorizedRowBatch row = new VectorizedRowBatch(2, 10);
//        if(arg[0].equals("insert"))
//
//            ORC.writer(row, arg[1], arg[2], arg[3]);
//        else{
//            for (int i = 0; i<arg.length; i++){
//                System.out.println(arg[i]);
//            }
//            ORC.reader(arg[1], arg[2]);
//            ORC.readerPrint("/JavaCode/results0");
//        }


        ORCProjection mmm = new ORCProjection();
        Consumer<OrcFilterContext> ll = mmm.builder("mmmm");




    }


}
