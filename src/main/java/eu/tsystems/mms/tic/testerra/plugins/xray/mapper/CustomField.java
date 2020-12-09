package eu.tsystems.mms.tic.testerra.plugins.xray.mapper;

public class CustomField implements Field {
    private final int fieldId;

    public CustomField(int fieldId) {
        this.fieldId = fieldId;
    }

    @Override
    public String getFieldName() {
        return String.format("customfield_%d", fieldId);
    }

    @Override
    public String getJQLTerm() {
        return String.format("cf[%s]", fieldId);
    }
}
