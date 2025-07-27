package io.github.clojang.sophodromos;

import java.util.Optional;
import org.apache.maven.project.MavenProject;

/** Processes and filters output lines that don't match specific patterns. */
class OutputLineProcessor {
  private final MavenProject project;
  private final TestOutputFormatter formatter;

  /**
   * Constructs a new OutputLineProcessor. Package-private constructor for internal use within the
   * sophodromos package.
   *
   * @param project the Maven project
   * @param formatter the output formatter
   */
  protected OutputLineProcessor(final MavenProject project, final TestOutputFormatter formatter) {
    this.project = project;
    this.formatter = formatter;
  }

  /**
   * Preprocesses an output line for formatting or filtering. Package-private method for internal
   * use within the sophodromos package.
   *
   * @param line the line to process
   * @return the processed line or null if it should be skipped
   */
  @SuppressWarnings({"PMD.DataflowAnomalyAnalysis", "PMD.NullAssignment", "PMD.LawOfDemeter"})
  // False positives from OnlyOneReturn refactoring and standard method chains
  protected String preprocessOutputLine(final String line) {
    String result = line;

    if (shouldSkipLine(line)) {
      result = null;
    } else if (isAssertionFailure(line)) {
      result = formatter.formatErrorLine(line.trim());
    } else if (isStackTrace(line)) {
      result = handleStackTrace(line).orElse(null);
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

  @SuppressWarnings("PMD.LawOfDemeter") // Standard string operation
  private boolean isStackTrace(final String line) {
    final String trimmedLine = line.trim();
    return trimmedLine.startsWith("at ");
  }

  private Optional<String> handleStackTrace(final String line) {
    Optional<String> result;

    if (isStackTrace(line)) {
      final String groupId = project.getGroupId();
      if (line.contains(groupId) || line.contains("Test")) {
        result = Optional.of(formatter.formatErrorLine("  " + line.trim()));
      } else {
        // Skip external stack traces
        result = Optional.empty();
      }
    } else {
      result = Optional.of(line);
    }

    return result;
  }
}
