package orc;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import orc.helperClasses.*;
import orc.helperClasses.GRACEHashArray;
import orc.helperClasses.TestingUtils;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.binary.BinaryBasicNameMapper;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.BinaryConfiguration;
import org.apache.ignite.configuration.ClientConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.apache.orc.TypeDescription;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static orc.SharedConnections.*;

public class UTests {
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
        ORCManager.reader("/JavaCode/insertedTest", test, projection, "/JavaCode/finalResults");
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

//        for (int i = 0; i < bucket; i++) {
//            Join tmp = new Join(rTable.readRecords(i), rTable.getFileBuckets(i), "/tmp/testing"+i);
//            pool.execute(tmp);
//        }

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

//        for (int i = 0; i < bucket; i++) {
//            Join tmp = new Join(rTable.readRecords(i), rTable.getFileBuckets(i), "/tmp/testing"+i);
//            pool.execute(tmp);
//        }

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
        String fileB = "/tmp/data.json";
        TestingUtils.generateDataB(fileB);

        String[] argsB = {"insert", fileB, "/tmp/tableB.orc", "struct<id:int,name:string,last:string,score:float,isFemale:int,fk:int>"};
        WorkerManager.dbms(argsB);
    }

    @Test
    public void testingReadA() throws Exception {
        ORCManager.readerPrint("/tmp/tableB.orc");
    }

    @Test
    public void joinWholeTest() throws Exception {
        // TODO: treat collisions
        JoinManager.join( "/tmp/tableA.orc", "id",  "/tmp/tableB.orc", "fk" , "/tmp/results/joinresult.json");
    }

    @Test
    public void joinManagerTest() throws Exception {
        String[] argsA = {"join", "/tmp/tableA.orc", "id",  "/tmp/tableB.orc", "fk", "/tmp/results/joinresult.json"};
        WorkerManager.dbms(argsA);
    }

    @Test
    public void joinNFS() throws Exception {
        String[] argsA = {"join", "/nfs/QUERY_RESULTS/21036271910.temporc", "productid",  "/nfs/QUERY_RESULTS/3039440750.temporc", "id", "/nfs/testing.temporc"};
        WorkerManager.dbms(argsA);
    }


    @Test
    public void addingForLocalTest() throws Exception {
        JsonArray garray = new JsonArray();
        garray.add("insert");
        garray.add("/home/kristalys/git/Container-DBMS/data.json");
        garray.add("/home/kristalys/mytable/hello.orc");
        garray.add("struct<id:int,name:string,email:string,address:string>");

        String[] args = new String[garray.size()];

        for (int i = 0; i < args.length; i++) {
            args[i] = garray.get(i).getAsString();
            System.out.println(args[i]);
        }

        WorkerManager.dbms(args);
    }
    @Test
    public void testSingleFilterSelection() throws Exception {
        String schemaString = "struct<id:int,name:string,email:string,address:string>";
        TypeDescription schema = TypeDescription.fromString(schemaString);
        String selection = "(id>20)";

        ProjectionTree op = new ProjectionTree(schema);
        op.treeBuilder(selection);
        System.out.println("mmmm");

        ORCManager.reader("/home/kristalys/mytable/hello.orc", "(id>20)", "", "/home/kristalys/QUERY_RESULTS/2110881374");
    }

    private class Person {
        private String name;
        private int city_id;

        public Person(String name, int city_id) {
            this.name = name;
            this.city_id = city_id;
        }

        public String Name() {
            return name;
        }

        public int City_id() {
            return city_id;
        }

        @Override
        public String toString() {
            return "Person{" +
                    "name='" + name + '\'' +
                    ", city_id=" + city_id +
                    '}';
        }
    }

    @Test
    /*
    Created on the go and got local space of time
     */
    public void singleSideigniteTestCreate() throws Exception {
        IgniteConfiguration cfg = new IgniteConfiguration().setBinaryConfiguration(
                new BinaryConfiguration().setNameMapper(new BinaryBasicNameMapper(true)));

        // Starting the node
        Ignite ignite = Ignition.start(cfg);

        IgniteCache<Integer, Person> cache = ignite.getOrCreateCache("person");


        cache.put(122, new Person("Kristalys", 109));
        Person hello = cache.get(122);
        System.out.println(hello.toString());
        Ignition.stop(false);
    }

    @Test
    public void igniteTestDockerInsert() throws Exception {
        ClientConfiguration cfg = new ClientConfiguration().setAddresses("127.0.0.1:10800");
        try (IgniteClient client = Ignition.startClient(cfg)) {
            ClientCache<Integer, Person> cache = client.getOrCreateCache("person");
            cache.put(122, new Person("Kristalys", 109));
        }
    }

    @Test
    public void igniteTestDockerRead() throws Exception {
        ClientConfiguration cfg = new ClientConfiguration().setAddresses("127.0.0.1:10800");
        try (IgniteClient client = Ignition.startClient(cfg)) {
            ClientCache<Integer, Person> cache = client.getOrCreateCache("person");
            Person test = cache.get(122);
            System.out.println(test);
        }
    }

//    @Test
//    public void testAttributes(){
//        Tuple test = new Tuple(3);
//        test.addAttribute(Attribute.AttributeType.String,0, "Name", "Kristalys");
//        test.addAttribute(Attribute.AttributeType.Float,1, "Money", 13.5F);
//        test.addAttribute(Attribute.AttributeType.Integer,2, "Age", 42);
//        System.out.println(test.toString());
//
//    }
//
//    @Test
//    public void testAttributesSaveInIgnite(){
//        Tuple test = new Tuple(3);
//        test.addAttribute(Attribute.AttributeType.String,0, "Name", "Kristalys");
//        test.addAttribute(Attribute.AttributeType.Float,1, "Money", 13.5F);
//        test.addAttribute(Attribute.AttributeType.Integer,2, "Age", 33);
//        System.out.println(test.toString());
//        Tuple test2 = new Tuple(3);
//        test2.addAttribute(Attribute.AttributeType.String,0, "Name", "Abigail");
//        test2.addAttribute(Attribute.AttributeType.Float,1, "Money", 7.6F);
//        test2.addAttribute(Attribute.AttributeType.Integer,2, "Age", 39);
//        System.out.println(test2.toString());
//
//        ArrayList<Tuple> list = new ArrayList<>();
//        list.add(test);
//        list.add(test2);
//
//        ClientConfiguration cfg = new ClientConfiguration().setAddresses("136.145.116.98:10800");
//        try (IgniteClient client = Ignition.startClient(cfg)) {
//            ClientCache<String, ArrayList<Tuple>> cache = client.getOrCreateCache("cacheTest");
//            cache.put("/temp/partition/1", list);
//        }
//
//    }

    @Test
    public void testAttributesReadInIgnite() throws Exception {
        ClientConfiguration cfg = new ClientConfiguration().setAddresses("136.145.116.98:10800");
        try (IgniteClient client = Ignition.startClient(cfg)) {
            ClientCache<String, ArrayList<Tuple>> cache = client.getOrCreateCache("cacheTest");
            ArrayList<Tuple> list = cache.get("/temp/partition/1");
            for (Tuple tuple : list) {
                System.out.println(tuple.toString());
            }
        }
    }

    @Test
    public void s3test(){
        AWSCredentials credentials = new BasicAWSCredentials(
                "AKIA6E4TYZ3JLKC2LPFR",
                "UaMYsDlAzWeFCx0r1So4/gZLZIkbgO21kVXiDoP1"
        );
        AmazonS3 s3client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.US_EAST_1)
                .build();

        List<Bucket> buckets = s3client.listBuckets();
        for(Bucket bucket : buckets) {
            System.out.println(bucket.getName());
        }
    }
    @Test
    public void s3test2(){
        AWSCredentials credentials = new BasicAWSCredentials(
                "AKIA6E4TYZ3JLKC2LPFR",
                "UaMYsDlAzWeFCx0r1So4/gZLZIkbgO21kVXiDoP1"
        );
        AmazonS3 s3client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.US_EAST_1)
                .build();

        s3client.putObject(
                "testingjoin",
                "/product/product.orc",
                new File("C:\\Users\\Abigail\\git-data\\Container-DBMS\\tables\\product.orc")
        );
        s3client.putObject(
                "testingjoin",
                "/customer/customer.orc",
                new File("C:\\Users\\Abigail\\git-data\\Container-DBMS\\tables\\customer.orc")
        );
    }
    @Test
    public void s3test3(){
        AWSCredentials credentials = new BasicAWSCredentials(
                "AKIA6E4TYZ3JLKC2LPFR",
                "UaMYsDlAzWeFCx0r1So4/gZLZIkbgO21kVXiDoP1"
        );
        AmazonS3 s3client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.US_EAST_1)
                .build();

        ObjectListing objectListing = s3client.listObjects("testingjoin");
        for(S3ObjectSummary os : objectListing.getObjectSummaries()) {
            System.out.println(os.getKey());
        }
    }

    @Test
    public void joinTestingAWS(){
        AWSCredentials credentials = new BasicAWSCredentials(
                "AKIA6E4TYZ3JLKC2LPFR",
                "UaMYsDlAzWeFCx0r1So4/gZLZIkbgO21kVXiDoP1"
        );
        AmazonS3 s3client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.US_EAST_1)
                .build();

        ObjectListing objectListing = s3client.listObjects("testingjoin");
        for(S3ObjectSummary os : objectListing.getObjectSummaries()) {
            System.out.println(os.getKey());
        }
    }

    @Test
    public void testingHashProbing() throws Exception {
        String[] args = {"joinPartition","/customer/customer.orc","id","customer","5"};
        WorkerManager.dbms(args);
    }
}