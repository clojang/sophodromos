package io.github.clojang.sophodromos;

import org.apache.maven.project.MavenProject;

/**
 * Processes and filters output lines that don't match specific patterns.
 */
class OutputLineProcessor {
  private final MavenProject project;
  private final TestOutputFormatter formatter;

  /**
   * Constructs a new OutputLineProcessor.
   *
   * @param project the Maven project
   * @param formatter the output formatter
   */
  OutputLineProcessor(final MavenProject project, 
      final TestOutputFormatter formatter) {
    this.project = project;
    this.formatter = formatter;
  }

  /**
   * Preprocesses an output line for formatting or filtering.
   *
   * @param line the line to process
   * @return the processed line or null if it should be skipped
   */
  String preprocessOutputLine(final String line) {
    String result = line;

    if (shouldSkipLine(line)) {
      result = null;
    } else if (isAssertionFailure(line)) {
      result = formatter.formatErrorLine(line.trim());
    } else if (isStackTrace(line)) {
      result = handleStackTrace(line);
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
    return line.contains("AssertionError") 
        || line.contains("Expected") 
        || line.contains("Actual");
  }

  private boolean isStackTrace(final String line) {
    final String trimmedLine = line.trim();
    return trimmedLine.startsWith("at ");
  }

  private String handleStackTrace(final String line) {
    String result = line;
    if (isStackTrace(line)) {
      final String groupId = project.getGroupId();
      if (line.contains(groupId) || line.contains("Test")) {
        result = formatter.formatErrorLine("  " + line.trim());
      } else {
        result = null; // Skip external stack traces
      }
    }
    return result;
  }
}