package orc.helperClasses;

import java.io.Serializable;


public abstract class Attribute implements Serializable {
    //TODO: add Long and Double
    public enum AttributeType {String, Long, Decimal, Double};
    public String column;
    public AttributeType type;


    public Attribute(String column, AttributeType type){
        this.type = type;
        this.column = column;
    }

    public abstract String getStringValue();
}
