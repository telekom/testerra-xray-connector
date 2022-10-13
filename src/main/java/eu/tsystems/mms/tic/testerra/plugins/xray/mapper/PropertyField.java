package eu.tsystems.mms.tic.testerra.plugins.xray.mapper;

import eu.tsystems.mms.tic.testframework.common.PropertyManagerProvider;

public class PropertyField implements Field, PropertyManagerProvider {
    private final String property;

    public PropertyField(String property) {
        this.property = property;
    }

    @Override
    public String getFieldName() {
        return String.format("customfield_%d", getCustomFieldId());
    }

    private int getCustomFieldId() {
        int intProperty = Long.valueOf(PROPERTY_MANAGER.getLongProperty(property)).intValue();
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
