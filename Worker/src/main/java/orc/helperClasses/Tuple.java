package orc.helperClasses;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class Tuple implements Serializable {
        private Attribute[] attributeArrayList;
        public int attributesNum;

        public Tuple(int attributesNum){
                this.attributesNum = attributesNum;
                this.attributeArrayList = new Attribute[attributesNum];
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

        @Override
        public String toString() {
                String result = "";
                for (int i = 0; i < attributeArrayList.length; i++) {
                        result += attributeArrayList[i].getStringValue() + " ";
                }
                return result;
        }
}