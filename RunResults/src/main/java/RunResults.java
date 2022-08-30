import io.restassured.*;
import io.restassured.specification.RequestSpecification;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RunResults {
    static public final String DBMS = "136.145.77.80";
    static public final String REDIS_HOST_TIMES = "136.145.77.83"; //redis
    static public final int REDIS_PORT_TIMES = 6380;
    static public final int REDIS_PORT = 6379;
    static public void main(String[] arg) {
        Jedis jedisTime = new Jedis(REDIS_HOST_TIMES, REDIS_PORT_TIMES);
        Jedis jedisCache = new Jedis(REDIS_HOST_TIMES, REDIS_PORT);

        jedisCache.flushAll();
        jedisTime.flushAll();

        jedisCache.rpush("product","/product/product0.orc", "/product/product2.orc", "/product/product1.orc",
                "/product/product3.orc", "/product/product4.orc",
                "/product/product5.orc", "/product/product6.orc");
        jedisCache.rpush("customer", "/customer/customer0.orc", "/customer/customer1.orc", "/customer/customer2.orc",
                "/customer/customer3.orc", "/customer/customer4.orc", "/customer/customer6.orc",
                "/customer/customer5.orc", "/customer/customer7.orc");

        String jsonString = "{" +
                "\"query\": \"select * from product inner join customer using(id);\"," +
                "\"mode\": 3 ," +
                "\"buckets\": 10 " +
                "}";
        RequestSpecification request = RestAssured.given()
                .baseUri("http://" + DBMS + ":7271/database/query")
                .body(jsonString);
        long c = request.get().getTimeIn(TimeUnit.MILLISECONDS);
        List<String> results = jedisTime.lrange("times", 0 , -1);
        Map<String, String> maps = new HashMap<>();
        boolean probing = true;
        System.out.println(c);
        for (int i = 0; i < results.size(); i++) {
            String str = results.get(i);
            String[] strarr = str.split(" ");
            if(str.contains("Partition (Read File)")){
                maps.put(strarr[3], strarr[6]);
            } else if (str.contains("Partition (Tuples to Buckets)")){
                maps.put(strarr[4], maps.get(strarr[4]) + "," + strarr[7]);
            } else if (str.contains("Container")){
                System.out.println(maps.get(strarr[1]) + "," + strarr[4]);
                maps.remove(strarr[1]);
            } else if (str.contains("Probing (Create Hash)")){
                if(probing){
                    probing = false;
                    System.out.println("-Probing-");
                }
                maps.put(strarr[3], strarr[6]);
            } else if (str.contains("Probing (Join)")){
                maps.put(strarr[2], maps.get(strarr[2]) + "," + strarr[5]);
            }



//            System.out.println(results.get(i));
        }
    }

}
