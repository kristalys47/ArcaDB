package orc;

import orc.helperClasses.AES;
import orc.helperClasses.Attribute;
import orc.helperClasses.GRACEHashArrayInParts;
import orc.helperClasses.Tuple;
import org.apache.orc.TypeDescription;
import org.json.JSONArray;
import org.json.JSONObject;

public class FileAttributesManager {

    public static void semistructuredID(JSONArray result, int buckets, String relation) {
        GRACEHashArrayInParts table = new GRACEHashArrayInParts(buckets, relation);
        AES hashing = new AES("helohelohelohelo");
        for (int i = 0; i < result.length(); i++) {
            System.out.println(result.toString());
            Tuple created = new Tuple(3);
            String name = result.getString(i);
            System.out.println(name);
            String extractID = name.split("\\.")[0];
            Long id = Long.valueOf(extractID);
            created.addAttribute(Attribute.AttributeType.Long, 0, "id", Commons.hashFunction(id.toString(), hashing));
            created.addAttribute(Attribute.AttributeType.Long, 1, "id", id);
            created.addAttribute(Attribute.AttributeType.String, 2, "fileName", name);
            table.addRecord(created);
        }
        table.flushRemainders();
    }
}
