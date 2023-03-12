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
            Tuple created = new Tuple(3);
            String name = result.getString(i);
            String extractID = name.split(".")[0];
            Integer id = Integer.valueOf(extractID);
            created.addAttribute(Attribute.AttributeType.Long, 0, "id", Commons.hashFunction(id.toString(), hashing));
            created.addAttribute(Attribute.AttributeType.Long, 1, "id", id);
            created.addAttribute(Attribute.AttributeType.Long, 2, "fileName", name);
            table.addRecord(created);
        }
        table.flushRemainders();
    }
}
