package io.github.clojang.sophodromos;

import io.github.clojang.gradldromus.AnsiColors;
import io.github.clojang.gradldromus.CleanTerminalPrinter;
import io.github.clojang.gradldromus.GradlDromusExtension;

/**
 * Test output formatter that uses GradlDromus formatting classes. Delegates all formatting to the
 * proven GradlDromus implementation.
 */
@SuppressWarnings("PMD.TestClassWithoutTestCases") // This is not a test class
public class TestOutputFormatter {

  private static final int SUMMARY_LENGTH = 200;
  private static final double MS_TO_SECONDS = 1000.0;

  private final GradlDromusExtension extension;
  private final AnsiColors colors;
  private final CleanTerminalPrinter printer;
  private final HeaderFooterFormatter headerFormatter;
  private final ProgressFormatter progressFormatter;
  private final TestResultFormatter resultFormatter;

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
    this.headerFormatter = new HeaderFooterFormatter(printer, colors);
    this.progressFormatter = new ProgressFormatter(colors);
    this.resultFormatter = new TestResultFormatter(colors, extension);
  }

  /**
   * Formats a header with the given title.
   *
   * @param title the header title
   * @return the formatted header string
   */
  public String formatHeader(final String title) {
    return headerFormatter.formatHeader(title);
  }

  /**
   * Formats a progress line with appropriate colors.
   *
   * @param line the line to format
   * @return the formatted line
   */
  @SuppressWarnings("PMD.DataflowAnomalyAnalysis") // False positive from OnlyOneReturn refactoring
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
        .append(
            colors.colorize("Time: " + (executionTimeMs / MS_TO_SECONDS) + "s", AnsiColors.WHITE));

    final String resultMessage =
        failed == 0 && errors == 0
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
    return headerFormatter.formatFooter();
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
    return resultFormatter.formatTestResult(className, methodName, status, duration);
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
