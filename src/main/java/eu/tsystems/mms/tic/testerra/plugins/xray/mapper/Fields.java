package eu.tsystems.mms.tic.testerra.plugins.xray.mapper;

import eu.tsystems.mms.tic.testerra.plugins.xray.synchronize.XrayMapper;

public enum Fields implements Field {
    TEST_EXECUTION_START_DATE(new PropertyField(XrayMapper.PROPERTY_TEST_EXECUTION_START_DATE)),
    TEST_EXECUTION_FINISH_DATE(new PropertyField(XrayMapper.PROPERTY_TEST_EXECUTION_FINISH_DATE)),
    TEST_EXECUTION_REVISION(new PropertyField(XrayMapper.PROPERTY_TEST_EXECUTION_REVISION)),
    TEST_EXECUTION_TEST_ENVIRONMENTS(new PropertyField(XrayMapper.PROPERTY_TEST_EXECUTION_TEST_ENVIRONMENTS)),
    TEST_EXECUTION_TEST_PLANS(new PropertyField(XrayMapper.PROPERTY_TEST_EXECUTION_TEST_PLANS)),
    TEST_SET_TESTS(new PropertyField(XrayMapper.PROPERTY_TEST_SET_TESTS)),
    LABELS(new NameField("labels")),
    SUMMARY(new NameField("summary")),
    DESCRIPTION(new NameField("description")),
    VERSIONS(new NameField("versions")),
    FIX_VERSIONS(new NameField("fixVersions")),
    COMPONENTS(new NameField("components")),
    STATUS(new NameField("status")),
    PROJECT(new NameField("project")),
    ISSUE_TYPE(new NameField("issuetype")),
    ASSIGNEE(new NameField("assignee")),
    ATTACHMENT(new NameField("attachment")),
    ;

    private final Field field;

    Fields(Field field) {
        this.field = field;
    }

    public Field getField() {
        return field;
    }

    @Override
    public String getFieldName() {
        return field.getFieldName();
    }

    @Override
    public String getJQLTerm() {
        return field.getJQLTerm();
    }
}
