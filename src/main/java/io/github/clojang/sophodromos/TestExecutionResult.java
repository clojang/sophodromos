package io.github.clojang.sophodromos;

import java.util.ArrayList;
import java.util.List;

/** Holds the results of test execution. */
@SuppressWarnings({"PMD.DataClass", "PMD.TestClassWithoutTestCases"}) 
// This is not a test class, it's a data holder
public class TestExecutionResult {

  private static final int MIN_PARTS_LENGTH = 1;

  private final List<String> outputLines = new ArrayList<>();
  private final List<String> errorLines = new ArrayList<>();
  private final List<String> failures = new ArrayList<>();

  private int totalTests;
  private int passedTests;
  private int failedTests;
  private int errorTests;
  private int skippedTests;
  private long executionTime;
  private int exitCode;

  /**
   * Default constructor - all fields initialized to default values.
   * Explicit constructor added to satisfy PMD AtLeastOneConstructor rule.
   */
  public TestExecutionResult() {
    // Using default initialization for all fields
  }

  /**
   * Adds an output line and analyzes it for test statistics.
   *
   * @param line the output line to add
   */
  public void addOutputLine(final String line) {
    if (line != null) {
      outputLines.add(line);
      analyzeOutputLine(line);
    }
  }

  /**
   * Adds an error line and analyzes it for failure information.
   *
   * @param line the error line to add
   */
  public void addErrorLine(final String line) {
    if (line != null) {
      errorLines.add(line);
      analyzeErrorLine(line);
    }
  }

  private void analyzeOutputLine(final String line) {
    if (line.contains("Tests run:")) {
      parseTestResults(line);
    }

    if (isTestFailure(line)) {
      failures.add(line);
    }
  }

  private void parseTestResults(final String line) {
    try {
      final String[] parts = line.split(",");
      for (final String part : parts) {
        final String trimmedPart = part.trim();
        updateTestCounts(trimmedPart);
      }
      passedTests = totalTests - failedTests - errorTests - skippedTests;
    } catch (final NumberFormatException e) {
      // Ignore parsing errors - malformed test output should not break the build
      // This is intentionally minimal logging to avoid cluttering output
      // Full logging could be added at debug level if needed for troubleshooting
    }
  }

  private void updateTestCounts(final String part) {
    if (part.startsWith("Tests run:")) {
      totalTests += extractNumber(part);
    } else if (part.startsWith("Failures:")) {
      failedTests += extractNumber(part);
    } else if (part.startsWith("Errors:")) {
      errorTests += extractNumber(part);
    } else if (part.startsWith("Skipped:")) {
      skippedTests += extractNumber(part);
    }
  }

  private int extractNumber(final String part) {
    int result = 0;
    final String[] splitPart = part.split(":");
    if (splitPart.length > MIN_PARTS_LENGTH) {
      final String numberStr = splitPart[1].trim();
      result = Integer.parseInt(numberStr);
    }
    return result;
  }

  private boolean isTestFailure(final String line) {
    return line.contains("FAILURE:") || line.contains("ERROR:");
  }

  private void analyzeErrorLine(final String line) {
    if (line.contains("FAILURE") || line.contains("ERROR")) {
      failures.add(line);
    }
  }

  // Getters
  public List<String> getOutputLines() {
    return new ArrayList<>(outputLines);
  }

  public List<String> getErrorLines() {
    return new ArrayList<>(errorLines);
  }

  public List<String> getFailures() {
    return new ArrayList<>(failures);
  }

  public int getTotalTests() {
    return totalTests;
  }

  public int getPassedTests() {
    return passedTests;
  }

  public int getFailedTests() {
    return failedTests;
  }

  public int getErrorTests() {
    return errorTests;
  }

  public int getSkippedTests() {
    return skippedTests;
  }

  public long getExecutionTime() {
    return executionTime;
  }

  public int getExitCode() {
    return exitCode;
  }

  public boolean hasFailures() {
    return failedTests > 0 || errorTests > 0 || exitCode != 0;
  }

  public int getFailureCount() {
    return failedTests;
  }

  public int getErrorCount() {
    return errorTests;
  }

  // Setters
  public void setTotalTests(final int totalTests) {
    this.totalTests = totalTests;
  }

  public void setPassedTests(final int passedTests) {
    this.passedTests = passedTests;
  }

  public void setFailedTests(final int failedTests) {
    this.failedTests = failedTests;
  }

  public void setErrorTests(final int errorTests) {
    this.errorTests = errorTests;
  }

  public void setSkippedTests(final int skippedTests) {
    this.skippedTests = skippedTests;
  }

  public void setExecutionTime(final long executionTime) {
    this.executionTime = executionTime;
  }

  public void setExitCode(final int exitCode) {
    this.exitCode = exitCode;
  }
}