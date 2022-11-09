package orc.helperClasses;

import org.apache.hadoop.hive.ql.exec.vector.ColumnVector;
import org.apache.orc.TypeDescription;

public class Utils {
    public static ColumnVector.Type getTypeFromTypeCategory(TypeDescription.Category type) {
        switch (type){
            case DECIMAL:
                return ColumnVector.Type.DECIMAL;
            case DOUBLE:
            case FLOAT:
                return ColumnVector.Type.DOUBLE;
            case BOOLEAN:
            case INT:
            case LONG:
            case SHORT:
                return ColumnVector.Type.LONG;
            case BYTE:
            case CHAR:
            case VARCHAR:
            case STRING:
            default:
                return ColumnVector.Type.BYTES;
        }
    }

//    public static Attribute.AttributeType getAttributeTypeFromTypeCategory(TypeDescription.Category type) {
//        switch (type){
//            case DECIMAL:
//            case DOUBLE:
//            case FLOAT:
//                return Attribute.AttributeType.Float;
//            case BOOLEAN:
//            case INT:
//            case LONG:
//            case SHORT:
//                return Attribute.AttributeType.Integer;
//            case BYTE:
//            case CHAR:
//            case VARCHAR:
//            case STRING:
//            default:
//                return Attribute.AttributeType.String;
//        }
//    }
    public static Attribute.AttributeType getAttributeTypeFromType(TypeDescription type) {
        switch (type.toString()){
            case "binary":
            case "char":
            case "string":
            case "varchar":
                return Attribute.AttributeType.String;
            case "bigint":
            case "boolean":
            case "date":
            case "int":
            case "smallint":
            case "tinyint":
                return Attribute.AttributeType.Long;
            case "float":
            case "double":
                return Attribute.AttributeType.Double;
            default:
                if(type.toString().contains("decimal"))
                    return Attribute.AttributeType.Decimal;
                else
                    return null;
        }
    }
}
