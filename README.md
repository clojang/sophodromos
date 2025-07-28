# Sophodromos

[![Build Status][gh-actions-badge]][gh-actions]

[![Project Logo][logo]][logo-large]

*A wise and efficient test runner with intelligent execution strategies*

Sophodromos (from Greek "sophos" meaning wise + "dromos" meaning running/course) is a smart test execution framework that optimizes test runs through intelligent analysis and strategic execution planning.

## Features

🧠 **Intelligent Test Selection** - Analyzes code changes to run only relevant tests  
⚡ **Parallel Execution** - Optimizes test parallelization based on historical performance data  
📈 **Performance Analytics** - Tracks and analyzes test performance trends over time  
🎯 **Failure Prediction** - Uses ML techniques to predict and prevent test failures  
🔄 **Adaptive Strategies** - Learns from previous runs to improve execution efficiency  
💾 **Persistent Cache** - Maintains intelligent caching of test results and metadata  

## Installation

### Maven Dependency

Add to your project's `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>io.github.clojang</groupId>
        <artifactId>sophodromos</artifactId>
        <version>0.1.2</version>
    </dependency>
</dependencies>
```

### Gradle Dependency

Add to your `build.gradle`:

```kotlin
dependencies {
    implementation 'io.github.clojang:sophodromos:1.0.0'
}
```

### Plugin Configuration

For Gradle projects:

```kotlin
plugins {
    id("io.github.clojang.sophodromos") version "1.0.0"
}
```

## Quick Start

Here's a simple example to get you started:

```java
import io.github.clojang.sophodromos.SophodromosRunner;
import io.github.clojang.sophodromos.config.ExecutionConfig;

public class MyTestSuite {
    public static void main(String[] args) {
        // Setup intelligent test execution
        ExecutionConfig config = new ExecutionConfig()
            .setParallelism(4)
            .setIntelligentSelection(true)
            .setPerformanceTracking(true)
            .setCacheResults(true);
        
        SophodromosRunner runner = new SophodromosRunner(config);
        runner.executeTests("src/test/java");
    }
}
```

## Configuration

Configure Sophodromos using the `ExecutionConfig` class:

```java
ExecutionConfig config = new ExecutionConfig()
    .setParallelism(8)                    // Number of parallel test threads
    .setIntelligentSelection(true)        // Enable smart test selection
    .setPerformanceTracking(true)         // Track test performance metrics
    .setCacheResults(true)                // Cache test results
    .setFailurePrediction(true)           // Enable ML-based failure prediction
    .setExecutionStrategy("adaptive")     // Execution strategy
    .setReportFormat("detailed");         // Report output format

SophodromosRunner runner = new SophodromosRunner(config);
```

### Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `parallelism` | int | 4 | Number of parallel execution threads |
| `intelligentSelection` | boolean | true | Enable smart test selection based on code changes |
| `performanceTracking` | boolean | true | Track and analyze test performance over time |
| `cacheResults` | boolean | true | Cache test results for faster subsequent runs |
| `failurePrediction` | boolean | false | Use ML to predict potential test failures |
| `executionStrategy` | String | "adaptive" | Execution strategy (adaptive, sequential, parallel, priority) |
| `reportFormat` | String | "summary" | Report format (summary, detailed, json, xml) |

## Execution Strategies

Sophodromos supports multiple execution strategies:

### Adaptive Strategy (Recommended)
```java
config.setExecutionStrategy("adaptive");
```
Automatically adjusts execution based on historical data and current system resources.

### Priority Strategy
```java
config.setExecutionStrategy("priority");
```
Executes tests based on failure probability and impact analysis.

### Parallel Strategy
```java
config.setExecutionStrategy("parallel");
```
Maximizes parallel execution while respecting resource constraints.

### Sequential Strategy
```java
config.setExecutionStrategy("sequential");
```
Traditional sequential execution for debugging or resource-constrained environments.

## Intelligent Test Selection

Sophodromos analyzes your codebase to determine which tests need to run:

```java
// Analyze changes since last commit
runner.analyzeChanges("HEAD~1");

// Run only tests affected by changes
TestResults results = runner.executeSelectedTests();

// View selection reasoning
SelectionReport report = runner.getSelectionReport();
System.out.println("Selected " + report.getSelectedCount() + " of " + 
                  report.getTotalCount() + " tests");
```

## Performance Analytics

Track and analyze test performance over time:

```java
// Get performance metrics
PerformanceAnalytics analytics = runner.getAnalytics();

// View trends
List<TestTrend> trends = analytics.getTrends(Duration.ofDays(30));

// Identify slow tests
List<TestMetric> slowTests = analytics.getSlowestTests(10);

// Export metrics
analytics.exportMetrics("performance-report.json");
```

## Failure Prediction

Enable ML-based failure prediction to catch issues early:

```java
config.setFailurePrediction(true);

// Run prediction analysis
PredictionReport prediction = runner.predictFailures();

// View high-risk tests
List<TestRisk> risks = prediction.getHighRiskTests();
for (TestRisk risk : risks) {
    System.out.println("High risk: " + risk.getTestName() + 
                      " (confidence: " + risk.getConfidence() + "%)");
}
```

## Output Format

Sophodromos produces clean, informative output:

**Summary Format:**
```
==============================================================================
Sophodromos Test Execution Report (v1.0.0)
------------------------------------------------------------------------------
Strategy: Adaptive    |    Parallelism: 4 threads    |    Selection: Intelligent
Total Tests: 156      |    Selected: 23 tests        |    Cached: 133 tests
Execution Time: 2.3s  |    Time Saved: 45.7s         |    Success Rate: 100%

💚 23 passed, 💔 0 failed, 💤 0 skipped, ⚡ 133 cached

Performance Insights:
─────────────────────
• 15% faster than average execution
• 3 tests showing performance regression
• Cache hit rate: 85%

✨ All tests passed! Next run estimated: 1.8s
==============================================================================
```

**Detailed Format:**
```
==============================================================================
Sophodromos Detailed Execution Report
------------------------------------------------------------------------------
Test Selection Analysis:
• Code changes detected in: 5 files
• Impact analysis found: 23 affected tests
• Cache hit rate: 85% (133/156 tests)

Test Execution:
📦 com.example.service
    UserServiceTest.shouldCreateUser() .............................💚 (45ms)
    UserServiceTest.shouldValidateEmail() ..........................💚 (12ms)
    
📦 com.example.controller  
    UserControllerTest.shouldHandleGetRequest() ....................💚 (89ms)
    UserControllerTest.shouldHandlePostRequest() ...................💚 (123ms)

Performance Analysis:
─────────────────────
Fastest: UserServiceTest.shouldValidateEmail() (12ms)
Slowest: UserControllerTest.shouldHandlePostRequest() (123ms)
Average: 67ms per test

Failure Predictions:
───────────────────
🟡 UserServiceTest.shouldHandleNullInput() - Medium risk (65% confidence)
🔴 DatabaseConnectionTest.shouldReconnect() - High risk (89% confidence)

✨ Execution complete! Consider reviewing high-risk tests.
==============================================================================
```

## Integration Examples

### Maven Integration
```xml
<plugin>
    <groupId>io.github.clojang</groupId>
    <artifactId>sophodromos-maven-plugin</artifactId>
    <version>0.1.2</version>
    <configuration>
        <parallelism>6</parallelism>
        <intelligentSelection>true</intelligentSelection>
        <executionStrategy>adaptive</executionStrategy>
    </configuration>
</plugin>
```

### Gradle Integration
```kotlin
sophodromos {
    parallelism = 8
    intelligentSelection = true
    performanceTracking = true
    executionStrategy = "adaptive"
    reportFormat = "detailed"
    
    // Advanced options
    cacheDirectory = file("build/sophodromos-cache")
    analysisDepth = "full"
    mlModelPath = "models/failure-prediction.model"
}
```

### CI/CD Integration
```yaml
# GitHub Actions example
- name: Run Sophodromos Tests
  run: ./gradlew test -Psophodromos.strategy=priority -Psophodromos.parallelism=4
  
- name: Upload Performance Report
  uses: actions/upload-artifact@v3
  with:
    name: sophodromos-performance-report
    path: build/reports/sophodromos/
```

## Demo Application

Run the demo to see Sophodromos in action:

```bash
mvn compile exec:java -Dexec.mainClass="io.github.clojang.sophodromos.demo.DemoApp"
```

[![Demo screenshot][screenshot]][screenshot]

## Building from Source

```bash
git clone https://github.com/clojang/sophodromos.git
cd sophodromos
mvn clean install
```

## Requirements

- **Java**: 17+
- **Maven**: 3.8+ or **Gradle**: 8.0+
- **Memory**: 512MB+ recommended for ML features

## Contributing

Found a bug or have a feature request? Please [open an issue](https://github.com/clojang/sophodromos/issues/new) on GitHub!

### Development Setup

```bash
git clone https://github.com/clojang/sophodromos.git
cd sophodromos
./mvnw clean compile
./mvnw test
```

## Background

Sophodromos was created to address the growing complexity of test suites in modern applications. As codebases grow, running all tests becomes time-consuming and resource-intensive. By applying intelligent analysis and machine learning techniques, Sophodromos helps teams maintain fast feedback loops while ensuring comprehensive test coverage.

The name combines the Greek words "sophos" (wise) and "dromos" (running/course), reflecting the project's goal of bringing wisdom to test execution.

## Related Projects

This library is part of the Clojang ecosystem:
- [clojog](https://github.com/clojang/clojog) - Beautiful logging for Java applications
- [gradldromus](https://github.com/clojang/gradldromus) - Clean test output formatting for Gradle

## License

© 2025, Clojang. All rights reserved.

Licensed under the Apache License, Version 2.0. See `LICENSE` file for details.

---

**Made with 💚 by developers who believe testing should be both smart and fast**

[//]: ---Named-Links---

[logo]: https://github.com/clojang/gradldromus/blob/main/resources/images/logo.jpg?raw=true
[logo-large]: https://github.com/clojang/gradldromus/blob/main/resources/images/logo-large.jpg?raw=true
[screenshot]: resources/images/demo-screenshot.png
[gh-actions-badge]: https://github.com/clojang/sophodromos/workflows/CI/badge.svg
[gh-actions]: https://github.com/clojang/sophodromos/actions?query=workflow%3ACI