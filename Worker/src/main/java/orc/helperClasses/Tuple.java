package orc.helperClasses;

import org.apache.hadoop.hive.ql.exec.vector.BytesColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.ColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.DoubleColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.LongColumnVector;
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
        ColumnVector.Type coltype = Utils.getTypeFromTypeCategory(type.getCategory());
        Attribute.AttributeType typeAttribute = Utils.getAttributeTypeFromTypeCategory(type.getCategory());
        switch (typeAttribute) {
            case Integer:
                LongColumnVector lcv = (LongColumnVector) value;
                this.attributeArrayList[position] = new IntegerAttribute(name, (int) lcv.vector[index]);
                break;
            case String:
                BytesColumnVector bcv = (BytesColumnVector) value;
                StringBuilder sb = new StringBuilder();
                bcv.stringifyValue(sb, index);
                this.attributeArrayList[position] = new StringAttribute(name, sb.toString());
                break;
            case Float:
                DoubleColumnVector dcv = (DoubleColumnVector) value;
                this.attributeArrayList[position] = new FloatAttribute(name, (float) dcv.vector[index]);
                break;
        }
    }

    public void addAttribute(Attribute.AttributeType type, int position, String name, long value, int index){
        this.attributeArrayList[position] = new IntegerAttribute(name, (int) value);
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