package io.github.clojang.sophodromos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/** SophoDromos Test Mojo - Provides GradlDromus-style test formatting for Maven. */
@Mojo(
    name = "test",
    defaultPhase = LifecyclePhase.TEST,
    requiresDependencyResolution = ResolutionScope.TEST,
    threadSafe = true)
public class SophoDromosTestMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;

  @Parameter(defaultValue = "${session}", readonly = true, required = true)
  private MavenSession session;

  @Parameter(property = "maven.test.skip", defaultValue = "false")
  private boolean skipTests;

  @Parameter(property = "sophodromos.colorOutput", defaultValue = "true")
  private boolean colorOutput;

  @Parameter(property = "sophodromos.showProgress", defaultValue = "true")
  private boolean showProgress;

  @Parameter(property = "sophodromos.detailedFailures", defaultValue = "true")
  private boolean detailedFailures;

  private TestOutputFormatter formatter;
  private TestExecutionInterceptor interceptor;
  private TestProcessManager processManager;
  private TestOutputCapture outputCapture;

  /**
   * Default constructor - dependencies are injected via Maven annotations.
   * Explicit constructor added to satisfy PMD AtLeastOneConstructor rule.
   */
  public SophoDromosTestMojo() {
    // Dependencies injected via Maven annotations
  }

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (skipTests) {
      logSkippedTests();
      return;
    }

    try {
      initializeComponents();
      displayHeader();
      final TestExecutionResult result = executeTestsWithInterception();
      displayFormattedResults(result);
      checkForFailures(result);
    } catch (final InterruptedException e) {
      handleInterruption();
      throw new MojoExecutionException("Test execution was interrupted", e);
    } catch (final IOException e) {
      throw new MojoExecutionException("IO error during test execution", e);
    }
  }

  private void handleInterruption() {
    Thread.currentThread().interrupt();
  }

  private void logSkippedTests() {
    final Log log = getLog();
    if (log.isInfoEnabled()) {
      log.info("Tests are skipped.");
    }
  }

  private void initializeComponents() {
    formatter = new TestOutputFormatter(colorOutput, detailedFailures);
    interceptor = new TestExecutionInterceptor(project, formatter);
    processManager = new TestProcessManager(project);
    outputCapture = new TestOutputCapture(interceptor, showProgress, getLog());
  }

  private void displayHeader() {
    final Log log = getLog();
    if (log.isInfoEnabled()) {
      final String headerMessage =
          formatter.formatHeader("SophoDromos Test Runner (powered by GradlDromus)");
      log.info(headerMessage);
    }
  }

  private void checkForFailures(final TestExecutionResult result) throws MojoFailureException {
    if (result.hasFailures()) {
      throw new MojoFailureException(
          "Tests failed: "
              + result.getFailureCount()
              + " failures, "
              + result.getErrorCount()
              + " errors");
    }
  }

  private TestExecutionResult executeTestsWithInterception()
      throws IOException, InterruptedException {
    logTestExecutionStart();

    final Process process = processManager.createSurefireProcess();
    final ProcessStreams streams = getProcessStreams(process);
    final TestExecutionResult result = new TestExecutionResult();

    final ThreadManager threadManager = createThreadManager(streams, result);
    threadManager.startThreads();

    final int exitCode = process.waitFor();
    threadManager.waitForCompletion();

    result.setExitCode(exitCode);
    return result;
  }

  private ProcessStreams getProcessStreams(final Process process) {
    return new ProcessStreams(process.getInputStream(), process.getErrorStream());
  }

  private ThreadManager createThreadManager(final ProcessStreams streams, 
      final TestExecutionResult result) {
    final Thread outputThread = outputCapture.createOutputCaptureThread(
        streams.getInputStream(), result);
    final Thread errorThread = outputCapture.createErrorCaptureThread(
        streams.getErrorStream(), result);
    return new ThreadManager(outputThread, errorThread);
  }

  private void logTestExecutionStart() {
    final Log log = getLog();
    if (log.isInfoEnabled()) {
      log.info("Starting test execution...");
    }
  }

  private void displayFormattedResults(final TestExecutionResult result) {
    final Log log = getLog();
    if (log.isInfoEnabled()) {
      final String summaryHeader = formatter.formatSummaryHeader();
      log.info(summaryHeader);

      final String summary =
          formatter.formatTestSummary(
              result.getTotalTests(),
              result.getPassedTests(),
              result.getFailedTests(),
              result.getErrorTests(),
              result.getSkippedTests(),
              result.getExecutionTime());

      log.info(summary);

      if (result.hasFailures() && detailedFailures) {
        final String failureHeader = formatter.formatFailureHeader();
        log.info(failureHeader);

        for (final String failure : result.getFailures()) {
          final String failureDetail = formatter.formatFailureDetail(failure);
          log.info(failureDetail);
        }
      }

      final String footer = formatter.formatFooter();
      log.info(footer);
    }
  }

  // Helper classes to support the refactored code
  private static class ProcessStreams {
    private final InputStream inputStream;
    private final InputStream errorStream;

    ProcessStreams(final InputStream inputStream, final InputStream errorStream) {
      this.inputStream = inputStream;
      this.errorStream = errorStream;
    }

    InputStream getInputStream() {
      return inputStream;
    }

    InputStream getErrorStream() {
      return errorStream;
    }
  }

  private static class ThreadManager {
    private final Thread outputThread;
    private final Thread errorThread;

    ThreadManager(final Thread outputThread, final Thread errorThread) {
      this.outputThread = outputThread;
      this.errorThread = errorThread;
    }

    void startThreads() {
      outputThread.start();
      errorThread.start();
    }

    void waitForCompletion() throws InterruptedException {
      outputThread.join(5000);
      errorThread.join(5000);
    }
  }
}

// Supporting classes that would be created in separate files
class TestProcessManager {
  private final MavenProject project;

  TestProcessManager(final MavenProject project) {
    this.project = project;
  }

  Process createSurefireProcess() throws IOException {
    final ProcessBuilder processBuilder = createSurefireProcessBuilder();
    return processBuilder.start();
  }

  private ProcessBuilder createSurefireProcessBuilder() {
    final List<String> command = new ArrayList<>();
    command.add("mvn");
    command.add("surefire:test");
    command.add("-q");
    command.add("-Dmaven.test.failure.ignore=true");

    final ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.directory(project.getBasedir());
    processBuilder.redirectErrorStream(false);
    return processBuilder;
  }
}

class TestOutputCapture {
  private final TestExecutionInterceptor interceptor;
  private final boolean showProgress;
  private final Log log;

  TestOutputCapture(final TestExecutionInterceptor interceptor, 
      final boolean showProgress, final Log log) {
    this.interceptor = interceptor;
    this.showProgress = showProgress;
    this.log = log;
  }

  Thread createOutputCaptureThread(final InputStream inputStream, 
      final TestExecutionResult result) {
    return new Thread(() -> processOutputStream(inputStream, result));
  }

  Thread createErrorCaptureThread(final InputStream errorStream, 
      final TestExecutionResult result) {
    return new Thread(() -> processErrorStream(errorStream, result));
  }

  private void processOutputStream(final InputStream inputStream, 
      final TestExecutionResult result) {
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

  private void processReaderLines(final BufferedReader reader, 
      final TestExecutionResult result, final boolean isError) 
      throws IOException {
    String line = reader.readLine();
    while (line != null) {
      final String formattedLine = isError
          ? interceptor.interceptErrorOutput(line)
          : interceptor.interceptTestOutput(line);
      
      processFormattedLine(formattedLine, result, isError);
      line = reader.readLine();
    }
  }

  private void processFormattedLine(final String formattedLine, 
      final TestExecutionResult result, final boolean isError) {
    if (formattedLine != null) {
      if (isError) {
        result.addErrorLine(formattedLine);
        log.error(formattedLine);
      } else {
        result.addOutputLine(formattedLine);
        logProgressIfEnabled(formattedLine);
      }
    }
  }

  private void logProgressIfEnabled(final String formattedLine) {
    if (showProgress && log.isInfoEnabled()) {
      log.info(formattedLine);
    }
  }
}