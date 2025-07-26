package io.github.clojang.sophodromos;

import io.github.clojang.gradldromus.AnsiColors;
import io.github.clojang.gradldromus.CleanTerminalPrinter;
import io.github.clojang.gradldromus.GradlDromusExtension;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Locale;

/**
 * Test output formatter that uses GradlDromus formatting classes. Delegates all formatting to the
 * proven GradlDromus implementation
 */
@SuppressWarnings("PMD.TestClassWithoutTestCases") // This is not a test class
public class TestOutputFormatter {

  private static final String INDENT = "    ";
  private static final int SUMMARY_LENGTH = 200;
  private static final int DOTS_BASE = 76;

  private final GradlDromusExtension extension;
  private final AnsiColors colors;
  private final CleanTerminalPrinter printer;
  private final HeaderFooterFormatter headerFooterFormatter;
  private final ProgressFormatter progressFormatter;
  private final TestResultFormatter testResultFormatter;

  /**
   * Constructs a new TestOutputFormatter.
   *
   * @param colorOutput whether to use colored output
   * @param detailedFailures whether to show detailed failure information
   */
  public TestOutputFormatter(final boolean colorOutput, final boolean detailedFailures) {
    // Create GradlDromus extension with our preferences
    this.extension = new GradlDromusExtension();
    this.extension.setUseColors(colorOutput);
    this.extension.setShowTimings(true);
    this.extension.setShowExceptions(detailedFailures);
    this.extension.setShowStackTraces(detailedFailures);

    this.colors = new AnsiColors(colorOutput);
    this.printer = new CleanTerminalPrinter(extension);
    this.headerFooterFormatter = new HeaderFooterFormatter(printer, colors);
    this.progressFormatter = new ProgressFormatter(colors);
    this.testResultFormatter = new TestResultFormatter(colors, extension);
  }

  /**
   * Formats a header with the given title.
   *
   * @param title the header title
   * @return the formatted header string
   */
  public String formatHeader(final String title) {
    return headerFooterFormatter.formatHeader(title);
  }

  /**
   * Formats a progress line with appropriate colors.
   *
   * @param line the line to format
   * @return the formatted line
   */
  public String formatProgressLine(final String line) {
    String result = line;
    if (line != null && !line.isBlank()) {
      result = progressFormatter.formatLineByContent(line);
    }
    return result;
  }

  /**
   * Formats an error line with appropriate styling.
   *
   * @param line the error line to format
   * @return the formatted error line
   */
  public String formatErrorLine(final String line) {
    return colors.colorize("    → " + line, AnsiColors.RED);
  }

  /**
   * Formats a summary header section.
   *
   * @return the formatted summary header
   */
  public String formatSummaryHeader() {
    return colors.colorize("\nTest Summary:", AnsiColors.BLUE)
        + "\n"
        + colors.colorize("─────────────", AnsiColors.BLUE);
  }

  /**
   * Formats a test summary with statistics.
   *
   * @param total the total number of tests
   * @param passed the number of passed tests
   * @param failed the number of failed tests
   * @param errors the number of error tests
   * @param skipped the number of skipped tests
   * @param executionTimeMs the execution time in milliseconds
   * @return the formatted summary
   */
  public String formatTestSummary(
      final int total,
      final int passed,
      final int failed,
      final int errors,
      final int skipped,
      final long executionTimeMs) {
    final StringBuilder summary = new StringBuilder(SUMMARY_LENGTH);

    summary
        .append(colors.colorize("Total: " + total + " tests, ", AnsiColors.WHITE))
        .append(
            colors.colorize(
                extension.getPassSymbol() + " " + passed + " passed, ", AnsiColors.GREEN))
        .append(
            colors.colorize(extension.getFailSymbol() + " " + failed + " failed, ", AnsiColors.RED))
        .append(
            colors.colorize(
                extension.getSkipSymbol() + " " + skipped + " skipped", AnsiColors.CYAN))
        .append('\n')
        .append(colors.colorize("Time: ", AnsiColors.WHITE))
        .append(executionTimeMs / 1000.0)
        .append('s');

    final String resultMessage = (failed == 0 && errors == 0)
        ? colors.colorize("✨ All tests passed!", AnsiColors.BRIGHT_GREEN)
        : colors.colorize("❌ Some tests failed.", AnsiColors.BRIGHT_RED);

    summary.append("\n\n").append(resultMessage);

    return summary.toString();
  }

  /**
   * Formats a failure header section.
   *
   * @return the formatted failure header
   */
  public String formatFailureHeader() {
    return colors.colorize("\nFailure Details:", AnsiColors.RED)
        + "\n"
        + colors.colorize("═══════════════", AnsiColors.RED);
  }

  /**
   * Formats a failure detail line.
   *
   * @param failure the failure message to format
   * @return the formatted failure detail
   */
  public String formatFailureDetail(final String failure) {
    return colors.colorize("    → " + failure, AnsiColors.RED);
  }

  /**
   * Formats a footer section.
   *
   * @return the formatted footer string
   */
  public String formatFooter() {
    return headerFooterFormatter.formatFooter();
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
  public String formatTestResult(
      final String className, final String methodName, final String status, final long duration) {
    return testResultFormatter.formatTestResult(className, methodName, status, duration);
  }

  // Getters to access GradlDromus configuration
  public GradlDromusExtension getExtension() {
    return extension;
  }

  public AnsiColors getColors() {
    return colors;
  }

  public CleanTerminalPrinter getPrinter() {
    return printer;
  }
}

/**
 * Handles header and footer formatting.
 */
class HeaderFooterFormatter {
  private final CleanTerminalPrinter printer;
  private final AnsiColors colors;

  HeaderFooterFormatter(final CleanTerminalPrinter printer, 
      final AnsiColors colors) {
    this.printer = printer;
    this.colors = colors;
  }

  String formatHeader(final String title) {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final PrintStream printStream = new PrintStream(baos);

    printer.println(printStream, "");
    printer.printHeading(printStream, colors, "=", AnsiColors.BRIGHT_GREEN);
    printer.println(printStream, 
        colors.colorize("Running tests with " + title, AnsiColors.GREEN));
    printer.printHeading(printStream, colors, "-", AnsiColors.BRIGHT_GREEN);

    return baos.toString();
  }

  String formatFooter() {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final PrintStream printStream = new PrintStream(baos);

    printer.println(printStream, "");
    printer.printHeading(printStream, colors, "=", AnsiColors.BRIGHT_GREEN);

    return baos.toString();
  }
}

/**
 * Handles progress line formatting with colors.
 */
class ProgressFormatter {
  private static final String INDENT = "    ";
  private final AnsiColors colors;

  ProgressFormatter(final AnsiColors colors) {
    this.colors = colors;
  }

  String formatLineByContent(final String line) {
    String result = colors.colorize(INDENT + line, AnsiColors.WHITE);
    
    if (isSuccessLine(line)) {
      result = colors.colorize(INDENT + line, AnsiColors.BRIGHT_GREEN);
    } else if (isFailureLine(line)) {
      result = colors.colorize(INDENT + line, AnsiColors.BRIGHT_RED);
    } else if (isSkipLine(line)) {
      result = colors.colorize(INDENT + line, AnsiColors.BRIGHT_CYAN);
    } else if (isExecutionLine(line)) {
      result = colors.colorize(line, AnsiColors.BOLD, AnsiColors.BRIGHT_YELLOW);
    }
    
    return result;
  }

  private boolean isSuccessLine(final String line) {
    return line.contains("✅") || line.contains("PASS") || line.contains("OK");
  }

  private boolean isFailureLine(final String line) {
    return line.contains("❌") || line.contains("FAIL") || line.contains("ERROR");
  }

  private boolean isSkipLine(final String line) {
    return line.contains("⊝") || line.contains("SKIP");
  }

  private boolean isExecutionLine(final String line) {
    return line.contains("🧪");
  }
}

/**
 * Handles individual test result formatting.
 */
class TestResultFormatter {
  private static final String INDENT = "    ";
  private static final int DOTS_BASE = 76;
  
  private final AnsiColors colors;
  private final GradlDromusExtension extension;

  TestResultFormatter(final AnsiColors colors, 
      final GradlDromusExtension extension) {
    this.colors = colors;
    this.extension = extension;
  }

  String formatTestResult(
      final String className, final String methodName, final String status, final long duration) {
    final StringBuilder outputStr = new StringBuilder();

    // Indent
    outputStr.append(INDENT);

    // Class name (white)
    if (className != null) {
      final String simpleClassName = className.substring(
          className.lastIndexOf('.') + 1);
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

  private int calculateDotsNeeded(final String className, final String methodName) {
    int nameLength = INDENT.length(); // indent
    if (className != null) {
      nameLength += className.substring(className.lastIndexOf('.') + 1).length() + 1;
    }
    nameLength += methodName.length() + 1;
    return Math.max(1, DOTS_BASE - nameLength);
  }

  private StatusInfo getStatusInfo(final String status) {
    StatusInfo result;
    switch (status.toUpperCase(Locale.ROOT)) {
      case "PASS":
      case "SUCCESS":
        result = new StatusInfo(extension.getPassSymbol(), AnsiColors.BOLD + AnsiColors.BRIGHT_GREEN);
        break;
      case "FAIL":
      case "FAILURE":
        result = new StatusInfo(extension.getFailSymbol(), AnsiColors.BOLD + AnsiColors.BRIGHT_RED);
        break;
      case "SKIP":
      case "SKIPPED":
        result = new StatusInfo(extension.getSkipSymbol(), AnsiColors.BOLD + AnsiColors.BRIGHT_CYAN);
        break;
      default:
        result = new StatusInfo("?", AnsiColors.YELLOW);
        break;
    }
  }
}