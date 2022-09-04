package orc.helperClasses;

import org.apache.hadoop.hive.ql.exec.vector.*;
import org.apache.orc.TypeDescription;

import java.io.Serializable;

public class Tuple implements Serializable {
    public Attribute[] attributeArrayList;
    public int attributesNum;

    public Tuple(int attributesNum){
        this.attributesNum = attributesNum;
        this.attributeArrayList = new Attribute[attributesNum];
    }

    public void addAttribute(TypeDescription type, int position, String name, ColumnVector value, int index){
        Attribute.AttributeType typeAttribute = Utils.getAttributeTypeFromType(type);
        switch (typeAttribute) {
            case Long:
                LongColumnVector lcv = (LongColumnVector) value;
                this.attributeArrayList[position] = new LongAttribute(name, lcv.vector[index]);
                break;
            case String:
                BytesColumnVector bcv = (BytesColumnVector) value;
                StringBuilder sb = new StringBuilder();
                bcv.stringifyValue(sb, index);
                this.attributeArrayList[position] = new StringAttribute(name, sb.toString());
                break;
            case Double:
                DoubleColumnVector ddcv = (DoubleColumnVector) value;
                this.attributeArrayList[position] = new DoubleAttribute(name, ddcv.vector[index]);
                break;
            case Decimal:
                DecimalColumnVector dcv = (DecimalColumnVector) value;
                //TODO: This can be int/long + double/float or just double or float
                this.attributeArrayList[position] = new FloatAttribute(name, dcv.vector[index].floatValue());
                break;
        }
    }

    public void addAttribute(Attribute.AttributeType type, int position, String name, long value, int index){
        this.attributeArrayList[position] = new LongAttribute(name, (long) value);
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