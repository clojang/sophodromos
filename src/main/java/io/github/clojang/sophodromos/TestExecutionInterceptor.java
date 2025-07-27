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
    if (line == null || line.isBlank()) {
      return line;
    }

    String result = patternMatcher.tryMatchTestClassExecution(line, formatter);
    if (result != null) {
      return result;
    }

    result = patternMatcher.tryMatchIndividualTest(line, formatter);
    if (result != null) {
      return result;
    }

    result = patternMatcher.tryMatchTestSuccess(line, formatter);
    if (result != null) {
      return result;
    }

    result = patternMatcher.tryMatchTestResults(line, formatter);
    if (result != null) {
      return result;
    }

    return lineProcessor.preprocessOutputLine(line);
  }

  /**
   * Intercepts and processes error output lines.
   *
   * @param line the error line to process
   * @return the formatted error line
   */
  public String interceptErrorOutput(final String line) {
    if (line == null || line.isBlank()) {
      return line;
    }
    return formatter.formatErrorLine(line);
  }
}
