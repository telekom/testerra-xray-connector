package eu.tsystems.mms.tic.testerra.plugins.xray.mapper;

public class NameField implements Field {
    private final String name;

    public NameField(String name) {
        this.name = name;
    }

    @Override
    public String getFieldName() {
        return name;
    }

    @Override
    public String getJQLTerm() {
        return name;
    }
}
