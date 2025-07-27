package io.github.clojang.sophodromos;

import org.apache.maven.project.MavenProject;

/** Intercepts and processes test execution output using GradlDromus formatting. */
@SuppressWarnings("PMD.TestClassWithoutTestCases") // This is not a test class
public class TestExecutionInterceptor {

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
    return interceptTestOutput(line, null);
  }

  /**
   * Intercepts and processes test output lines with result tracking.
   *
   * @param line the output line to process
   * @param executionResult the test execution result to update (optional)
   * @return the formatted output line or null if the line should be skipped
   */
  public String interceptTestOutput(final String line, final TestExecutionResult executionResult) {
    String formattedLine;

    if (line == null || line.isBlank()) {
      formattedLine = line;
    } else {
      formattedLine = patternMatcher.tryMatchTestClassExecution(line, formatter);
      if (formattedLine == null) {
        formattedLine = patternMatcher.tryMatchIndividualTest(line, formatter);
      }
      if (formattedLine == null) {
        formattedLine = patternMatcher.tryMatchTestSuccess(line, formatter);
      }
      if (formattedLine == null) {
        formattedLine = patternMatcher.tryMatchTestResults(line, formatter, executionResult);
      }
      if (formattedLine == null) {
        formattedLine = lineProcessor.preprocessOutputLine(line);
      }
    }

    return formattedLine;
  }

  /**
   * Intercepts and processes error output lines.
   *
   * @param line the error line to process
   * @return the formatted error line
   */
  public String interceptErrorOutput(final String line) {
    String result;

    if (line == null || line.isBlank()) {
      result = line;
    } else {
      result = formatter.formatErrorLine(line);
    }

    return result;
  }
}
