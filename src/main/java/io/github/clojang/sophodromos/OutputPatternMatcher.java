package io.github.clojang.sophodromos;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Handles pattern matching for different types of test output. */
class OutputPatternMatcher {
  // Patterns for common test output formats
  private static final Pattern RUNNING_PATTERN = Pattern.compile("^Running (.+)$");
  private static final Pattern RESULT_PATTERN =
      Pattern.compile(
          "^Tests run: (\\d+), Failures: (\\d+), Errors: (\\d+), "
              + "Skipped: (\\d+), Time elapsed: ([\\d.]+) sec");
  private static final Pattern INDIVID_TEST_FAIL_PTN =
      Pattern.compile(
          "^(.+?)\\((.+?)\\)\\s+Time elapsed:\\s+([\\d.]+)\\s+sec" + "\\s+<<<\\s+(FAILURE|ERROR)!");
  private static final Pattern SUCCESS_PATTERN =
      Pattern.compile("^(.+?)\\((.+?)\\)\\s+Time elapsed:\\s+([\\d.]+)\\s+sec$");

  /**
   * Default constructor for OutputPatternMatcher. Package-private constructor for internal use
   * within the sophodromos package.
   */
  /* package-private */ OutputPatternMatcher() {
    // Default constructor to satisfy PMD AtLeastOneConstructor rule
  }

  /**
   * Tries to match a test class execution line. Package-private method for internal use within the
   * sophodromos package.
   *
   * @param line the line to match
   * @param formatter the output formatter
   * @return formatted result or null if no match
   */
  /* package-private */ String tryMatchTestClassExecution(
      final String line, final TestOutputFormatter formatter) {
    final Matcher runningMatcher = RUNNING_PATTERN.matcher(line);
    if (runningMatcher.matches()) {
      final String testClass = runningMatcher.group(1);
      return formatTestClassExecution(testClass, formatter);
    }
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
  /* package-private */ String tryMatchIndividualTest(
      final String line, final TestOutputFormatter formatter) {
    final Matcher testMatcher = INDIVID_TEST_FAIL_PTN.matcher(line);
    if (testMatcher.matches()) {
      final String methodName = testMatcher.group(1);
      final String className = testMatcher.group(2);
      final double timeElapsed = Double.parseDouble(testMatcher.group(3));
      final String status = testMatcher.group(4);
      return formatter.formatTestResult(className, methodName, status, (long) (timeElapsed * 1000));
    }
    return null;
  }

  /**
   * Tries to match a successful test line. Package-private method for internal use within the
   * sophodromos package.
   *
   * @param line the line to match
   * @param formatter the output formatter
   * @return formatted result or null if no match
   */
  /* package-private */ String tryMatchTestSuccess(
      final String line, final TestOutputFormatter formatter) {
    final Matcher successMatcher = SUCCESS_PATTERN.matcher(line);
    if (successMatcher.matches()) {
      final String methodName = successMatcher.group(1);
      final String className = successMatcher.group(2);
      final double timeElapsed = Double.parseDouble(successMatcher.group(3));
      return formatter.formatTestResult(
          className, methodName, "SUCCESS", (long) (timeElapsed * 1000));
    }
    return null;
  }

  /**
   * Tries to match a test results summary line. Package-private method for internal use within the
   * sophodromos package.
   *
   * @param line the line to match
   * @param formatter the output formatter
   * @return formatted result or null if no match
   */
  /* package-private */ String tryMatchTestResults(
      final String line, final TestOutputFormatter formatter) {
    final Matcher resultMatcher = RESULT_PATTERN.matcher(line);
    if (resultMatcher.matches()) {
      return formatTestResults(
          Integer.parseInt(resultMatcher.group(1)), // tests run
          Integer.parseInt(resultMatcher.group(2)), // failures
          Integer.parseInt(resultMatcher.group(3)), // errors
          Integer.parseInt(resultMatcher.group(4)), // skipped
          Double.parseDouble(resultMatcher.group(5)), // time
          formatter);
    }
    return null;
  }

  private String formatTestClassExecution(
      final String testClass, final TestOutputFormatter formatter) {
    final String simpleName = getSimpleClassName(testClass);
    return formatter.formatProgressLine("🧪 Executing " + simpleName + "...");
  }

  private String getSimpleClassName(final String testClass) {
    return testClass.substring(testClass.lastIndexOf('.') + 1);
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
