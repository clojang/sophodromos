package io.github.clojang.sophodromos;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.maven.project.MavenProject;

/** Intercepts and processes test execution output using GradlDromus formatting. */
@SuppressWarnings("PMD.TestClassWithoutTestCases") // This is not a test class
public class TestExecutionInterceptor {

  private final MavenProject project;
  private final TestOutputFormatter formatter;
  private final OutputPatternMatcher patternMatcher;
  private final OutputLineProcessor lineProcessor;

  /**
   * Constructs a new TestExecutionInterceptor.
   *
   * @param project the Maven project
   * @param formatter the test output formatter
   */
  public TestExecutionInterceptor(final MavenProject project, final TestOutputFormatter formatter) {
    this.project = project;
    this.formatter = formatter;
    this.patternMatcher = new OutputPatternMatcher();
    this.lineProcessor = new OutputLineProcessor(project, formatter);
  }

  /**
   * Intercepts and processes test output lines.
   *
   * @param line the output line to process
   * @return the formatted output line or null if the line should be skipped
   */
  public String interceptTestOutput(final String line) {
    String result = line;
    
    if (line != null && !line.isBlank()) {
      result = patternMatcher.tryMatchTestClassExecution(line, formatter);
      if (result == null) {
        result = patternMatcher.tryMatchIndividualTest(line, formatter);
      }
      if (result == null) {
        result = patternMatcher.tryMatchTestSuccess(line, formatter);
      }
      if (result == null) {
        result = patternMatcher.tryMatchTestResults(line, formatter);
      }
      if (result == null) {
        result = lineProcessor.preprocessOutputLine(line);
      }
    }
    
    return result;
  }

  /**
   * Intercepts and processes error output lines.
   *
   * @param line the error line to process
   * @return the formatted error line
   */
  public String interceptErrorOutput(final String line) {
    String result = line;
    if (line != null && !line.isBlank()) {
      result = formatter.formatErrorLine(line);
    }
    return result;
  }
}

/**
 * Handles pattern matching for different types of test output.
 */
class OutputPatternMatcher {
  // Patterns for common test output formats
  private static final Pattern RUNNING_PATTERN = Pattern.compile("^Running (.+)$");
  private static final Pattern RESULT_PATTERN =
      Pattern.compile(
          "^Tests run: (\\d+), Failures: (\\d+), Errors: (\\d+), "
              + "Skipped: (\\d+), Time elapsed: ([\\d.]+) sec");
  private static final Pattern INDIVIDUAL_PATTERN =
      Pattern.compile(
          "^(.+?)\\((.+?)\\)\\s+Time elapsed:\\s+([\\d.]+)\\s+sec" + "\\s+<<<\\s+(FAILURE|ERROR)!");
  private static final Pattern SUCCESS_PATTERN =
      Pattern.compile("^(.+?)\\((.+?)\\)\\s+Time elapsed:\\s+([\\d.]+)\\s+sec$");

  String tryMatchTestClassExecution(final String line, 
      final TestOutputFormatter formatter) {
    // PMD suppression: Using static pattern matcher is acceptable
    final Matcher runningMatcher = RUNNING_PATTERN.matcher(line);
    String result = null;
    if (runningMatcher.matches()) {
      final String testClass = runningMatcher.group(1);
      result = formatTestClassExecution(testClass, formatter);
    }
    return result;
  }

  String tryMatchIndividualTest(final String line, 
      final TestOutputFormatter formatter) {
    final Matcher testMatcher = INDIVIDUAL_PATTERN.matcher(line);
    String result = null;
    if (testMatcher.matches()) {
      final String methodName = testMatcher.group(1);
      final String className = testMatcher.group(2);
      final double timeElapsed = Double.parseDouble(testMatcher.group(3));
      final String status = testMatcher.group(4);
      result = formatter.formatTestResult(className, methodName, status, 
          (long) (timeElapsed * 1000));
    }
    return result;
  }

  String tryMatchTestSuccess(final String line, final TestOutputFormatter formatter) {
    final Matcher successMatcher = SUCCESS_PATTERN.matcher(line);
    String result = null;
    if (successMatcher.matches()) {
      final String methodName = successMatcher.group(1);
      final String className = successMatcher.group(2);
      final double timeElapsed = Double.parseDouble(successMatcher.group(3));
      result = formatter.formatTestResult(className, methodName, "SUCCESS", 
          (long) (timeElapsed * 1000));
    }
    return result;
  }

  String tryMatchTestResults(final String line, final TestOutputFormatter formatter) {
    final Matcher resultMatcher = RESULT_PATTERN.matcher(line);
    String result = null;
    if (resultMatcher.matches()) {
      result = formatTestResults(
          Integer.parseInt(resultMatcher.group(1)), // tests run
          Integer.parseInt(resultMatcher.group(2)), // failures
          Integer.parseInt(resultMatcher.group(3)), // errors
          Integer.parseInt(resultMatcher.group(4)), // skipped
          Double.parseDouble(resultMatcher.group(5)), // time
          formatter);
    }
    return result;
  }

  private String formatTestClassExecution(final String testClass, final TestOutputFormatter formatter) {
    final String simpleName = testClass.substring(testClass.lastIndexOf('.') + 1);
    return formatter.formatProgressLine("🧪 Executing " + simpleName + "...");
  }

  private String formatTestResults(
      final int testsRun,
      final int failures,
      final int errors,
      final int skipped,
      final double timeElapsed,
      final TestOutputFormatter formatter) {
    final StringBuilder result = new StringBuilder();

    if (failures == 0 && errors == 0) {
      result.append("✅ ");
    } else {
      result.append("❌ ");
    }

    result.append(String.format("Tests: %d", testsRun));

    if (failures > 0) {
      result.append(String.format(", Failures: %d", failures));
    }

    if (errors > 0) {
      result.append(String.format(", Errors: %d", errors));
    }

    if (skipped > 0) {
      result.append(String.format(", Skipped: %d", skipped));
    }

    result.append(String.format(" (%.3fs)", timeElapsed));

    return formatter.formatProgressLine(result.toString());
  }
}

/**
 * Processes and filters output lines that don't match specific patterns.
 */
class OutputLineProcessor {
  private final MavenProject project;
  private final TestOutputFormatter formatter;

  OutputLineProcessor(final MavenProject project, final TestOutputFormatter formatter) {
    this.project = project;
    this.formatter = formatter;
  }

  String preprocessOutputLine(final String line) {
    String result = line;

    if (shouldSkipLine(line)) {
      result = null;
    } else if (isAssertionFailure(line)) {
      result = formatter.formatErrorLine(line.trim());
    } else if (isStackTrace(line)) {
      result = handleStackTrace(line);
    }

    return result;
  }

  private boolean shouldSkipLine(final String line) {
    return line.contains("[INFO]")
        || line.contains("[DEBUG]")
        || line.contains("[WARNING]")
        || line.isBlank()
        || line.matches("^-+$");
  }

  private boolean isAssertionFailure(final String line) {
    return line.contains("AssertionError") || line.contains("Expected") || line.contains("Actual");
  }

  private boolean isStackTrace(final String line) {
    final String trimmedLine = line.trim();
    return trimmedLine.startsWith("at ");
  }

  private String handleStackTrace(final String line) {
    String result = line;
    if (isStackTrace(line)) {
      final String groupId = project.getGroupId();
      if (line.contains(groupId) || line.contains("Test")) {
        result = formatter.formatErrorLine("  " + line.trim());
      } else {
        result = null; // Skip external stack traces
      }
    }
    return result;
  }
}