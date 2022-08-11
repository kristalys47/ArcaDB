import io.restassured.*;
import io.restassured.specification.RequestSpecification;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class RunResults {
    static public final String REDIS_HOST_TIMES = "136.145.116.85"; //redis
    static public final int REDIS_PORT_TIMES = 6380;
    static public void main(String[] arg) {
        String jsonString = "{" +
                "\"query\": \"select * from product inner join customer using(id);\"," +
                "\"mode\": 3 " +
                "}";
        RequestSpecification request = RestAssured.given()
                .baseUri("http://" + REDIS_HOST_TIMES + ":7271/database/query")
                .body(jsonString);
        long c = request.get().getTimeIn(TimeUnit.MILLISECONDS);
        System.out.println("Response Time: " + c);
        Jedis jedis = new Jedis(REDIS_HOST_TIMES, REDIS_PORT_TIMES);
        List<String> results = jedis.lrange("times", 0 , -1);
        for (int i = 0; i < results.size(); i++) {
            System.out.println(results.get(i));
        }
        jedis.flushAll();
    }

}
