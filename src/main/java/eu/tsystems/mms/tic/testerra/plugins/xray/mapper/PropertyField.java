package eu.tsystems.mms.tic.testerra.plugins.xray.mapper;

import eu.tsystems.mms.tic.testframework.common.PropertyManager;

public class PropertyField implements Field {
    private final String property;

    public PropertyField(String property) {
        this.property = property;
    }

    private int getFieldId() {
        return PropertyManager.getIntProperty(property);
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
