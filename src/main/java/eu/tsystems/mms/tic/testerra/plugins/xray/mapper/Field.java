package eu.tsystems.mms.tic.testerra.plugins.xray.mapper;

/**
 * General Jira Field interface
 */
public interface Field {
    String getFieldName();
    String getJQLTerm();
    default String getValidationRegex() {
        return ".*";
    }
}
