package orc.helperClasses;

public class IntegerAttribute extends Attribute {
    public Integer value;

    public IntegerAttribute(String column, Integer value) {
        super(column, Attribute.AttributeType.Integer);
        this.value = value;
    }

    @Override
    public String getStringValue() {
        return value.toString();
    }
}
