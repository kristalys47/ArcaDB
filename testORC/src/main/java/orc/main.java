package orc;


import org.apache.hadoop.hive.ql.exec.vector.VectorizedRowBatch;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URISyntaxException;

public class main {
    //insert /JavaCode/testORC struct<name:string,val:int> {\"values\":[[\"13\",\"4\"],[\"23\",\"3\"],[\"33\",\"10\"]]}
    //read /JavaCode/testORC name {"conditions": [["name", "0", "13"], ["val", "-1", "3"]]}
    static public void main(String[] arg) throws IOException, URISyntaxException, ParseException {


        VectorizedRowBatch row = new VectorizedRowBatch(2, 10);
        if(arg[0].equals("insert"))

            ORC.writer(row, arg[1], arg[2], arg[3]);
        else{
            for (int i = 0; i<arg.length; i++){
                System.out.println(arg[i]);
            }
            ORC.reader(arg[1], arg[2]);
        }
    }


}
