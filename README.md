# Sophodromos

[![Build Status][gh-actions-badge]][gh-actions]

[![Project Logo][logo]][logo-large]

*An explicit, clean, and beautiful test formatter for Maven's test runner*

Sophodromos is a simple Maven plugin that formats test output in a clean, readable style inspired by GradlDromus. It runs your tests using Maven Surefire and provides colorized, progress-based output that's easy to read and understand.

## Installation

Add the plugin to your project's `pom.xml`:

```xml
<plugin>
    <groupId>io.github.clojang</groupId>
    <artifactId>sophodromos</artifactId>
    <version>0.1.8</version>
</plugin>
```

## Usage

### Basic Usage

Run tests with sophodromos formatting:

```bash
mvn sd:test
```

### Recommended Usage

To avoid running tests twice (Maven's default tests + sophodromos), use:

```bash
mvn compile sd:test
# or
mvn install -Dmaven.test.skip=true sd:test
```

### Example Output

```
================================================================================
SophoDromos Test Runner (version: 0.1.8)
--------------------------------------------------------------------------------

my-project

  SimpleTest.testAddition() ..................................................💚 (2ms)
  SimpleTest.testSubtraction() ...............................................💚
  SimpleTest.testMultiplication() ...........................................💚
  AnotherTests.testDivision() ................................................💚
  AnotherTests.testModulo() ..................................................💚

Test Summary:
─────────────
Total: 5 tests, 💚 5 passed, 💔 0 failed, 💤 0 skipped
Time: 0.0s

✨ All tests passed!
================================================================================
```

This plugin works with multi-module projects, too. If you want to only run tests for a specific module, you can use:

```bash
mvn sd:test -pl modules/my-module -Dmaven.test.skip=true
```

If your doesn't use the standard `modules` and uses a flat structure, you can run:

```bash
mvn sd:test -pl my-module -Dmaven.test.skip=true
```

## Configuration

### Plugin Configuration

Configure the plugin in your `pom.xml`:

```xml
<plugin>
    <groupId>io.github.clojang</groupId>
    <artifactId>sophodromos</artifactId>
    <version>0.1.8</version>
    <configuration>
        <!-- Display options -->
        <showModuleNames>true</showModuleNames>
        <showMethodNames>true</showMethodNames>
        <showTimings>true</showTimings>
        <useColors>true</useColors>
        
        <!-- Terminal settings -->
        <terminalWidth>0</terminalWidth>
        <suppressOutput>false</suppressOutput>
        
        <!-- Custom status symbols -->
        <passSymbol>💚</passSymbol>
        <failSymbol>💔</failSymbol>
        <skipSymbol>💤</skipSymbol>
        
        <!-- Legacy options (still supported) -->
        <colorOutput>true</colorOutput>
        <showProgress>true</showProgress>
        <detailedFailures>true</detailedFailures>
        <skipTests>false</skipTests>
    </configuration>
</plugin>
```

### Command Line Options

You can also configure options via system properties:

```bash
# Display control
mvn sd:test -Dsophodromos.showModuleNames=false
mvn sd:test -Dsophodromos.showMethodNames=false
mvn sd:test -Dsophodromos.showTimings=false
mvn sd:test -Dsophodromos.useColors=false

# Custom symbols
mvn sd:test -Dsophodromos.passSymbol=✅
mvn sd:test -Dsophodromos.failSymbol=❌
mvn sd:test -Dsophodromos.skipSymbol=⏭️

# Terminal settings
mvn sd:test -Dsophodromos.terminalWidth=120

# Legacy options
mvn sd:test -Dsophodromos.colorOutput=false
mvn sd:test -Dsophodromos.showProgress=false
mvn sd:test -Dsophodromos.detailedFailures=false
mvn sd:test -Dsophodromos.skip=true
```

### Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| **Display Options** | | | |
| `showModuleNames` | boolean | `true` | Show module/project names in output |
| `showMethodNames` | boolean | `true` | Show individual test method names during execution |
| `showTimings` | boolean | `true` | Show execution times for tests |
| `useColors` | boolean | `true` | Enable colorized output |
| **Terminal Settings** | | | |
| `terminalWidth` | int | `0` | Override terminal width detection (0 = auto-detect) |
| `suppressOutput` | boolean | `false` | Suppress Maven's default test output |
| **Custom Symbols** | | | |
| `passSymbol` | String | `💚` | Symbol for passed tests |
| `failSymbol` | String | `💔` | Symbol for failed tests |
| `skipSymbol` | String | `💤` | Symbol for skipped tests |
| **Legacy Options** | | | |
| `colorOutput` | boolean | `true` | Enable colored output (same as `useColors`) |
| `showProgress` | boolean | `true` | Show test progress (same as `showMethodNames`) |
| `detailedFailures` | boolean | `true` | Show detailed failure information when tests fail |
| `skipTests` | boolean | `false` | Skip running tests entirely |

## Project Types

Sophodromos works with all Maven project structures:

- **Single-module projects**: Standard `src/test/java` layout
- **Multi-module projects**: Runs tests for each module separately
- **Traditional layouts**: Any Maven-compatible test structure

## Features

- **Clean Output**: Beautiful, readable test formatting
- **Color Support**: Green checkmarks for passed tests, red X's for failures
- **Progress Indicators**: See tests as they run
- **Timing Information**: Shows execution time for individual tests
- **Multi-module Support**: Works seamlessly with Maven reactor builds
- **Surefire Integration**: Uses Maven Surefire under the hood

## Requirements

- **Java**: 17+
- **Maven**: 3.8+

## Building from Source

```bash
git clone https://github.com/clojang/sophodromos.git
cd sophodromos
mvn clean install
```

## Development

### Running Tests

```bash
# Run sophodromos's own tests
make test

# Test in example projects
cd test-projects/single-module && make test
cd test-projects/multi-module && mvn sd:test
```

### Making Changes

1. Edit source code in `src/main/java/io/github/clojang/sophodromos/`
2. Build with `mvn clean install`
3. Test with example projects

## Contributing

Found a bug or have a feature request? Please [open an issue](https://github.com/clojang/sophodromos/issues/new) on GitHub!

## Background

Sophodromos was created to bring clean, readable test output to Maven projects. While Gradle has excellent test formatters like GradlDromus, Maven's default output can be verbose and hard to parse. Sophodromos fills this gap by providing a simple, focused test formatter.

The name combines the Greek words "sophos" (wise) and "dromos" (running/course), reflecting the project's goal of bringing wisdom to test execution.

## Related Projects

This library is part of the Clojang ecosystem:
- [gradldromus](https://github.com/clojang/gradldromus) - Clean test output formatting for Gradle

## License

© 2025, Clojang. All rights reserved.

Licensed under the Apache License, Version 2.0. See `LICENSE` file for details.

---

**Made with 💚 by developers who believe testing should be beautiful**

[//]: ---Named-Links---

[logo]: https://github.com/clojang/gradldromus/blob/main/resources/images/logo.jpg?raw=true
[logo-large]: https://github.com/clojang/gradldromus/blob/main/resources/images/logo-large.jpg?raw=true
[gh-actions-badge]: https://github.com/clojang/sophodromos/workflows/CI/badge.svg
[gh-actions]: https://github.com/clojang/sophodromos/actions?query=workflow%3ACI