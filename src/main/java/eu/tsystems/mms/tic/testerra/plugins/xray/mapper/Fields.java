package eu.tsystems.mms.tic.testerra.plugins.xray.mapper;

public enum Fields implements Field {
    TEST_EXECUTION_START_DATE(new PropertyField("xray.test.execution.start.time.field.id")),
    TEST_EXECUTION_FINISH_DATE(new PropertyField("xray.test.execution.finish.time.field.id")),
    REVISION(new PropertyField("xray.test.execution.revision.field.id")),
    TEST_ENVIRONMENTS(new PropertyField("xray.test.execution.test-environments.field.id")),
    TEST_PLANS(new PropertyField("xray.test.execution.test-plans.field.id")),
    TEST_SET_TESTS(new PropertyField("xray.test.set.tests.field.id")),
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
