package io.github.clojang.sophodromos;

import io.github.clojang.gradldromus.AnsiColors;
import io.github.clojang.gradldromus.CleanTerminalPrinter;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Handles header and footer formatting for test output.
 */
class HeaderFooterFormatter {
  private final CleanTerminalPrinter printer;
  private final AnsiColors colors;

  /**
   * Constructs a new HeaderFooterFormatter.
   *
   * @param printer the terminal printer
   * @param colors the color formatter
   */
  HeaderFooterFormatter(final CleanTerminalPrinter printer, 
      final AnsiColors colors) {
    this.printer = printer;
    this.colors = colors;
  }

  /**
   * Formats a header with the given title.
   *
   * @param title the header title
   * @return the formatted header string
   */
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

  /**
   * Formats a footer section.
   *
   * @return the formatted footer string
   */
  String formatFooter() {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final PrintStream printStream = new PrintStream(baos);

    printer.println(printStream, "");
    printer.printHeading(printStream, colors, "=", AnsiColors.BRIGHT_GREEN);

    return baos.toString();
  }
}