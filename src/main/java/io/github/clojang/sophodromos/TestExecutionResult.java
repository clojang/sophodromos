package io.github.clojang.sophodromos;

import java.util.ArrayList;
import java.util.List;

/** Tracks the results of test execution including counts, failures, and timing. */
@SuppressWarnings({
  "PMD.TestClassWithoutTestCases", // This is not a test class
  "PMD.DataClass", // This is a legitimate data transfer object
  "PMD.UnnecessaryConstructor" // Constructor required to satisfy AtLeastOneConstructor rule
})
public class TestExecutionResult {

  private int totalTests;
  private int passedTests;
  private int failedTests;
  private int errorTests;
  private int skippedTests;
  private long executionTime;
  private int exitCode;
  private final List<String> failures = new ArrayList<>();
  private final List<String> outputLines = new ArrayList<>();
  private final List<String> errorLines = new ArrayList<>();

  /** Default constructor. */
  public TestExecutionResult() {
    // Explicit default constructor to satisfy PMD
  }

  /**
   * Gets the total number of tests.
   *
   * @return total test count
   */
  public int getTotalTests() {
    return totalTests;
  }

  /**
   * Gets the number of passed tests.
   *
   * @return passed test count
   */
  public int getPassedTests() {
    return passedTests;
  }

  /**
   * Gets the number of failed tests.
   *
   * @return failed test count
   */
  public int getFailedTests() {
    return failedTests;
  }

  /**
   * Gets the number of error tests.
   *
   * @return error test count
   */
  public int getErrorTests() {
    return errorTests;
  }

  /**
   * Gets the number of skipped tests.
   *
   * @return skipped test count
   */
  public int getSkippedTests() {
    return skippedTests;
  }

  /**
   * Gets the total execution time in milliseconds.
   *
   * @return execution time
   */
  public long getExecutionTime() {
    return executionTime;
  }

  /**
   * Gets the exit code of the test process.
   *
   * @return exit code
   */
  public int getExitCode() {
    return exitCode;
  }

  /**
   * Gets the list of failure messages.
   *
   * @return failure messages
   */
  public List<String> getFailures() {
    return new ArrayList<>(failures);
  }

  /**
   * Checks if there are any failures or errors.
   *
   * @return true if there are failures
   */
  public boolean hasFailures() {
    return failedTests > 0 || errorTests > 0;
  }

  /**
   * Gets the total number of failed and error tests.
   *
   * @return failure count
   */
  public int getFailureCount() {
    return failedTests + errorTests;
  }

  /**
   * Gets the number of error tests (alias for getErrorTests).
   *
   * @return error count
   */
  public int getErrorCount() {
    return errorTests;
  }

  /**
   * Sets the total number of tests.
   *
   * @param total total test count
   */
  public void setTotalTests(final int total) {
    this.totalTests = total;
  }

  /**
   * Sets the number of passed tests.
   *
   * @param passed passed test count
   */
  public void setPassedTests(final int passed) {
    this.passedTests = passed;
  }

  /**
   * Sets the number of failed tests.
   *
   * @param failed failed test count
   */
  public void setFailedTests(final int failed) {
    this.failedTests = failed;
  }

  /**
   * Sets the number of error tests.
   *
   * @param errors error test count
   */
  public void setErrorTests(final int errors) {
    this.errorTests = errors;
  }

  /**
   * Sets the number of skipped tests.
   *
   * @param skipped skipped test count
   */
  public void setSkippedTests(final int skipped) {
    this.skippedTests = skipped;
  }

  /**
   * Sets the execution time in milliseconds.
   *
   * @param time execution time
   */
  public void setExecutionTime(final long time) {
    this.executionTime = time;
  }

  /**
   * Sets the exit code.
   *
   * @param code exit code
   */
  public void setExitCode(final int code) {
    this.exitCode = code;
  }

  /**
   * Adds a failure message.
   *
   * @param failure failure message
   */
  public void addFailure(final String failure) {
    if (failure != null && !failure.isBlank()) {
      failures.add(failure);
    }
  }

  /**
   * Updates test counts from parsed Surefire output.
   *
   * @param testsRun total tests run
   * @param failures number of failures
   * @param errors number of errors
   * @param skipped number of skipped tests
   */
  public void updateFromSurefireOutput(
      final int testsRun, final int failures, final int errors, final int skipped) {
    this.totalTests = testsRun;
    this.failedTests = failures;
    this.errorTests = errors;
    this.skippedTests = skipped;
    this.passedTests = testsRun - failures - errors - skipped;
  }

  /**
   * Adds an output line from the test execution.
   *
   * @param line output line
   */
  public void addOutputLine(final String line) {
    if (line != null) {
      outputLines.add(line);
    }
  }

  /**
   * Adds an error line from the test execution.
   *
   * @param line error line
   */
  public void addErrorLine(final String line) {
    if (line != null) {
      errorLines.add(line);
    }
  }

  /**
   * Gets all output lines.
   *
   * @return output lines
   */
  public List<String> getOutputLines() {
    return new ArrayList<>(outputLines);
  }

  /**
   * Gets all error lines.
   *
   * @return error lines
   */
  public List<String> getErrorLines() {
    return new ArrayList<>(errorLines);
  }
}
