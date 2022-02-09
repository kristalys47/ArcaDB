package orc;

import static org.junit.Assert.assertEquals;

import org.testng.annotations.Test;

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
}
