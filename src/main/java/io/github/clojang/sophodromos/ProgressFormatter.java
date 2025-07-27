package io.github.clojang.sophodromos;

import io.github.clojang.gradldromus.AnsiColors;

/** Handles progress line formatting with colors. */
class ProgressFormatter {
  private static final String INDENT = "    ";
  private final AnsiColors colors;

  /**
   * Constructs a new ProgressFormatter. Package-private constructor for internal use within the
   * sophodromos package.
   *
   * @param colors the color formatter
   */
  protected ProgressFormatter(final AnsiColors colors) {
    this.colors = colors;
  }

  /**
   * Formats a line based on its content type. Package-private method for internal use within the
   * sophodromos package.
   *
   * @param line the line to format
   * @return the formatted line with appropriate colors
   */
  protected String formatLineByContent(final String line) {
    String result;

    if (isSuccessLine(line)) {
      result = colors.colorize(INDENT + line, AnsiColors.BRIGHT_GREEN);
    } else if (isFailureLine(line)) {
      result = colors.colorize(INDENT + line, AnsiColors.BRIGHT_RED);
    } else if (isSkipLine(line)) {
      result = colors.colorize(INDENT + line, AnsiColors.BRIGHT_CYAN);
    } else if (isExecutionLine(line)) {
      result = colors.colorize(line, AnsiColors.BOLD, AnsiColors.BRIGHT_YELLOW);
    } else {
      result = colors.colorize(INDENT + line, AnsiColors.WHITE);
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
