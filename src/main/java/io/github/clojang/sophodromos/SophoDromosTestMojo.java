package io.github.clojang.sophodromos;

import java.io.IOException;
import java.io.InputStream;
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
  private TestProcessManager processManager;
  private TestOutputCapture outputCapture;

  /**
   * Default constructor - dependencies are injected via Maven annotations. Explicit constructor
   * added to satisfy PMD AtLeastOneConstructor rule.
   */
  public SophoDromosTestMojo() {
    super();
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
    final Thread currentThread = Thread.currentThread();
    currentThread.interrupt();
  }

  private void logSkippedTests() {
    final Log log = getLog();
    if (log.isInfoEnabled()) {
      log.info("Tests are skipped.");
    }
  }

  private void initializeComponents() {
    formatter = new TestOutputFormatter(colorOutput, detailedFailures);
    final TestExecutionInterceptor interceptor = new TestExecutionInterceptor(project, formatter);
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
    final InputStream inputStream = process.getInputStream();
    final InputStream errorStream = process.getErrorStream();
    return new ProcessStreams(inputStream, errorStream);
  }

  private ThreadManager createThreadManager(
      final ProcessStreams streams, final TestExecutionResult result) {
    final Thread outputThread =
        outputCapture.createOutputCaptureThread(streams.getInputStream(), result);
    final Thread errorThread =
        outputCapture.createErrorCaptureThread(streams.getErrorStream(), result);
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

    /* package-private */ ProcessStreams(
        final InputStream inputStream, final InputStream errorStream) {
      this.inputStream = inputStream;
      this.errorStream = errorStream;
    }

    /* package-private */ InputStream getInputStream() {
      return inputStream;
    }

    /* package-private */ InputStream getErrorStream() {
      return errorStream;
    }
  }

  private static class ThreadManager {
    private final Thread outputThread;
    private final Thread errorThread;

    /* package-private */ ThreadManager(final Thread outputThread, final Thread errorThread) {
      this.outputThread = outputThread;
      this.errorThread = errorThread;
    }

    /* package-private */ void startThreads() {
      outputThread.start();
      errorThread.start();
    }

    /* package-private */ void waitForCompletion() throws InterruptedException {
      outputThread.join(5000);
      errorThread.join(5000);
    }
  }
}
