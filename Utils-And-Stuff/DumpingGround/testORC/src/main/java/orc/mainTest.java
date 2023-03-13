
import static org.junit.Assert.assertEquals;

import orc.helper.classes.GRACEHashArray;
import orc.helper.classes.Join;
import orc.helper.classes.TestingUtils;
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

        ExecutorService pool = Executors.newFixedThreadPool(10);

        for (int i = 0; i < bucket; i++) {
            Join tmp = new Join(rTable.readRecords(i), rTable.getFileBuckets(i));
            pool.execute(tmp);
        }

        pool.shutdown();


//        GRACEHashArray rTable = new GRACEHashArray(2, 340);
    }

    @Test
    public void testJoin() throws FileNotFoundException {
        int bucket = 2;
        GRACEHashArray rTable = new GRACEHashArray(bucket, 340);
        rTable.addRecord(175, "175,Hello,Kristalys,Ruiz,1757");
        rTable.addRecord(456, "456,Hello,4556");
        rTable.addRecord(1421, "1421,Hello,14221");
        rTable.addRecord(142, "142,Hello,1412");
        rTable.addRecord(3325, "3325,Hello,31125");
        rTable.addRecord(142, "142,Hello,1412");
        rTable.flushRemainders();

        ExecutorService pool = Executors.newFixedThreadPool(10);

        for (int i = 0; i < bucket; i++) {
            Join tmp = new Join(rTable.readRecords(i), rTable.getFileBuckets(i));
            pool.execute(tmp);
        }

        pool.shutdown();


//        GRACEHashArray rTable = new GRACEHashArray(2, 340);
    }

    @Test
    public void createfileAData() throws Exception {
        String fileA = "/tmp/tableA.json";
        TestingUtils.generateDataA(fileA);

        String[] argsA = {"insert", fileA, "/tmp/tableA.orc", "struct<id:int,name:string,last:string,score:float,isFemale:int>"};
        WorkerManager.dbms(argsA);
    }

    @Test
    public void createfileBData() throws Exception {
        String fileB = "/tmp/tableB.json";
        TestingUtils.generateDataB(fileB);

        String[] argsB = {"insert", fileB, "/tmp/tableB.orc", "struct<id:int,name:string,last:string,score:float,isFemale:int,fk:int>"};
        WorkerManager.dbms(argsB);
    }

    @Test
    public void joinWholeTest() throws Exception {
        // TODO: treat collisions
        JoinManager.join( "/tmp/tableA.orc", "id",  "/tmp/tableB.orc", "fk" );
    }

    @Test

    public void joinManagerTest() throws Exception {
        String[] argsA = {"join", "/tmp/tableA.orc", "id",  "/tmp/tableB.orc", "fk"};
        WorkerManager.dbms(argsA);
    }
}
