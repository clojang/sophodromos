package io.github.clojang.sophodromos;

import java.util.ArrayList;
import java.util.List;

/** Holds the results of test execution. */
public class TestExecutionResult {

  private final List<String> outputLines = new ArrayList<>();
  private final List<String> errorLines = new ArrayList<>();
  private final List<String> failures = new ArrayList<>();

  private int totalTests = 0;
  private int passedTests = 0;
  private int failedTests = 0;
  private int errorTests = 0;
  private int skippedTests = 0;
  private long executionTime = 0;
  private int exitCode = 0;

  /**
   * Adds an output line and analyzes it for test statistics.
   *
   * @param line the output line to add
   */
  public void addOutputLine(String line) {
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
  public void addErrorLine(String line) {
    if (line != null) {
      errorLines.add(line);
      analyzeErrorLine(line);
    }
  }

  private void analyzeOutputLine(String line) {
    // TODO: Analyze output lines here to extract test statistics

    // Simple pattern matching for common test frameworks
    if (line.contains("Tests run:")) {
      // Extract test counts from surefire output
      // Format: "Tests run: 5, Failures: 1, Errors: 0, Skipped: 0"
      try {
        String[] parts = line.split(",");
        for (String part : parts) {
          part = part.trim();
          if (part.startsWith("Tests run:")) {
            totalTests += Integer.parseInt(part.split(":")[1].trim());
          } else if (part.startsWith("Failures:")) {
            failedTests += Integer.parseInt(part.split(":")[1].trim());
          } else if (part.startsWith("Errors:")) {
            errorTests += Integer.parseInt(part.split(":")[1].trim());
          } else if (part.startsWith("Skipped:")) {
            skippedTests += Integer.parseInt(part.split(":")[1].trim());
          }
        }
        passedTests = totalTests - failedTests - errorTests - skippedTests;
      } catch (NumberFormatException e) {
        // Ignore parsing errors
      }
    }

    // Detect individual test failures
    if (line.contains("FAILURE:") || line.contains("ERROR:")) {
      failures.add(line);
    }
  }

  private void analyzeErrorLine(String line) {
    // TODO: Analyze error lines here to extract failure information

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
  public void setTotalTests(int totalTests) {
    this.totalTests = totalTests;
  }

  public void setPassedTests(int passedTests) {
    this.passedTests = passedTests;
  }

  public void setFailedTests(int failedTests) {
    this.failedTests = failedTests;
  }

  public void setErrorTests(int errorTests) {
    this.errorTests = errorTests;
  }

  public void setSkippedTests(int skippedTests) {
    this.skippedTests = skippedTests;
  }

  public void setExecutionTime(long executionTime) {
    this.executionTime = executionTime;
  }

  public void setExitCode(int exitCode) {
    this.exitCode = exitCode;
  }
}
