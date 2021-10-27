package orc;



import org.json.simple.JSONObject;

import java.util.ArrayList;

public class Row {
    public ArrayList<String> type;
    public ArrayList<String> name;
    public ArrayList<String> value;

    public Row(){

    }
    public Row(String schema, String values){
        String[] parsedSchema = schema.split(",");
        String[] rows = values.split("\n");
        for(int i = 0; i<rows.length; i++){

        }


    }

    public JSONObject getColumnInformation(int i){
        if(this.type.get(i) == "string"){

        }
        return new JSONObject();
    }

}