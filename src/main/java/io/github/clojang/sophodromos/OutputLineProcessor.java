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
  /* package-private */ OutputLineProcessor(
      final MavenProject project, final TestOutputFormatter formatter) {
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
  /* package-private */ String preprocessOutputLine(final String line) {
    if (shouldSkipLine(line)) {
      return null;
    }

    if (isAssertionFailure(line)) {
      return formatter.formatErrorLine(line.trim());
    }

    if (isStackTrace(line)) {
      return handleStackTrace(line).orElse(null);
    }

    return line;
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
    return line.trim().startsWith("at ");
  }

  private Optional<String> handleStackTrace(final String line) {
    if (!isStackTrace(line)) {
      return Optional.of(line);
    }

    final String groupId = project.getGroupId();
    if (line.contains(groupId) || line.contains("Test")) {
      return Optional.of(formatter.formatErrorLine("  " + line.trim()));
    }

    // Skip external stack traces
    return Optional.empty();
  }
}
