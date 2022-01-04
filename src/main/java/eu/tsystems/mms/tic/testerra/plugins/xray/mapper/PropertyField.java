package eu.tsystems.mms.tic.testerra.plugins.xray.mapper;

import eu.tsystems.mms.tic.testframework.common.PropertyManager;

public class PropertyField implements Field {
    private final String property;

    public PropertyField(String property) {
        this.property = property;
    }

    private int getFieldId() {
        int intProperty = PropertyManager.getIntProperty(property);
        if (intProperty == -1) {
            throw new RuntimeException(String.format("Property %s is not defined", property));
        }
        return intProperty;
    }

    @Override
    public String getFieldName() {
        return String.format("customfield_%d", getFieldId());
    }

    @Override
    public String getJQLTerm() {
        return String.format("cf[%s]", getFieldId());
    }
}
