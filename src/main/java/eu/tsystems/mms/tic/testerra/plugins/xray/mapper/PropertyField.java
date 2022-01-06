package eu.tsystems.mms.tic.testerra.plugins.xray.mapper;

import eu.tsystems.mms.tic.testframework.common.PropertyManager;

public class PropertyField implements Field {
    private final String property;

    public PropertyField(String property) {
        this.property = property;
    }

    @Override
    public String getFieldName() {
        int intProperty = PropertyManager.getIntProperty(property);
        if (intProperty > 0) {
            return String.format("customfield_%d", intProperty);
        } else {
            return property;
        }
    }

    @Override
    public String getJQLTerm() {
        return String.format("cf[%s]", PropertyManager.getIntProperty(property));
    }
}
