package orc;

import static org.junit.Assert.assertEquals;

import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class mainTest {
    @Test
    public void testintInsertToOrc() throws Exception {
        String[] args = {"insert", "/JavaCode/insertedTest", "struct<id:int,name:string,last:string,score:float,isFemale:int>"};
        WorkerManager.dbms(args);
    }

    @Test
    public void testintReadInsertToOrc() throws Exception {
        String[] args = {"insertRead", "/JavaCode/insertedTest", "struct<id:int,name:string,last:string,score:float,isFemale:int>"};
        WorkerManager.dbms(args);
    }

    @Test
    public void testintProjectionToOrc() throws Exception {
        String[] args = {"", "/JavaCode/insertedTest", "last,isFemale", "((name=\"qftrjyivexdeikecdhbf\")|(id<3))"};
        WorkerManager.dbms(args);
    }

    @Test
    public void testintProjectionToOrc2() throws Exception {
        String test = "((name=\"qftrjyivexdeikecdhbf\")|(id<3))";
        String projection = "last,isFemale";
        ORCManager.reader("/JavaCode/insertedTest", projection, test);
    }

    @Test
    public void testingReadVisual() throws Exception {
        ORCManager.readerPrint("/JavaCode/insertedTest");
    }

    @Test
    public void testingRead() throws Exception {
        ORCManager.readerPrint("/JavaCode/finalResults");
    }

    @Test
    public void joinTest() throws Exception {
        JoinManager.readerPrint("/JavaCode/finalResults");
    }

    @Test
    public  void testConstructorHashJoin() throws FileNotFoundException {
        GRACEHashArray testObject = new GRACEHashArray(2, 340);
        testObject.addRecord(2, "Hello");
        testObject.addRecord(1, "Hello");
        testObject.addRecord(2, "Hello");
        testObject.addRecord(2, "Hello");
        testObject.addRecord(2, "Hello");
    }
    @Test
    public  void testReadHashJoin() throws FileNotFoundException {
        int bucket = 2;
        GRACEHashArray rTable = new GRACEHashArray(bucket, 340);
        rTable.addRecord(2, "175,Hello,Kristalys,Ruiz");
        rTable.addRecord(1, "456,Hello");
        rTable.addRecord(2, "1421,Hello");
        rTable.addRecord(2, "142,Hello");
        rTable.addRecord(2, "3325,Hello");
        rTable.flushRemainders();
        rTable.readRecords(0);
        rTable.readRecords(1);

        ExecutorService pool = Executors.newFixedThreadPool(10);

        for (int i = 0; i < bucket; i++) {
            Join tmp = new Join(rTable.readRecords(i), rTable.getFileBuckets(i));
            pool.execute(tmp);
        }



//        GRACEHashArray rTable = new GRACEHashArray(2, 340);
    }
}
