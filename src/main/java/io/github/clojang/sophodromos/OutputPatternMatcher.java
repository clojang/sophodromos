package io.github.clojang.sophodromos;

import io.github.clojang.gradldromus.AnsiColors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Handles pattern matching for different types of test output. */
@SuppressWarnings("PMD.AvoidDuplicateLiterals") // Necessary for PMD suppression annotations to work
class OutputPatternMatcher {

  private final AnsiColors colors;
  private boolean resultsSeen;
  // Patterns for common test output formats
  private static final Pattern RUNNING_PATTERN =
      Pattern.compile("^(?:\\[INFO\\]\\s+)?Running (.+)$");
  private static final Pattern RESULT_PATTERN =
      Pattern.compile(
          "^(?:\\[INFO\\]\\s+)?Tests run: (\\d+), Failures: (\\d+), Errors: (\\d+), "
              + "Skipped: (\\d+)(?:, Time elapsed: ([\\d.]+) s(?: -- in (.+))?)?$");
  private static final Pattern TEST_FAIL_PTN =
      Pattern.compile(
          "^(.+?)\\((.+?)\\)\\s+Time elapsed:\\s+([\\d.]+)\\s+s(?:ec)?\\s+<<<\\s+(FAILURE|ERROR)!");
  private static final Pattern SUCCESS_PATTERN =
      Pattern.compile("^(.+?)\\((.+?)\\)\\s+Time elapsed:\\s+([\\d.]+)\\s+s(?:ec)?$");
  private static final Pattern MTHD_EXEC_PATTERN =
      Pattern.compile(
          "^\\[INFO\\]\\s+([^\\s]+)\\.([^\\s]+)\\s+--\\s+Time elapsed:\\s+([\\d.]+)\\s+s"
              + "(?:ec)?$");

  /**
   * Constructs a new OutputPatternMatcher.
   *
   * @param colors the ANSI color formatter instance
   */
  public OutputPatternMatcher(final AnsiColors colors) {
    this.colors = colors;
  }

  /**
   * Tries to match a test class execution line. Package-private method for internal use within the
   * sophodromos package.
   *
   * @param line the line to match
   * @param formatter the output formatter
   * @return formatted result or null if no match
   */
  @SuppressWarnings({"PMD.DataflowAnomalyAnalysis", "PMD.LawOfDemeter"})
  // False positive from OnlyOneReturn refactoring
  protected String tryMatchTestClassExecution(
      final String line, final TestOutputFormatter formatter) {
    // Always suppress class execution messages to match desired output format.
    // We return null regardless of whether the line matches RUNNING_PATTERN because
    // we want to suppress all "Running TestClass" messages in the output.
    // This eliminates the antipattern of having a ternary operator where both
    // branches return the same value.
    return null;
  }

  /**
   * Tries to match an individual test failure/error line. Package-private method for internal use
   * within the sophodromos package.
   *
   * @param line the line to match
   * @param formatter the output formatter
   * @return formatted result or null if no match
   */
  @SuppressWarnings({"PMD.DataflowAnomalyAnalysis", "PMD.LawOfDemeter"})
  // False positive from OnlyOneReturn refactoring
  protected String tryMatchIndividualTest(final String line, final TestOutputFormatter formatter) {
    String result = null;
    final Matcher testMatcher = TEST_FAIL_PTN.matcher(line);
    if (testMatcher.matches()) {
      final String methodName = testMatcher.group(1);
      final String className = testMatcher.group(2);
      final double timeElapsed = Double.parseDouble(testMatcher.group(3));
      final String status = testMatcher.group(4);
      result =
          formatter.formatTestResult(className, methodName, status, (long) (timeElapsed * 1000));
    }
    return result;
  }

  /**
   * Tries to match a successful test line. Package-private method for internal use within the
   * sophodromos package.
   *
   * @param line the line to match
   * @param formatter the output formatter
   * @return formatted result or null if no match
   */
  @SuppressWarnings({"PMD.DataflowAnomalyAnalysis", "PMD.LawOfDemeter"})
  // False positive from OnlyOneReturn refactoring
  protected String tryMatchTestSuccess(final String line, final TestOutputFormatter formatter) {
    String result = null;
    final Matcher successMatcher = SUCCESS_PATTERN.matcher(line);
    if (successMatcher.matches()) {
      final String methodName = successMatcher.group(1);
      final String className = successMatcher.group(2);
      final double timeElapsed = Double.parseDouble(successMatcher.group(3));
      result =
          formatter.formatTestResult(className, methodName, "SUCCESS", (long) (timeElapsed * 1000));
    }
    return result;
  }

  /**
   * Tries to match an individual test method execution line. Package-private method for internal
   * use within the sophodromos package.
   *
   * @param line the line to match
   * @param formatter the output formatter
   * @return formatted result or null if no match
   */
  @SuppressWarnings({"PMD.DataflowAnomalyAnalysis", "PMD.LawOfDemeter"})
  // False positive from OnlyOneReturn refactoring
  protected String tryMatchMethodExecution(final String line, final TestOutputFormatter formatter) {
    String result = null;
    final Matcher methodMatcher = MTHD_EXEC_PATTERN.matcher(line);
    if (methodMatcher.matches()) {
      final String fullClassName = methodMatcher.group(1);
      final String methodName = methodMatcher.group(2);
      final double timeElapsed = Double.parseDouble(methodMatcher.group(3));
      final String className = getSimpleClassName(fullClassName);
      // Add parentheses to method name for consistency with desired output
      final String methodWithParens = methodName + "()";
      result =
          formatter.formatTestResult(
              "  " + className, methodWithParens, "SUCCESS", (long) (timeElapsed * 1000));
    }
    return result;
  }

  /**
   * Tries to match a test results summary line. Package-private method for internal use within the
   * sophodromos package.
   *
   * @param line the line to match
   * @param formatter the output formatter
   * @param executionResult the execution result to update (optional)
   * @return formatted result or null if no match
   */
  @SuppressWarnings({"PMD.DataflowAnomalyAnalysis", "PMD.LawOfDemeter"})
  // False positive from OnlyOneReturn refactoring
  protected String tryMatchTestResults(
      final String line,
      final TestOutputFormatter formatter,
      final TestExecutionResult executionResult) {
    String result = null;
    final Matcher resultMatcher = RESULT_PATTERN.matcher(line);
    if (resultMatcher.matches()) {
      final int testsRun = Integer.parseInt(resultMatcher.group(1));
      final int failures = Integer.parseInt(resultMatcher.group(2));
      final int errors = Integer.parseInt(resultMatcher.group(3));
      final int skipped = Integer.parseInt(resultMatcher.group(4));
      final double timeElapsed =
          resultMatcher.group(5) != null ? Double.parseDouble(resultMatcher.group(5)) : 0.0;

      // Update the execution result if provided
      if (executionResult != null) {
        executionResult.updateFromSurefireOutput(testsRun, failures, errors, skipped);
        executionResult.setExecutionTime((long) (timeElapsed * 1000));
      }

      // Only show the first test results summary to avoid duplicates
      if (!resultsSeen) {
        resultsSeen = true;
        result = formatTestResults(testsRun, failures, errors, skipped, timeElapsed, formatter);
      }
    }
    return result;
  }

  /**
   * Formats a module header line for displaying the current test module being executed.
   *
   * @param artifactId the Maven artifact ID
   * @param formatter the output formatter
   * @return the formatted module header
   */
  protected String formatModuleHeader(
      final String artifactId, final TestOutputFormatter formatter) {
    return colors.colorize(artifactId, AnsiColors.BOLD + AnsiColors.BRIGHT_YELLOW);
  }

  private String getSimpleClassName(final String testClass) {
    return testClass.substring(testClass.lastIndexOf('.') + 1);
  }

  @SuppressWarnings("PMD.UnusedFormalParameter")
  // Parameters intentionally unused in current implementation
  private String formatTestResults(
      final int testsRun,
      final int failures,
      final int errors,
      final int skipped,
      final double timeElapsed,
      final TestOutputFormatter formatter) {
    final StringBuilder result = new StringBuilder();

    // if (failures == 0 && errors == 0) {
    //   result.append("✅ ");
    // } else {
    //   result.append("❌ ");
    // }

    // result.append(String.format("Tests: %d", testsRun));

    // if (failures > 0) {
    //   result.append(String.format(", Failures: %d", failures));
    // }

    // if (errors > 0) {
    //   result.append(String.format(", Errors: %d", errors));
    // }

    // if (skipped > 0) {
    //   result.append(String.format(", Skipped: %d", skipped));
    // }

    // result.append(String.format(" (%.3fs)", timeElapsed));

    return formatter.formatProgressLine(result.toString());
  }
}
