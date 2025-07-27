package io.github.clojang.sophodromos;

import io.github.clojang.gradldromus.AnsiColors;
import io.github.clojang.gradldromus.CleanTerminalPrinter;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/** Handles header and footer formatting for test output. */
class HeaderFooterFormatter {
  private final CleanTerminalPrinter printer;
  private final AnsiColors colors;

  /**
   * Constructs a new HeaderFooterFormatter. Package-private constructor for internal use within the
   * sophodromos package.
   *
   * @param printer the terminal printer
   * @param colors the color formatter
   */
  protected HeaderFooterFormatter(final CleanTerminalPrinter printer, final AnsiColors colors) {
    this.printer = printer;
    this.colors = colors;
  }

  /**
   * Formats a header with the given title. Package-private method for internal use within the
   * sophodromos package.
   *
   * @param title the header title
   * @return the formatted header string
   */
  protected String formatHeader(final String title) {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final PrintStream printStream = new PrintStream(baos, true, StandardCharsets.UTF_8);

    printer.println(printStream, "");
    printer.printHeading(printStream, colors, "=", AnsiColors.BRIGHT_GREEN);
    printer.println(printStream, colors.colorize("Running tests with " + title, AnsiColors.GREEN));
    printer.printHeading(printStream, colors, "-", AnsiColors.BRIGHT_GREEN);

    return baos.toString(StandardCharsets.UTF_8);
  }

  /**
   * Formats a footer section. Package-private method for internal use within the sophodromos
   * package.
   *
   * @return the formatted footer string
   */
  protected String formatFooter() {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final PrintStream printStream = new PrintStream(baos, true, StandardCharsets.UTF_8);

    printer.println(printStream, "");
    printer.printHeading(printStream, colors, "=", AnsiColors.BRIGHT_GREEN);

    return baos.toString(StandardCharsets.UTF_8);
  }
}
