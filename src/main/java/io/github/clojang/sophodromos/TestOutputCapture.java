package io.github.clojang.sophodromos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.apache.maven.plugin.logging.Log;

/** Captures and processes test output from input and error streams. */
@SuppressWarnings("PMD.TestClassWithoutTestCases") // This is not a test class
class TestOutputCapture {
  private final TestExecutionInterceptor interceptor;
  private final boolean showProgress;
  private final Log log;

  /**
   * Constructs a new TestOutputCapture.
   *
   * @param interceptor the test execution interceptor
   * @param showProgress whether to show progress during execution
   * @param log the Maven logger
   */
  protected TestOutputCapture(
      final TestExecutionInterceptor interceptor, final boolean showProgress, final Log log) {
    this.interceptor = interceptor;
    this.showProgress = showProgress;
    this.log = log;
  }

  /**
   * Creates a thread to capture output from the input stream.
   *
   * @param inputStream the input stream to capture
   * @param result the test execution result to populate
   * @return the capture thread
   */
  protected Thread createOutputCaptureThread(
      final InputStream inputStream, final TestExecutionResult result) {
    return new Thread(() -> processOutputStream(inputStream, result));
  }

  /**
   * Creates a thread to capture output from the error stream.
   *
   * @param errorStream the error stream to capture
   * @param result the test execution result to populate
   * @return the capture thread
   */
  protected Thread createErrorCaptureThread(
      final InputStream errorStream, final TestExecutionResult result) {
    return new Thread(() -> processErrorStream(errorStream, result));
  }

  private void processOutputStream(
      final InputStream inputStream, final TestExecutionResult result) {
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      processReaderLines(reader, result, false);
    } catch (final IOException e) {
      log.error("Error reading test output", e);
    }
  }

  private void processErrorStream(final InputStream errorStream, final TestExecutionResult result) {
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(errorStream, StandardCharsets.UTF_8))) {
      processReaderLines(reader, result, true);
    } catch (final IOException e) {
      log.error("Error reading test error output", e);
    }
  }

  private void processReaderLines(
      final BufferedReader reader, final TestExecutionResult result, final boolean isError)
      throws IOException {
    String line = reader.readLine();
    while (line != null) {
      final String formattedLine =
          isError
              ? interceptor.interceptErrorOutput(line)
              : interceptor.interceptTestOutput(line, result);

      processFormattedLine(formattedLine, result, isError);
      line = reader.readLine();
    }
  }

  @SuppressWarnings("PMD.SystemPrintln") // Intentional console output for clean formatting
  private void processFormattedLine(
      final String formattedLine, final TestExecutionResult result, final boolean isError) {
    if (formattedLine != null) {
      if (isError) {
        result.addErrorLine(formattedLine);
        System.err.println(formattedLine);
      } else {
        result.addOutputLine(formattedLine);
        logProgressIfEnabled(formattedLine);
      }
    }
  }

  @SuppressWarnings("PMD.SystemPrintln") // Intentional console output for clean formatting
  private void logProgressIfEnabled(final String formattedLine) {
    if (showProgress) {
      System.out.println(formattedLine);
    }
  }
}
