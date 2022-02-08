# Testerra Xray Connector

<p align="center">
    <a href="https://mvnrepository.com/artifact/io.testerra/xray-connector" title="MavenCentral"><img src="https://img.shields.io/maven-central/v/io.testerra/xray-connector/1?label=Maven%20Central"></a>
    <a href="/../../commits/" title="Last Commit"><img src="https://img.shields.io/github/last-commit/telekom/testerra-xray-connector?style=flat"></a>
    <a href="/../../issues" title="Open Issues"><img src="https://img.shields.io/github/issues/telekom/testerra-xray-connector?style=flat"></a>
    <a href="./LICENSE" title="License"><img src="https://img.shields.io/badge/License-Apache%202.0-green.svg?style=flat"></a>
</p>

<p align="center">
  <a href="#setup">Setup</a> •
  <a href="#documentation">Documentation</a> •
  <a href="#support-and-feedback">Support</a> •
  <a href="#how-to-contribute">Contribute</a> •
  <a href="#contributors">Contributors</a> •
  <a href="#licensing">Licensing</a>
</p>

## About this module

This module provides additional features for [Testerra Framework](https://github.com/telekom/testerra) for automated tests.

This module allows to synchronize the test results to the test management plugin Xray for Atlassian Jira.

## Setup

### Requirements

| Xray connector | Testerra         |
| -------------- | ---------------- |
| `1.0`          | ` 1.0.0 .. 1.3`   |
| `1.1`          | ` 1.4 .. 1.7`        |
| `1.2`          | ` 1.8`        |
| `>=1.3`          | ` >=1.9`        |


### Usage

Include the following dependency in your project. Please replace the versions with the latest version.

Gradle:

```groovy
implementation 'io.testerra:xray-connector:1.4'
```

Maven:

```xml

<dependency>
    <groupId>io.testerra</groupId>
    <artifactId>xray-connector</artifactId>
    <version>1.4</version>
</dependency>
```

## Documentation

### Add property file

To use the Xray Connector plugin you have to provide multiple properties in your test project.  
The easiest way is to create a file `xray.properties` in `src/test/resources` directory of your project.

```properties
# Enable synchronization and define strategy
xray.sync.enabled=true

# Connection details (mandatory)
xray.rest.service.uri=https://jira.example.com/rest
xray.project.key=PROJECT-KEY

# Xray connector supports Token based authentication or basic authentication
# If no token is defined, Xray connector uses 'user/password'
xray.token=jiratoken
# or
xray.user=jira-sync-user
xray.password=password
```

You also need to configure [Jira custom fields IDs](#Jira custom fields IDs).

### Implement synchronizer interface

Before synchronisation can take place, you need to create a subclass of `AbstractXrayResultsSynchronizer`, which implements the interface `XrayResultsSynchronizer`.

The Xray connector will look up in the class path for your implementation and initialize it.

```java
public class MyXrayResultsSynchronizer extends AbstractXrayResultsSynchronizer {
}
```

### Default mapping

To synchronize your test results to a specific Jira issue, the Xray connector will use some mapping mechanism.

#### Test Execution

The default mapping implementation of a *Test Execution* is done by the following search criteria:

- Project key
- Issue type
- Summary
- Revision

When a *Test Execution* was found, it will be reused, otherwise a new *Test Execution* will be created when at least one test should be synchronized.

You can control the mapping by implementing [updateTestExecution()](#Update entities), which will be called right before [queryTestExecution()](#Custom mapping implementations).

#### Annotated Test

To create a mapping between your test methods and your Jira issues of type *Test* you just have to set up the `@XrayTest`
annotation on your method.

```java
public class MethodsAnnotatedTest extends TesterraTest {

    @Test
    @XrayTest(key = "EXAMPLE-2")
    public void test_passes() {
        Assert.assertTrue(true);
    }
}
```

#### Annotated Test Set

You can also annotate the *Test Set* by its issue key. All methods (even setup methods) in this class will be handled as having the `@XrayTest` annotation present. When you don't want to synchronize specific methods, add the `@XrayNoSync` annotation.

```java

@XrayTestSet(key = "EXAMPLE-5")
public class AnnotatedClassTest extends TesterraTest {
    
    @BeforeTest
    @XrayNoSync
    public void setup() {
        // Do some setup here
    }
    
    @Test
    public void test_fails() {
        Assert.assertTrue(false);
    }
}
```

### Other mapping implementations

A list of other mapping implementations.

#### DefaultSummaryMapper

This maps Java test methods to Jira *Tests* and Java classes to Jira *Test Sets* by their name, when no keys are present in the annotations. Additionally, it creates the issues when they don't exist. You enable that feature by passing that mapper in your `XrayResultsSynchronizer`.

```java
public class MyXrayResultsSynchronizer extends AbstractXrayResultsSynchronizer {
    public XrayMapper getXrayMapper() {
        return new DefaultSummaryMapper();
    }
}
```

You need to configure the property [xray.test.set.tests.field.id](#Jira custom fields IDs) before.

### Custom mapping

When you want to have full control over the mapping, you can provide your own implementation of `XrayMapper`.

```java
public class GenericMapper implements XrayMapper {

    @Override
    public JqlQuery queryTestExecution(XrayTestExecutionIssue xrayTestExecutionIssue) {
        return JqlQuery.create()
                .addCondition(new RevisionContainsExact("Reuse My Test Execution"))
                .build();
    }

    @Override
    public JqlQuery queryTest(MethodContext methodContext) {
        return JqlQuery.create()
                .addCondition(new IssueTypeEquals(IssueType.Test))
                .addCondition(new SummaryContainsExact(methodContext.getName()))
                .build()
    }

    @Override
    public JqlQuery queryTestSet(ClassContext classContext) {
        return JqlQuery.create()
                .addCondition(new IssueTypeEquals(IssueType.TestSet))
                .addCondition(new SummaryContainsExact("My Tests"))
                .build()
    }
}
```

In this case the Xray connector will reuse the Test Execution with revision "*Reuse My Test Execution*", maps all classes to the *Test Set* with summary "*My Tests*" and search for associated Jira *Tests* where the summary matches the method name. 

#### Update entities

The `XrayMapper` also provides callbacks for updating entities.

```java
public class GenericMapper implements XrayMapper {
    
    @Override
    public void updateTestExecution(XrayTestExecutionIssue xrayTestExecutionIssue, ExecutionContext executionContext) {
        xrayTestExecutionIssue.getTestEnvironments().add("Test");
        xrayTestExecutionIssue.setFixVersions(List.of(new JiraNameReference("1.0")));
    }

    @Override
    public void updateTestSet(XrayTestSetIssue xrayTestSetIssue, ClassContext classContext) {
        xrayTestSetIssue.getLabels().add("TestAutomation");
    }

    @Override
    public void updateTest(XrayTestIssue xrayTestIssue, MethodContext methodContext) {
        xrayTestIssue.getLabels().add("TestAutomation");
    }
}
```

You can use these methods to update the Jira issues right before importing. Please mind, that not all features are supported by the [Xray import API](#References).

#### Creating new entities

By default, the Xray connector doesn't create any issues. You can enable that by passing `true` in the interface.

```java
public class GenericMapper implements XrayMapper {
    @Override
    public boolean shouldCreateNewTestSet(ClassContext classContext) {
        return true;
    }

    @Override
    public boolean shouldCreateNewTest(MethodContext methodContext) {
        return true;
    }
}
```

### Jira custom fields IDs

Jira's Xray extension uses custom field IDs instead of human readable names, therefore you need to define these custom IDs in the `properties` file.

```properties
xray.test.execution.start.time.field.id=
xray.test.execution.finish.time.field.id=
xray.test.execution.revision.field.id=
xray.test.execution.test-environments.field.id=
xray.test.execution.test-plans.field.id=
xray.test.set.tests.field.id=
```

You can retrieve these IDs directly from the Jira frontend by inspecting the field in the DOM as shown in the following screenshot.

![](doc/Jira-Field-Ids.jpg)

### Properties

|Property|Default|Description|
|---|---|---|
|xray.sync.enabled|false|Enable synchronization|
|xray.rest.service.uri|not set|URI of the Jira REST service (with Xray-Plugin installed)|
|xray.project.key|not set|Jira project key|
|xray.user|not set|Jira user to sync test execution|
|xray.password|not set|Associated password for user|
|xray.test.execution.start.time.field.id|not set|The Jira custom field for test execution start time.|
|xray.test.execution.finish.time.field.id|not set|The Jira custom field for test execution finish time.|
|xray.test.execution.revision.field.id|not set|The Jira custom field for test execution revision.|
|xray.test.execution.test-environments.field.id|not set|The Jira custom field for test execution test-environments.|
|xray.test.execution.test-plans.field.id|not set|The Jira custom field for test execution test-plans.|
|xray.test.set.tests.field.id|not set|The Jira custom field for test set tests.|
|xray.test.execution.issuetype.name|`Test Execution`|The issueType name for a test execution |
|xray.test.issuetype.name|`Test`|The issueType name for a test |
|xray.test.set.issuetype.name|`Test Set`|The issueType name for a test set |
|xray.sync.frequency|10|Xray import frequency (after n methods)|
|xray.validation.revision.regexp|.*|Revision is validated against this regular expression to prevent unintended creation of test executions.  (**deprecated**)|
|xray.validation.revision.summary|.*|Summary is validated against this regular expression to prevent unintended creation of test executions.  (**deprecated**)|
|xray.validation.revision.description|.*|Description is validated against this regular expression to prevent unintended creation of test executions.  (**deprecated**)|
|xray.webresource.filter.logging.enabled|false|Enable logging of all web requests and response sent/received to/from Jira|
|xray.webresource.filter.getrequestsonly.enabled|false|Enable this for debugging to avoid PUT/POST/DELETE requests sent to Jira|
|xray.webresource.filter.getrequestsonly.fake.response.key|FAKE-666666|This key will returned, when `xray.webresource.filter.getrequestsonly.enabled` set to `true` and PUT/POST/DELETE request was sent.|

---

## Troubleshooting

Hints for the following occuring symptoms:

Symptom | Explanation | Solution
--- | --- | ---
`{"error": "...java.sql.SQLIntegrityConstraintViolationException: ORA-00001: unique constraint (JIRA_SCHEMA.SYS_C00134897) violated` | An issue could not be imported because it already exists. | Make sure that the issue key for an existing issue could be found via. the `query()` methods of the `XrayMapper`.
`{"errorMessages":["We can't create this issue for you right now, it could be due to unsupported content you've entered into one or more of the issue fields...` | Missing data on the issue. | Try to create an issue manually, call the REST API for this issue and check which fields are set by default. |

## Publication

This module is deployed and published to Maven Central. All JAR files are signed via Gradle signing plugin.

The following properties have to be set via command line or ``~/.gradle/gradle.properties``

| Property                      | Description                                         |
| ----------------------------- | --------------------------------------------------- |
| `moduleVersion`               | Version of deployed module, default is `1-SNAPSHOT` |
| `deployUrl`                   | Maven repository URL                                |
| `deployUsername`              | Maven repository username                           |
| `deployPassword`              | Maven repository password                           |
| `signing.keyId`               | GPG private key ID (short form)                     |
| `signing.password`            | GPG private key password                            |
| `signing.secretKeyRingFile`   | Path to GPG private key                             |

If all properties are set, call the following to build, deploy and release this module:
````shell
gradle publish closeAndReleaseRepository
````

## References

1. Import Xray results: https://docs.getxray.app/display/XRAY/Import+Execution+Results

## Code of Conduct

This project has adopted the [Contributor Covenant](https://www.contributor-covenant.org/) in version 2.0 as our code of conduct. Please see the details in our [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md). All contributors must abide by the code of conduct.

## Working Language

We decided to apply _English_ as the primary project language.  

Consequently, all content will be made available primarily in English. We also ask all interested people to use English as language to create issues, in their code (comments, documentation etc.) and when you send requests to us. The application itself and all end-user faing content will be made available in other languages as needed.

## Support and Feedback
The following channels are available for discussions, feedback, and support requests:

| Type                     | Channel                                                |
| ------------------------ | ------------------------------------------------------ |
| **Issues**   | <a href="/../../issues/new/choose" title="Issues"><img src="https://img.shields.io/github/issues/telekom/testerra-xray-connector?style=flat"></a> |
| **Other Requests**    | <a href="mailto:testerra@t-systems-mms.com" title="Email us"><img src="https://img.shields.io/badge/email-CWA%20team-green?logo=mail.ru&style=flat-square&logoColor=white"></a>   |

## How to Contribute

Contribution and feedback is encouraged and always welcome. For more information about how to contribute, the project structure, as well as additional contribution information, see our [Contribution Guidelines](./CONTRIBUTING.md). By participating in this project, you agree to abide by its [Code of Conduct](./CODE_OF_CONDUCT.md) at all times.

## Contributors

At the same time our commitment to open source means that we are enabling -in fact encouraging- all interested parties to contribute and become part of its developer community.

## Licensing

Copyright (c) 2021 Deutsche Telekom AG.

Licensed under the **Apache License, Version 2.0** (the "License"); you may not use this file except in compliance with the License.

You may obtain a copy of the License at https://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the [LICENSE](./LICENSE) for the specific language governing permissions and limitations under the License.
