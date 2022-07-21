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




}
