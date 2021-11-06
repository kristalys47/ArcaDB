package orc;


public class main {
    //insert /JavaCode/testORC struct<name:byte,val:byte> {\"values\":[[\"Kristal\",\"3\"],[\"al\",\"4\"],[\"bob\",\"17\"],[\"Bi\",\"34\"],[\"col\",\"6\"],[\"Jil\",\"4\"],[\"sam\",\"3\"],[\"Dead\",\"0\"]]}
    //read /JavaCode/testORC name (((name="Kristal")|(val<-10))&(val>0))
    static public void main(String[] arg) throws Exception {

//        String test = "(((name=\"Kristal\")|((name=\"b\")|(val<10)))&(val>0))";
        String test = "(((name=\"Kristal\")|(val<-10))&(val>0))";
//        String test = "(val<11)";


        if(arg[0].equals("insert"))

            ORCManager.writer(arg[1], arg[2], arg[3]);
        else{
            for (int i = 0; i<arg.length; i++){
                System.out.println(arg[i]);
            }
            ORCManager.reader(arg[1], arg[2], test);
            ORCManager.readerPrint("/JavaCode/results0");
        }

    }


}
