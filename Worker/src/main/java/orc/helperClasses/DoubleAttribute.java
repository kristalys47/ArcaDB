package orc.helperClasses;

public class DoubleAttribute extends Attribute{
    public Double value;

    public DoubleAttribute(String column, Double value) {
        super(column, AttributeType.Double);
        this.value = value;
    }

    @Override
    public String getStringValue() {
        return value.toString();
    }
}
