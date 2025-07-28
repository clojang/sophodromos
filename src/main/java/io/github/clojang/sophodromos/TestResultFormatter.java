package io.github.clojang.sophodromos;

import io.github.clojang.gradldromus.AnsiColors;
import io.github.clojang.gradldromus.GradlDromusExtension;
import java.util.Locale;

/** Handles individual test result formatting. */
@SuppressWarnings("PMD.TestClassWithoutTestCases") // This is not a test class
class TestResultFormatter {
  private static final String INDENT = "";
  private static final int DOTS_BASE = 78;

  private final AnsiColors colors;
  private final GradlDromusExtension extension;

  /**
   * Constructs a new TestResultFormatter.
   *
   * @param colors the color formatter
   * @param extension the GradlDromus extension
   */
  protected TestResultFormatter(final AnsiColors colors, final GradlDromusExtension extension) {
    this.colors = colors;
    this.extension = extension;
  }

  /**
   * Formats an individual test result.
   *
   * @param className the test class name
   * @param methodName the test method name
   * @param status the test status
   * @param duration the test duration in milliseconds
   * @return the formatted test result
   */
  protected String formatTestResult(
      final String className, final String methodName, final String status, final long duration) {
    final StringBuilder outputStr = new StringBuilder();

    // Indent
    outputStr.append(INDENT);

    // Class name (white)
    if (className != null) {
      final String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
      outputStr.append(colors.colorize(simpleClassName + ".", AnsiColors.WHITE));
    }

    // Method name (yellow)
    outputStr.append(colors.colorize(methodName + " ", AnsiColors.YELLOW));

    // Calculate dots needed (similar to GradlDromus)
    final int dotsNeeded = calculateDotsNeeded(className, methodName);
    outputStr.append(colors.colorize(".".repeat(dotsNeeded), AnsiColors.BRIGHT_BLACK));

    // Status symbol and color
    final StatusInfo statusInfo = getStatusInfo(status);
    outputStr.append(colors.colorize(statusInfo.symbol, statusInfo.color));

    // Timing (if enabled)
    if (extension.isShowTimings() && duration > 0) {
      outputStr
          .append(' ')
          .append(colors.colorize("(" + duration + "ms)", AnsiColors.BRIGHT_BLACK));
    }

    return outputStr.toString();
  }

  @SuppressWarnings("PMD.LawOfDemeter") // Standard string operations
  private int calculateDotsNeeded(final String className, final String methodName) {
    int nameLength = INDENT.length(); // indent
    if (className != null) {
      final String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
      nameLength += simpleClassName.length() + 1;
    }
    nameLength += methodName.length() + 1;
    return Math.max(1, DOTS_BASE - nameLength);
  }

  private StatusInfo getStatusInfo(final String status) {
    StatusInfo result;
    switch (status.toUpperCase(Locale.ROOT)) {
      case "PASS":
      case "SUCCESS":
        result =
            new StatusInfo(extension.getPassSymbol(), AnsiColors.BOLD + AnsiColors.BRIGHT_GREEN);
        break;
      case "FAIL":
      case "FAILURE":
        result = new StatusInfo(extension.getFailSymbol(), AnsiColors.BOLD + AnsiColors.BRIGHT_RED);
        break;
      case "SKIP":
      case "SKIPPED":
        result =
            new StatusInfo(extension.getSkipSymbol(), AnsiColors.BOLD + AnsiColors.BRIGHT_CYAN);
        break;
      default:
        result = new StatusInfo("?", AnsiColors.YELLOW);
        break;
    }
    return result;
  }

  /** Container for status information including symbol and color. */
  protected static class StatusInfo {
    protected final String symbol;
    protected final String color;

    /**
     * Constructs a new StatusInfo.
     *
     * @param symbol the status symbol
     * @param color the status color
     */
    protected StatusInfo(final String symbol, final String color) {
      this.symbol = symbol;
      this.color = color;
    }
  }
}
