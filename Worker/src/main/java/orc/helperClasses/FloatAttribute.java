package orc.helperClasses;

public class FloatAttribute extends Attribute{
    public Float value;

    public FloatAttribute(String column, Float value) {
        super(column, AttributeType.Decimal);
        this.value = value;
    }

    @Override
    public String getStringValue() {
        return value.toString();
    }
}
