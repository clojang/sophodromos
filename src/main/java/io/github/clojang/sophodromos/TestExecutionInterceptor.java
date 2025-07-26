package io.github.clojang.sophodromos;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.maven.project.MavenProject;

/** Intercepts and processes test execution output using GradlDromus formatting. */
public class TestExecutionInterceptor {

  private final MavenProject project;
  private final TestOutputFormatter formatter;

  // Patterns for common test output formats
  private static final Pattern TEST_RUNNING_PATTERN = Pattern.compile("^Running (.+)$");
  private static final Pattern TEST_RESULT_PATTERN =
      Pattern.compile(
          "^Tests run: (\\d+), Failures: (\\d+), Errors: (\\d+), "
              + "Skipped: (\\d+), Time elapsed: ([\\d.]+) sec");
  private static final Pattern INDIVIDUAL_TEST_PATTERN =
      Pattern.compile(
          "^(.+?)\\((.+?)\\)\\s+Time elapsed:\\s+([\\d.]+)\\s+sec" + "\\s+<<<\\s+(FAILURE|ERROR)!");
  private static final Pattern INDIVIDUAL_TEST_SUCCESS_PATTERN =
      Pattern.compile("^(.+?)\\((.+?)\\)\\s+Time elapsed:\\s+([\\d.]+)\\s+sec$");

  /**
   * Constructs a new TestExecutionInterceptor.
   *
   * @param project the Maven project
   * @param formatter the test output formatter
   */
  public TestExecutionInterceptor(MavenProject project, TestOutputFormatter formatter) {
    this.project = project;
    this.formatter = formatter;
  }

  /**
   * Intercepts and processes test output lines.
   *
   * @param line the output line to process
   * @return the formatted output line or null if the line should be skipped
   */
  public String interceptTestOutput(String line) {
    if (line == null || line.trim().isEmpty()) {
      return line;
    }

    // Detect test class execution
    Matcher runningMatcher = TEST_RUNNING_PATTERN.matcher(line);
    if (runningMatcher.matches()) {
      String testClass = runningMatcher.group(1);
      return formatTestClassExecution(testClass);
    }

    // Detect individual test results (failures/errors)
    Matcher individualTestMatcher = INDIVIDUAL_TEST_PATTERN.matcher(line);
    if (individualTestMatcher.matches()) {
      String methodName = individualTestMatcher.group(1);
      String className = individualTestMatcher.group(2);
      double timeElapsed = Double.parseDouble(individualTestMatcher.group(3));
      String status = individualTestMatcher.group(4);
      return formatter.formatTestResult(className, methodName, status, (long) (timeElapsed * 1000));
    }

    // Detect individual test results (success)
    Matcher individualTestSuccessMatcher = INDIVIDUAL_TEST_SUCCESS_PATTERN.matcher(line);
    if (individualTestSuccessMatcher.matches()) {
      String methodName = individualTestSuccessMatcher.group(1);
      String className = individualTestSuccessMatcher.group(2);
      double timeElapsed = Double.parseDouble(individualTestSuccessMatcher.group(3));
      return formatter.formatTestResult(
          className, methodName, "SUCCESS", (long) (timeElapsed * 1000));
    }

    // Detect test results summary
    Matcher resultMatcher = TEST_RESULT_PATTERN.matcher(line);
    if (resultMatcher.matches()) {
      return formatTestResults(
          Integer.parseInt(resultMatcher.group(1)), // tests run
          Integer.parseInt(resultMatcher.group(2)), // failures
          Integer.parseInt(resultMatcher.group(3)), // errors
          Integer.parseInt(resultMatcher.group(4)), // skipped
          Double.parseDouble(resultMatcher.group(5)) // time
          );
    }

    // Pass through other lines with potential modifications
    return preprocessOutputLine(line);
  }

  /**
   * Intercepts and processes error output lines.
   *
   * @param line the error line to process
   * @return the formatted error line
   */
  public String interceptErrorOutput(String line) {
    if (line == null || line.trim().isEmpty()) {
      return line;
    }

    return formatter.formatErrorLine(line);
  }

  private String formatTestClassExecution(String testClass) {
    String simpleName = testClass.substring(testClass.lastIndexOf('.') + 1);
    return formatter.formatProgressLine("🧪 Executing " + simpleName + "...");
  }

  private String formatTestResults(
      int testsRun, int failures, int errors, int skipped, double timeElapsed) {
    StringBuilder result = new StringBuilder();

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

  private String preprocessOutputLine(String line) {
    // Skip Maven noise
    if (line.contains("[INFO]") || line.contains("[DEBUG]") || line.contains("[WARNING]")) {
      return null; // Skip these lines
    }

    // Skip empty lines and dashes
    if (line.trim().isEmpty() || line.matches("^-+$")) {
      return null;
    }

    // Format assertion failures and stack traces
    if (line.contains("AssertionError") || line.contains("Expected") || line.contains("Actual")) {
      return formatter.formatErrorLine(line.trim());
    }

    if (line.trim().startsWith("at ")) {
      // Only show stack traces from our project
      if (line.contains(project.getGroupId()) || line.contains("Test")) {
        return formatter.formatErrorLine("  " + line.trim());
      }
      return null; // Skip external stack traces
    }

    return line;
  }
}
