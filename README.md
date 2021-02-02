# Testerra X-Ray Connector

This module for Testerra provides a simple synchronization service for JIRA X-Ray plugin. 
It will register automatically by using Testerra `ModuleHook`, but you have to provide a valid property file and concrete implementations of interfaces for its usage.

---- 


## Releases

* Latest Release: `1.0-RC-3`

## Requirements

* Testerra in Version `1.0-RC-16`

## Usage

Include the following dependency in your project.

Gradle:
````groovy
implementation 'eu.tsystems.mms.tic.testerra:xray-connector:1.0-RC-3'
````

Maven:
````xml
<dependency>
    <groupId>eu.tsystems.mms.tic.testerra</groupId>
    <artifactId>xray-connector</artifactId>
    <version>1.0-RC-3</version>
</dependency>
````

### Add property file

To use the X-Ray Connector plugin you have to provide multiple properties in your test project.  
The easiest way is to create a file `xray.properties` in `src/test/resources` directory of your project.

````properties
# Enable synchronization and define strategy
xray.sync.enabled=true
xray.sync.skipped=true
xray.sync.strategy=adhoc

# Connection details (mandatory)
xray.rest.service.uri=https://jira.example.com/rest
xray.project.key=PROJECT-KEY
xray.user=jira-sync-user
xray.password=password

# Jira field IDs (mandatory)
xray.test.execution.start.time.field.id=
xray.test.execution.finish.time.field.id=
xray.test.execution.revision.field.id=
xray.test.execution.test-environments.field.id=
xray.test.execution.test-plan.field.id=

# Validations to avoid unintended operations
xray.validation.revision.regexp=.*
xray.validation.summary.regexp=.*
xray.validation.description.regexp=.*

# Automatically transitions when state reached
xray.transitions.on.created=
xray.transitions.on.updated=Test beginnen,Testdurchführung beenden,Testdurchführung zurücksetzen
xray.transitions.on.done=An Test übergeben

# Store previous results, when Updated test Execution is used
xray.previous.result.filename=

# Debugging features
xray.webresource.filter.getrequestsonly.enabled=false
xray.webresource.filter.getrequestsonly.fake.response.key=EXAMPLE-1
````

With this property file included and filled up with your user account and credentials you should be able to synchronize your test results with the adhoc-strategy.

### Retrieve custom field IDs

The Jira XRAY fields are implemented as custom fields and they may differ with every Jira installation. Thats why you must setup them.

You can retrieve these IDs directly from the Jira frontend by inspecting the field in the DOM as shown in the following screenshot.

![](doc/Jira-Field-Ids.jpg)


### Synchronization strategies

#### Adhoc

When property `xray.sync.strategy` is set to `adhoc` your test results will be synchronized directly after a test method finished.  
This will ensure, that you can track the current progress of your test execution in real-time in JIRA.

Please note that uploads and attachments for test execution will be uploaded after the execution finished.

#### Posthoc

When property `xray.sync.strategy` is set to `posthoc` your test results will be synchronized after the *complete* test execution ends.  
The X-Ray connector will store every test result internally and then progress a bulk-upload of all test results.

### Implement interfaces

For full control at runtime the X-Ray connector provide interfaces, that you have to implement before getting started.  
This approach let you configure nearly everything that can be configured - at run time!

The easiest way is, to start with this example.  
This implementation will provide static `XrayTestExecutionInfo`, but in practice you should determine these values at runtime.

With this configuration X-Ray connector will lookup for an JIRA issue of type `Test Execution` with matching attributes for:
- summary
- description
- revision
- assignee

If you provide `null` values, the connector will ignore these fields.  
The default implementations `EmptyMapper` and `EmptyTestExecutionUpdates` can be replaced by your own implementations and will be explained later.

````java
public class FooXrayResultsSynchronizer extends AbstractXrayResultsSynchronizer {
     
    @Override
    XrayTestExecutionInfo getExecutionInfo() {
        return new XrayTestExecutionInfo() {

            @Override
            public String getSummary() {
                return "My Test Execution";
            }

            @Override
            public String getDescription() {
                return "Automated test run";
            }

            @Override
            public String getRevision() {
                return "1.0-RC1";
            }

            @Override
            public String getAssignee() {
                return null;
            }

            @Override
            public String getFixVersion() {
                return null;
            }

            @Override
            public List<String> getTestEnvironments() {
                return null;
            }
        };
    }
     
    @Override
    public XrayMapper getXrayMapper() {
        return null;
    }
 
    @Override
    public XrayTestExecutionUpdates getExecutionUpdates() {
        return null;
    }
}
````

### Mapping
To synchronize your test results to a specific JIRA issue, the X-Ray connector will use some mapping.  
Basically there are two ways of mapping, both of them are instructed and controlled by annotations.

#### Test method mapping
To create a one-to-one mapping between your test methods and your JIRA issues of type `Test` you just have to set up the `XrayTest` annotation on your method.

````java
public class MethodsAnnotatedTest extends TesterraTest {

    @Test
    @XrayTest(key = "EXAMPLE-2")
    public void passes() {
        Assert.assertTrue(true);
    }
}
````

#### Test class mapping
Instead of annotating each method by itself, you can annotate just the test class with the `XrayTestSet` annotation  
and the X-Ray connector will do the rest for you by searching JIRA issues itself with the provided query on your implementation of `XrayMapper.methodToXrayTestQuery`.

For example, you can provide the following simple mapper, that will just grab the test method name and search JIRA issues by matching summary.

````java
@XrayTestSet(key = "EXAMPLE-5")
public class AnnotatedClassTest extends TesterraTest {
    //...
}
````

````java
public class TestMethodNameMapper implements XrayMapper {

    @Override
    public JqlQuery resultToXrayTest(ITestResult testNgResult) {
        final Object[] parameters = testNgResult.getParameters();
        if (parameters.length > 0) {
            final String summary = String.format("%s with %s", testNgResult.getMethod().getMethodName(), parameters[0]);
            return JqlQuery.create()
                    .addCondition(new SummaryContainsExact(summary))
                    .build();
        } else {
            return JqlQuery.create()
                    .addCondition(new SummaryContainsExact(testNgResult.getMethod().getMethodName()))
                    .build();
        }
    }

    @Override
    public JqlQuery classToXrayTestSet(ITestClass testNgClass) {return null;}
}
````

> Note : The default `EmptyMapper` will return `null`. This will lead in a synchronization error, because no matching JIRA issue was found. 

#### Generic mapping
If you don't want to annotate your class with `XrayTestSet` and neither your test methods with `XrayTest` you can use a full generic way by implementing the `XrayMapper.classToXrayTestSet()` method as well as the already known `methodToXrayTestQuery()`.

````java
public class GenericMapper implements XrayMapper {

    @Override
    public JqlQuery resultToXrayTest(ITestResult testNgResult) {
        final Object[] parameters = testNgResult.getParameters();
        if (parameters.length > 0) {
            final String summary = String.format("%s with %s", testNgResult.getMethod().getMethodName(), parameters[0]);
            return JqlQuery.create()
                    .addCondition(new SummaryContainsExact(summary))
                    .build();
        } else {
            return JqlQuery.create()
                    .addCondition(new SummaryContainsExact(testNgResult.getMethod().getMethodName()))
                    .build();

        }
    }

    @Override
    public JqlQuery classToXrayTestSet(ITestClass testNgClass) {
        return JqlQuery.create()
                .addCondition(new SummaryContainsExact("My Tests"))
                .build();
    }
}
````

In this case the X-ray connector will search JIRA issues for an issue of type `TestSet` with matching summary `My Tests`.  
Then the connector will run a search for all associated test methods for this test set to find an issue of type `Test` and a summary equal the test method name.

### Test execution updates on transition

As you may noticed in the cod examples above we provided a `EmptyTestExecutionUpdates` as implementation for `XrayTestExecutionUpdates` in our `FooXrayResultsSynchronizer`.  
Test execution updates should be defined to add metadata to associated JIRA issue of type `Test Execution`.

The X-ray connector will lookup JIRA for Test execution matching your criteria provided as `XrayTestExecutionInfo`.  
But if no matching Test Execution was found, it will create a new one by using the JIRA API.

To add labels, revision, summary, fix versions, associated version or other execution info you can use your own implementation of `XrayTestExecutionUpdates`.

````java
public class DefaultTestExecutionUpdates implements XrayTestExecutionUpdates {

    @Override
    public JiraIssueUpdate updateOnNewExecutionCreated() {
        return JiraIssueUpdate.create()
                .field(new SetLabels("TestAutomation"))
                .field(new TestPlan("TICKET-ID")))
                .build();
    }

    @Override
    public JiraIssueUpdate updateOnExistingExecutionUpdated() {
        return JiraIssueUpdate.create()
                .field(new SetLabels("TestAutomation"))
                .build();
    }

    @Override
    public JiraIssueUpdate updateOnExecutionDone() {
        return JiraIssueUpdate.create()
                .field(new SetAffectedVersions("1.0-RC"))
                .build();
    }
}
````

For example, this simple implementation will add the label "Test Automation" to your "updated" or freshly created test execution and set the "affected version" to "1.0-RC".

### Properties

|Property|Default|Description|
|---|---|---|
|xray.sync.enabled|false|Enable synchronization|
|xray.sync.skipped|false|Enable synchronization of test methods in state SKIPPED|
|xray.sync.strategy|adhoc|Enable `adhoc` or `posthoc` synchronization|
|xray.rest.service.uri|not set|URI of the JIRA REST service (with Xray-Plugin installed)|
|xray.project.key|not set|JIRA project key|
|xray.user|not set|JIRA user to sync test execution|
|xray.password|not set|Associated password for user|
|xray.test.execution.start.time.field.id|not set|The JIRA custom field for test execution start time.|
|xray.test.execution.finish.time.field.id|not set|The JIRA custom field for test execution finish time.|
|xray.test.execution.revision.field.id|not set|The JIRA custom field for test execution revision.|
|xray.test.execution.test-environments.field.id|not set|The JIRA custom field for test execution test-environments.|
|xray.test.execution.test-plan.field.id|not set|The JIRA custom field for test execution test-plans.|
|xray.validation.revision.regexp|.*|Revision is validated against this regular expression to prevent unintended creation of test executions.|
|xray.validation.revision.summary|.*|Summary is validated against this regular expression to prevent unintended creation of test executions.|
|xray.validation.revision.description|.*|Description is validated against this regular expression to prevent unintended creation of test executions.|
|xray.previous.result.filename|not set|When updating test execution, previous results are attached as json files using the defined file name. Default: No file created.|
|xray.transitions.on.created|not set|Transitions made on JIRA issue of type "Test Execution" when a status 'created' is reached (comma separated)|
|xray.transitions.on.updated|not set|Transitions made on JIRA issue of type "Test Execution" when a status 'updated' is reached (comma separated)|
|xray.transitions.on.done|not set|Transitions made on JIRA issue of type "Test Execution" when a status 'done' is reached (comma separated)|
|xray.webresource.filter.logging.enabled|false|Enable logging of all web requests and response sent/received to/from JIRA (deprecated)|
|xray.webresource.filter.getrequestsonly.enabled|false|Enable this for debugging to avoid PUT/POST/DELETE requests sent to JIRA|
|xray.webresource.filter.getrequestsonly.fake.response.key|FAKE-666666|This key will returned, when `xray.webresource.filter.getrequestsonly.enabled` set to `true` and PUT/POST/DELETE request was sent.|


