package orc;

import orc.helperClasses.Tuple;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.LinkedList;

import static orc.Commons.*;

public class Testing {

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
}
