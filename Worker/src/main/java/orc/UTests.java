package orc;

import alluxio.Constants;
import alluxio.client.file.FileOutStream;
import alluxio.client.file.FileSystem;
import alluxio.AlluxioURI;
import alluxio.conf.*;
import alluxio.exception.AlluxioException;
import alluxio.grpc.CreateFilePOptions;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import orc.helperClasses.Attribute;
import orc.helperClasses.ProjectionTree;
import orc.helperClasses.TestingUtils;
import orc.helperClasses.Tuple;
import org.apache.ignite.Ignition;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;
import org.apache.orc.TypeDescription;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static orc.Commons.*;

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
//        GRACEHashArray testObject = new GRACEHashArray(2, 340);
//        testObject.addRecord(2, "Hello");
//        testObject.addRecord(1, "Hello");
//        testObject.addRecord(2, "Hello");
//        testObject.addRecord(2, "Hello");
//        testObject.addRecord(2, "Hello");
    }
    @Test
    public  void testReadHashJoin() throws FileNotFoundException {
        int bucket = 2;
//        GRACEHashArray rTable = new GRACEHashArray(bucket, 340);
//        rTable.addRecord(2, "175,Hello,Kristalys,Ruiz");
//        rTable.addRecord(1, "456,Hello");
//        rTable.addRecord(2, "1421,Hello");
//        rTable.addRecord(2, "142,Hello");
//        rTable.addRecord(2, "3325,Hello");
//        rTable.flushRemainders();

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
//        GRACEHashArray rTable = new GRACEHashArray(bucket, 340);
//        rTable.addRecord(175, "175,Hello,Kristalys,Ruiz,1757");
//        rTable.addRecord(456, "456,Hello,4556");
//        rTable.addRecord(1421, "1421,Hello,14221");
//        rTable.addRecord(142, "142,Hello,1412");
//        rTable.addRecord(3325, "3325,Hello,31125");
//        rTable.addRecord(142, "142,Hello,1412");
//        rTable.flushRemainders();

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
//        JoinManager.join( "/tmp/tableA.orc", "id",  "/tmp/tableB.orc", "fk" , "/tmp/results/joinresult.json");
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

//    @Test
    /*
    Created on the go and got local space of time
     */
//    public void singleSideigniteTestCreate() throws Exception {
//        IgniteConfiguration cfg = new IgniteConfiguration().setBinaryConfiguration(
//                new BinaryConfiguration().setNameMapper(new BinaryBasicNameMapper(true)));
//
//        // Starting the node
//        Ignite ignite = Ignition.start(cfg);
//
//        IgniteCache<Integer, Person> cache = ignite.getOrCreateCache("person");
//
//
//        cache.put(122, new Person("Kristalys", 109));
//        Person hello = cache.get(122);
//        System.out.println(hello.toString());
//        Ignition.stop(false);
//    }

//    @Test
//    public void igniteTestDockerInsert() throws Exception {
//        ClientConfiguration cfg = new ClientConfiguration().setAddresses("127.0.0.1:10800");
//        try (IgniteClient client = Ignition.startClient(cfg)) {
//            ClientCache<Integer, Person> cache = client.getOrCreateCache("person");
//            cache.put(122, new Person("Kristalys", 109));
//        }
//    }
//
//    @Test
//    public void igniteTestDockerRead() throws Exception {
//        ClientConfiguration cfg = new ClientConfiguration().setAddresses("127.0.0.1:10800");
//        try (IgniteClient client = Ignition.startClient(cfg)) {
//            ClientCache<Integer, Person> cache = client.getOrCreateCache("person");
//            Person test = cache.get(122);
//            System.out.println(test);
//        }
//    }

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
//        test.addAttribute(Attribute.AttributeType.Double,1, "Money", 13.5F);
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
    public void testAttributesSaveInIgnite(){

        ArrayList<String > list = new ArrayList<>();
        list.add("mmmm");
        list.add("quizas?");
        list.add("PORQUWWWWWWW");

        ClientConfiguration cfg = new ClientConfiguration().setAddresses("136.145.77.83:10800");
        try (IgniteClient client = Ignition.startClient(cfg)) {
            ClientCache<String, ArrayList<String>> cache = client.getOrCreateCache("cacheTest");
            cache.put("/temp/partition/1", list);
        }

    }

    @Test
    public void testAttributesReadInIgnite() throws Exception {
        ClientConfiguration cfg = new ClientConfiguration().setAddresses("136.145.77.83:10800");
        try (IgniteClient client = Ignition.startClient(cfg)) {
            ClientCache<String, ArrayList<String>> cache = client.getOrCreateCache("cacheTest");
            ArrayList<String> list = cache.get("/temp/partition/1");
            for (String tuple : list) {
                System.out.println(tuple);
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
    public void testingHashProbing1() throws Exception {
        String[] args = {"joinPartition","/customer/customer.orc","id","customer","5"};
        WorkerManager.dbms(args);
    }
    @Test
    public void testingHashProbing2() throws Exception {
        String[] args = {"joinPartition", "/product/product.orc", "id", "product", "5"};;
        WorkerManager.dbms(args);
    }
    @Test
    public void testRedisAsIgnitewrite() throws Exception {
        Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT);
        LinkedList<String> list = new LinkedList<>();
        list.add("hello");
        list.add("bye");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(bos);
        o.writeObject(list);
        String jkey = "test";
        jedis.set(jkey.getBytes(), bos.toByteArray());


    }
    @Test
    public void testRedisAsIgniteread() throws Exception {
        String jkey = "test";
        Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT);
        ByteArrayInputStream b = new ByteArrayInputStream(jedis.get(jkey.getBytes()));
        ObjectInputStream o = new ObjectInputStream(b);
        LinkedList<String> records = (LinkedList<String>) o.readObject();
        for (String record : records) {
            System.out.println(record);
        }
    }

    @Test
    public void workerReadRedis() throws IOException, ClassNotFoundException {
        Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT);
        String key = "/join/0/product/";
        String jkey = key + jedis.lindex(key, 0);
        System.out.println(jkey);
        byte[] buff = jedis.get(jkey.getBytes());
        System.out.println(buff.length);
        ByteArrayInputStream b = new ByteArrayInputStream(jedis.get(jkey.getBytes()));
        ObjectInputStream o = new ObjectInputStream(b);
        LinkedList<Tuple> records = (LinkedList<Tuple>) o.readObject();
        for (Tuple record : records) {
            System.out.println(record.toString());
        }
    }

    @Test
    public void testingbytetos3() throws Exception {
        ArrayList<String> mmm = new ArrayList<>();
        mmm.add("mmmm");
        mmm.add("hellooo");
        AmazonS3 s3client = null;
        AWSCredentials credentials = new BasicAWSCredentials(AWS_S3_ACCESS_KEY, AWS_S3_SECRET_KEY);
        s3client = AmazonS3ClientBuilder
                    .standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withRegion(Regions.US_EAST_1)
                    .build();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(bos);
        o.writeObject(mmm);
        InputStream in = new ByteArrayInputStream(bos.toByteArray());
        s3client.putObject(S3_BUCKET, "kristalys", in, new ObjectMetadata());
    }
    @Test
    public void testingbytefroms3() throws Exception {
        ByteArrayInputStream b = null;
        AmazonS3 s3client = null;
        AWSCredentials credentials = new BasicAWSCredentials(AWS_S3_ACCESS_KEY, AWS_S3_SECRET_KEY);
        s3client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.US_EAST_1)
                .build();
            InputStream in = s3client.getObject(S3_BUCKET, "kristalys").getObjectContent();
            b = new ByteArrayInputStream(in.readAllBytes());


        ObjectInputStream o = new ObjectInputStream(b);
        ArrayList<String>  records = (ArrayList<String>) o.readObject();
        for (String record : records) {
            System.out.println(record);
        }
    }

    @Test
    public void jsonJoinSingle(){
        String mm = "[\"mmm\", \"jmj\"]";
        JsonArray gobj = JsonParser.parseString(mm).getAsJsonArray();
        for (JsonElement jsonElement : gobj) {
            System.out.println(jsonElement.toString());
        }
    }

    @Test
    public void testSingleNode() throws Exception {
        String mm = "{\"plan\":[\"join1\",\"id\",\"customer\",\"id\",\"product\",5],\"outer\":[\"/customer/customer.orc\"],\"inner\":[\"/product/product.orc\"]}";
        JsonObject gobj = JsonParser.parseString(mm).getAsJsonObject();
        JsonArray rows = gobj.getAsJsonArray("plan");
        String[] args = new String[rows.size()];

        for (int i = 0; i < args.length; i++) {
            args[i] = rows.get(i).getAsString();
            System.out.println(args[i]);
        }
        if(gobj.has("outer") && gobj.has("inner")){
            WorkerManager.dbms(args, gobj);
        } else {
            WorkerManager.dbms(args);
        }
    }

    @Test
    public void debuggingStuff() throws Exception {
        String[] hellooooo = {"testingjoin",
        "AKIA6E4TYZ3JLKC2LPFR",
        "UaMYsDlAzWeFCx0r1So4/gZLZIkbgO21kVXiDoP1",
        "136.145.77.83",
        "6379",
        "136.145.77.83",
        "6380",
        "7272",
        "7271",
        "mypassword",
        "myusername",
        "136.145.77.83",
        "5434",
        "test",
        "queue"};
        Jedis jedis = new Jedis("136.145.77.83", 6379);
        jedis.rpush("task", "{\"plan\":[\"joinPartition3\",\"/lineitem/lineitem0.orc\",\"\\\"01\\\"\",\"lineitem\",10]}");


        main.main(hellooooo);


    }

    @Test
    public void tryingAlluxios() throws IOException, AlluxioException {
//        InstancedConfiguration conf = new InstancedConfiguration(new AlluxioProperties().put(PropertyKey.MASTER_WEB_HOSTNAME, "136.145.77.83", ););
//        conf.set(PropertyKey.MASTER_WEB_HOSTNAME, "136.145.77.83");


//        Configuration.set(PropertyKey.MASTER_HOSTNAME, "136.145.77.83");
//        Configuration.set(PropertyKey.MASTER_HOSTNAME, "admws04.ece.uprm.edu");
        Configuration.set(PropertyKey.MASTER_HOSTNAME, "136.145.77.83");
        Configuration.set(PropertyKey.SECURITY_LOGIN_USERNAME, "root");

        FileSystem fs = FileSystem.Factory.create();

//        AlluxioURI path = new AlluxioURI("alluxio://admws04.ece.uprm.edu:19998/users/puessiii.txt");
        AlluxioURI base = new AlluxioURI("alluxio://136.145.77.83:19998");
        AlluxioURI path = new AlluxioURI(base, "/mmm/puessiii.txt", true);
        CreateFilePOptions options = CreateFilePOptions
                .newBuilder()
                .setRecursive(true)
//                .setBlockSizeBytes(64 * Constants.MB)
                .build();
        FileOutStream out = fs.createFile(path, options);
        out.write("This is me testing and exploting this ship up.".getBytes(StandardCharsets.UTF_8));
        out.flush();
        out.close();

    }



}