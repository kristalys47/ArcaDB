import requests as req

DBMS = "136.145.77.80"

r = req.get("http://136.145.77.80:7271/database/query")
print(r.text)[0:300]

# import io.restassured.*;
# import io.restassured.response.Response;
# import io.restassured.specification.RequestSpecification;
# import redis.clients.jedis.Jedis;
#
# import java.io.*;
#
# import com.opencsv.CSVWriter;
#
# import java.util.HashMap;
# import java.util.List;
# import java.util.Map;
# import java.util.concurrent.TimeUnit;
#
# public class RunResults {
#     static public final String
#     static public final String REDIS_HOST_TIMES = "136.145.77.83"; //redis
#     static public final int REDIS_PORT_TIMES = 6380;
#     static public final int REDIS_PORT = 6379;
#     static public void main(String[] arg) throws IOException {
#         Jedis jedisTime = new Jedis(REDIS_HOST_TIMES, REDIS_PORT_TIMES);
# //        Jedis jedisCache = new Jedis(REDIS_HOST_TIMES, REDIS_PORT);
# //
# //        jedisCache.flushAll();
# //        jedisTime.flushAll();
# //
#         int buckets = 5;
#         String jsonString = "{" +
#                 "\"mode\": 3," +
#                 "\"buckets\": " + buckets + "," +
#                 "\"query\": \"select * from lineitem, part where lineitem.\"01\" = part.\"00\"; " +
#                 "}";
#
#         OutputStreamWriter writer = new OutputStreamWriter(
#                 new FileOutputStream("results" + buckets + ".csv"), "UTF-8");
#
#         BufferedWriter bufWriter = new BufferedWriter(writer);
#
#         RequestSpecification request = RestAssured.given()
#                 .baseUri("http://" + DBMS + ":7271/database/query");
# //                .body(jsonString);
#
#         Response response = request.get();
#         long c = request.get().getTimeIn(TimeUnit.MILLISECONDS);
#         System.out.println(request.response());
#         bufWriter.write(c + "\n");
#         List<String> results = jedisTime.lrange("times", 0 , -1);
#         Map<String, String> maps = new HashMap<>();
#         boolean probing = true;
#         for (int i = 0; i < results.size(); i++) {
#             String str = results.get(i);
#             String[] strarr = str.split(" ");
#             if(str.contains("Partition (Read File)")){
#                 maps.put(strarr[3], strarr[6]);
#             } else if (str.contains("Partition (Tuples to Buckets)")){
#                 maps.put(strarr[4], maps.get(strarr[4]) + "," + strarr[7]);
#             } else if (str.contains("Container")){
#                 bufWriter.write(maps.get(strarr[1]) + "," + strarr[4] + "\n");
#                 maps.remove(strarr[1]);
#             } else if (str.contains("Probing (Create Hash)")){
#                 if(probing){
#                     probing = false;
#                     bufWriter.write("-Probing-" + "\n");
#                 }
#                 maps.put(strarr[3], strarr[6]);
#             } else if (str.contains("Probing (Join)")){
#                 maps.put(strarr[2], maps.get(strarr[2]) + "," + strarr[5]);
#             }
#         }
#     }
#
# }
