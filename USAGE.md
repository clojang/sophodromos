# SophoDromos Usage Guide

## Overview

SophoDromos provides GradlDromus-style test formatting for Maven projects with two usage modes:

1. **Standalone Mode**: Run `mvn sd:test` to execute tests with SophoDromos formatting alongside regular Maven tests
2. **Replace Mode**: Configure SophoDromos to replace the default Maven Surefire plugin execution

## Standalone Mode (Current Default)

Add the plugin to your `pom.xml`:

```xml
<plugin>
    <groupId>io.github.clojang</groupId>
    <artifactId>sophodromos</artifactId>
    <version>0.1.5</version>
</plugin>
```

Run tests with SophoDromos formatting:
```bash
mvn sd:test
```

This runs independently of `mvn test` - both can be used.

## Replace Mode (Recommended for CI/Single Test Runner)

To completely replace the default Maven test execution with SophoDromos formatting:

1. **Disable the default Surefire plugin**:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.5.3</version>
    <configuration>
        <skip>true</skip>
    </configuration>
</plugin>
```

2. **Configure SophoDromos to run in the test phase**:
```xml
<plugin>
    <groupId>io.github.clojang</groupId>
    <artifactId>sophodromos</artifactId>
    <version>0.1.5</version>
    <executions>
        <execution>
            <goals>
                <goal>replace-test</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

3. **Run tests normally**:
```bash
mvn test
```

This will skip the default Surefire execution and use SophoDromos formatting instead.

**IMPORTANT**: When using replace mode, do NOT run `mvn install sd:replace-test` as this will run tests twice. Use either:
- `mvn test` (runs only SophoDromos tests)
- `mvn install` (runs full build with SophoDromos tests)
- `mvn compile sd:replace-test` (standalone SophoDromos execution)

## Configuration Options

Both modes support the following configuration parameters:

- `sophodromos.colorOutput` (default: true) - Enable/disable color output
- `sophodromos.showProgress` (default: true) - Show progress indicators
- `sophodromos.detailedFailures` (default: true) - Show detailed failure information
- `maven.test.skip` (default: false) - Skip all tests

Example with custom configuration:
```xml
<plugin>
    <groupId>io.github.clojang</groupId>
    <artifactId>sophodromos</artifactId>
    <version>0.1.5</version>
    <configuration>
        <colorOutput>false</colorOutput>
        <detailedFailures>true</detailedFailures>
    </configuration>
</plugin>
```