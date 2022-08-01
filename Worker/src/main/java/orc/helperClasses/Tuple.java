package orc.helperClasses;

import org.apache.orc.TypeDescription;

import java.io.Serializable;

public class Tuple implements Serializable {
    public Attribute[] attributeArrayList;
    public int attributesNum;

    public Tuple(int attributesNum){
        this.attributesNum = attributesNum;
        this.attributeArrayList = new Attribute[attributesNum];
    }

    public void addAttribute(TypeDescription type, int position, String name, Object value){
        Attribute.AttributeType typeAttribute = Utils.getAttributeTypeFromTypeCategory(type.getCategory());
        switch (typeAttribute) {
            case Integer:
                this.attributeArrayList[position] = new IntegerAttribute(name, (Integer) value);
                break;
            case String:
                this.attributeArrayList[position] = new StringAttribute(name, (String) value);
                break;
            case Float:
                this.attributeArrayList[position] = new FloatAttribute(name, (Float) value);
                break;
        }
    }

    public void addAttribute(Attribute.AttributeType type, int position, String name, Object value){
        switch (type) {
            case Integer:
                this.attributeArrayList[position] = new IntegerAttribute(name, (Integer) value);
                break;
            case String:
                this.attributeArrayList[position] = new StringAttribute(name, (String) value);
                break;
            case Float:
                this.attributeArrayList[position] = new FloatAttribute(name, (Float) value);
                break;
        }
    }

    public Attribute readAttribute(int position){
        return this.attributeArrayList[position];
    }

    static public Tuple joinTuple(Tuple a, Tuple b){
        Tuple newJoined = new Tuple(a.attributesNum + b.attributesNum - 2);
        int index = 0;
        for (int i = 1; i < a.attributeArrayList.length; i++) {
            newJoined.attributeArrayList[index] = a.attributeArrayList[i];
            index++;
        }
        for (int i = 1; i < b.attributeArrayList.length; i++) {
            newJoined.attributeArrayList[index] = b.attributeArrayList[i];
            index++;
        }
        return newJoined;
    }

    public TypeDescription tupleSchema(){
        //TODO: THis method.
        return null;
    }

    @Override
    public String toString() {
        String result = "";
        for (int i = 0; i < attributeArrayList.length; i++) {
            result += attributeArrayList[i].getStringValue() + " ";
        }
        return result;
    }
}