# SophoDromos Maven Plugin

A Maven plugin that intercepts test execution and provides beautiful, customized formatting for test output.

## Features

- 🎨 Colored output with ANSI escape codes
- 📊 Enhanced test result summaries
- 🔍 Detailed failure reporting
- ⚡ Real-time progress indicators
- 🛠️ Highly customizable formatting

## Installation

1. Build and install the plugin:
```bash
mvn clean install
```

2. Add the plugin to your target project's `pom.xml`:
```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.sophodromos</groupId>
            <artifactId>sophodromos-maven-plugin</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <executions>
                <execution>
                    <goals>
                        <goal>test</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

## Usage

Run tests with SophoDromos formatting:
```bash
mvn sophodromos:test
```

## Configuration

Configure the plugin behavior:
```xml
<plugin>
    <groupId>com.sophodromos</groupId>
    <artifactId>sophodromos-maven-plugin</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <configuration>
        <colorOutput>true</colorOutput>
        <showProgress>true</showProgress>
        <detailedFailures>true</detailedFailures>
    </configuration>
</plugin>
```

## Customization

The plugin is designed to be easily customizable. Look for `TODO` comments in the source code to find areas where you can modify the output formatting:

- `TestOutputFormatter.java` - Customize colors, symbols, and layout
- `TestExecutionInterceptor.java` - Modify how different types of output are processed
- `SophoDromosTestMojo.java` - Change the overall execution flow

## Development

To modify the formatting:

1. Edit the relevant Java files
2. Rebuild: `mvn clean install`
3. Test with: `mvn sophodromos:test`

## License

MIT License
