package orc.helperClasses;

public class LongAttribute extends Attribute {
    public Long value;

    public LongAttribute(String column, Long value) {
        super(column, Attribute.AttributeType.Long);
        this.value = value;
    }

    @Override
    public String getStringValue() {
        return value.toString();
    }
}
