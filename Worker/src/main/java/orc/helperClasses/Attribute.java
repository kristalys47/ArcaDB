package orc.helperClasses;

import java.io.Serializable;


public abstract class Attribute implements Serializable {
    //TODO: add Long and Double
    public enum AttributeType {String, Integer, Float};
    public String column;
    public AttributeType type;


    public Attribute(String column, AttributeType type){
        this.type = type;
        this.column = column;
    }

    public abstract String getStringValue();
}
