package orc.helperClasses;

public class StringAttribute extends Attribute {
    public String value;

    public StringAttribute(String column, String value) {
        super(column, AttributeType.String);
        this.value = value;
    }

    @Override
    public String getStringValue() {
        return value;
    }
}
