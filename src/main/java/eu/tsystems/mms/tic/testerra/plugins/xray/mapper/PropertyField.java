package eu.tsystems.mms.tic.testerra.plugins.xray.mapper;

import eu.tsystems.mms.tic.testframework.common.PropertyManager;

public class PropertyField implements Field {
    private final String property;

    public PropertyField(String property) {
        this.property = property;
    }

    @Override
    public String getFieldName() {
        return String.format("customfield_%d", getCustomFieldId());
    }

    private int getCustomFieldId() {
        int intProperty = PropertyManager.getIntProperty(property);
        if (intProperty > 0) {
            return intProperty;
        } else {
            throw new RuntimeException(String.format("Property '%s' is not defined", this.property));
        }
    }

    @Override
    public String getJQLTerm() {
        return String.format("cf[%s]", getCustomFieldId());
    }
}
