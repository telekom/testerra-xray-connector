package eu.tsystems.mms.tic.testerra.plugins.xray.mapper;

import eu.tsystems.mms.tic.testframework.common.PropertyManager;

public enum Fields implements Field {
    TEST_EXECUTION_START_DATE(new CustomField(PropertyManager.getIntProperty("xray.test.execution.start.time.field.id", 14270))),
    TEST_EXECUTION_FINISH_DATE(new CustomField(PropertyManager.getIntProperty("xray.test.execution.finish.time.field.id", 14271))),
    REVISION(new CustomField(PropertyManager.getIntProperty("xray.test.execution.revision.field.id", 14272))),
    TEST_ENVIRONMENTS(new CustomField(PropertyManager.getIntProperty("xray.test.execution.test-environments.field.id", 15155))),
    TEST_PLAN(new CustomField(PropertyManager.getIntProperty("xray.test.execution.test-plan.field.id"))),
    LABELS(new NameField("labels")),
    SUMMARY(new NameField("summary")),
    DESCRIPTION(new NameField("description")),
    VERSIONS(new NameField("versions")),
    FIX_VERSIONS(new NameField("fixVersions")),
    COMPONENTS(new NameField("components")),
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

    @Override
    public String getValidationRegex() {
        return field.getValidationRegex();
    }
}
