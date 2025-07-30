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
    defaultPhase = LifecyclePhase.NONE,
    requiresDependencyResolution = ResolutionScope.TEST,
    threadSafe = true)
@SuppressWarnings({
  "PMD.TooManyMethods",
  "PMD.TooManyFields",
  "PMD.LawOfDemeter",
  "PMD.UnnecessaryConstructor",
  "PMD.CloseResource",
  "PMD.AvoidDuplicateLiterals",
  "PMD.GodClass",
  "PMD.LocalVariableCouldBeFinal",
  "PMD.AvoidFinalLocalVariable"
})
// Standard Maven plugin patterns
public class SophoDromosTestMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;

  @Parameter(defaultValue = "${session}", readonly = true, required = true)
  private MavenSession session;

  @Parameter(property = "sophodromos.skip", defaultValue = "false")
  private boolean skipTests;

  @Parameter(property = "sophodromos.colorOutput", defaultValue = "true")
  private boolean colorOutput;

  @Parameter(property = "sophodromos.showProgress", defaultValue = "true")
  private boolean showProgress;

  @Parameter(property = "sophodromos.detailedFailures", defaultValue = "true")
  private boolean detailedFailures;

  // Display options (matching gradldromus)
  @Parameter(property = "sophodromos.showModuleNames", defaultValue = "true")
  private boolean showModuleNames;

  @Parameter(property = "sophodromos.showMethodNames", defaultValue = "true")
  private boolean showMethodNames;

  @Parameter(property = "sophodromos.showTimings", defaultValue = "true")
  private boolean showTimings;

  @Parameter(property = "sophodromos.useColors", defaultValue = "true")
  private boolean useColors;

  // Terminal settings
  @Parameter(property = "sophodromos.terminalWidth", defaultValue = "0")
  private int terminalWidth;

  @Parameter(property = "sophodromos.suppressOutput", defaultValue = "false")
  private boolean suppressOutput;

  // Custom symbols
  @Parameter(property = "sophodromos.passSymbol", defaultValue = "💚")
  private String passSymbol;

  @Parameter(property = "sophodromos.failSymbol", defaultValue = "💔")
  private String failSymbol;

  @Parameter(property = "sophodromos.skipSymbol", defaultValue = "💤")
  private String skipSymbol;

  private TestOutputFormatter formatter;
  private TestProcessManager processManager;
  private TestOutputCapture outputCapture;

  /** Default constructor. */
  public SophoDromosTestMojo() {
    super();
  }

  @Override
  @SuppressWarnings("PMD.OnlyOneReturn") // Early returns for different skip conditions
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (skipTests) {
      logSkippedTests();
      return;
    }

    // Determine execution mode: single-module or multi-module
    final boolean isMultiModule = !shouldUseSingleModuleMode();
    if (isMultiModule) {
      executeMultiModuleMode();
      return;
    }

    // Warn about potential duplicate test execution (only for single-module)
    warnAboutDuplicateExecution();

    try {
      initializeComponents();
      displayHeader();
      displayModuleHeader();
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

  private void warnAboutDuplicateExecution() {
    final Log log = getLog();
    if (log.isWarnEnabled()) {
      log.warn("⚠️  If you ran 'mvn install sd:test' or similar, tests may run twice.");
      log.warn("💡 To avoid duplicate test execution, use either:");
      log.warn("   • mvn compile sd:test  (for SophoDromos only)");
      log.warn("   • mvn install -Dmaven.test.skip=true sd:test  (skip default, run SophoDromos)");
      log.warn("   • Use -Dsophodromos.skip=true to skip SophoDromos tests if needed");
    }
  }

  /**
   * Executes sophodromos in multi-module mode with coordinated output.
   * First module shows header, last module shows summary.
   */
  private void executeMultiModuleMode() throws MojoExecutionException {
    try {
      // Determine if this is the first or last module in the reactor
      final List<MavenProject> allProjects = session.getProjects();
      final boolean isFirstModule = allProjects.get(0).equals(project);
      final boolean isLastModule = allProjects.get(allProjects.size() - 1).equals(project);

      // Initialize components
      initializeComponents();

      // Only first module shows header and warnings
      if (isFirstModule) {
        System.out.println("⚠️  Multi-module SophoDromos execution detected");
        System.out.println("💡 Individual module output will be suppressed - final summary will be shown at the end");
        System.out.println();
        displayHeader();
      }

      // All modules run tests but suppress individual summaries
      final TestExecutionResult result = executeTestsWithInterception();

      // Only last module shows the final summary
      if (isLastModule) {
        // Create aggregated summary from all modules
        System.out.println();
        System.out.println("=".repeat(80));
        System.out.println("Multi-Module Test Summary");
        System.out.println("=".repeat(80));
        displayFormattedResults(result);
      }

      // Check for failures
      checkForFailures(result);

    } catch (final IOException | InterruptedException e) {
      throw new MojoExecutionException("Failed to execute tests in multi-module mode", e);
    }
  }

  private void logSkippedTests() {
    final Log log = getLog();
    if (log.isInfoEnabled()) {
      log.info("Tests are skipped.");
    }
  }

  private void initializeComponents() {
    // Create formatter with enhanced configuration
    formatter =
        new TestOutputFormatter(
            useColors && colorOutput, // Use both old and new color flags
            detailedFailures,
            showTimings,
            passSymbol,
            failSymbol,
            skipSymbol,
            terminalWidth);

    final TestExecutionInterceptor interceptor = new TestExecutionInterceptor(project, formatter);
    processManager = new TestProcessManager(project);
    outputCapture = new TestOutputCapture(interceptor, showProgress && showMethodNames, getLog());
  }

  /** Initializes components for multi-module execution with output control. */
  private void initializeMultiModuleComponents(final boolean suppressOutput) {
    // Create formatter with enhanced configuration, but potentially suppress output
    formatter =
        new TestOutputFormatter(
            useColors && colorOutput, // Use both old and new color flags
            detailedFailures,
            showTimings,
            passSymbol,
            failSymbol,
            skipSymbol,
            terminalWidth);

    final TestExecutionInterceptor interceptor = new TestExecutionInterceptor(project, formatter);
    processManager = new TestProcessManager(project);

    // Control output based on multi-module settings
    final boolean showOutput = !suppressOutput && showProgress && showMethodNames;
    outputCapture = new TestOutputCapture(interceptor, showOutput, getLog());
  }

  /** Determines if a line is actual test output (vs Maven build output). */
  @SuppressWarnings("PMD.OnlyOneReturn") // Multiple return points for clarity
  private boolean isTestOutputLine(final String line) {
    if (line == null || line.isBlank()) {
      return false;
    }

    // Include lines that are sophodromos-formatted test output
    // These typically contain test method names or progress indicators
    return line.contains("💚")
        || line.contains("💔")
        || line.contains("💤")
        || line.contains("✨")
        || line.contains("❌")
        || line.contains(".") && line.contains("()")
        || line.contains("Test")
        || line.contains("Test Summary:")
        || line.contains("─────────────");
  }

  @SuppressWarnings("PMD.SystemPrintln") // Intentional console output for clean formatting
  private void displayHeader() {
    final String version = getSophoDromosVersion();
    final String headerMessage =
        formatter.formatHeader("SophoDromos Test Runner (version: " + version + ")");
    System.out.println(headerMessage);
  }

  @SuppressWarnings("PMD.SystemPrintln") // Intentional console output for clean formatting
  private void displayModuleHeader() {
    if (showModuleNames && shouldShowModuleNames()) {
      final String artifactId = project.getArtifactId();
      final OutputPatternMatcher patternMatcher = new OutputPatternMatcher(formatter.getColors());
      final String moduleHeader = patternMatcher.formatModuleHeader(artifactId, formatter);
      System.out.println(moduleHeader);
    }
  }

  private boolean shouldShowModuleNames() {
    // Always show module names unless user explicitly disabled them
    // The showModuleNames parameter controls this, not the execution mode
    return showModuleNames;
  }

  /**
   * Determines if we should use single-module formatting mode. Single mode should run if: 1. The
   * project is not module-based AND not part of a multi-module build, OR 2. The project is module-based but has only one module, OR 3.
   * The project is module-based with multiple modules, but -pl flag was used
   */
  @SuppressWarnings({
    "PMD.OnlyOneReturn",
    "PMD.PrematureDeclaration",
    "PMD.LongVariable",
    "PMD.DataflowAnomalyAnalysis",
    "PMD.SimplifyBooleanReturns"
  })
  private boolean shouldUseSingleModuleMode() {
    final boolean isModuleBased = isModuleBasedProject();
    final boolean hasMultipleModules = session.getProjects().size() > 1;
    final boolean usingProjectSelection = isUsingProjectSelection();
    final boolean isPartOfMultiModuleBuild = isPartOfMultiModuleBuild();

    // If this module is part of a multi-module build but is not the aggregator,
    // it should suppress its output (not use single-module mode)
    if (isPartOfMultiModuleBuild && !isModuleBased) {
      return false; // Use multi-module mode to suppress output
    }

    if (!isModuleBased) {
      // Case 1: Not module-based (single standalone project)
      return true;
    }

    if (!hasMultipleModules) {
      // Case 2: Module-based but only one module
      return true;
    }

    if (usingProjectSelection) {
      // Case 3: Multiple modules but user selected specific modules with -pl
      return true;
    }

    // Default: Multi-module mode (module-based project with multiple modules, no -pl)
    return false;
  }

  private boolean isModuleBasedProject() {
    // Use Maven's official API: aggregator projects have "pom" packaging
    // and declare modules in their POM
    return "pom".equals(project.getPackaging())
        && project.getModules() != null
        && !project.getModules().isEmpty();
  }

  /**
   * Determines if the current project is part of a multi-module build
   * by checking if it has a parent with pom packaging and multiple modules.
   */
  private boolean isPartOfMultiModuleBuild() {
    // Check if we're in a reactor build with multiple projects
    final List<MavenProject> projects = session.getProjects();
    if (projects.size() > 1) {
      return true;
    }

    // Alternative: Check if our parent project is a multi-module aggregator
    final MavenProject parent = project.getParent();
    if (parent != null) {
      return "pom".equals(parent.getPackaging())
          && parent.getModules() != null
          && parent.getModules().size() > 1;
    }

    return false;
  }

  @SuppressWarnings({"PMD.OnlyOneReturn", "PMD.LongVariable"})
  private boolean isUsingProjectSelection() {
    // Use Maven's official APIs to detect -pl flag usage
    // Compare declared modules vs actual projects in session

    final boolean hasModules = project.getModules() != null && !project.getModules().isEmpty();
    if (!hasModules) {
      return false;
    }

    // If we're in a reactor build but have fewer projects than declared modules,
    // it indicates -pl was used to select specific projects
    final int declaredModules = project.getModules().size();
    final int sessionProjects = session.getProjects().size();

    // Account for the parent project itself in the count
    final boolean hasParentInSession = session.getProjects().contains(project);
    final int expectedTotal = declaredModules + (hasParentInSession ? 1 : 0);

    return sessionProjects < expectedTotal;
  }

  @SuppressWarnings({"PMD.AvoidCatchingGenericException", "PMD.DataflowAnomalyAnalysis"})
  private String getSophoDromosVersion() {
    String result = "unknown";
    try {
      final Package pkg = this.getClass().getPackage();
      final String version = pkg.getImplementationVersion();
      if (version != null) {
        result = version;
      }
    } catch (final Exception e) {
      getLog().debug("Could not retrieve implementation version", e);
    }
    return result;
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
    // Intentionally empty - we want clean output without Maven [INFO] messages
  }

  @SuppressWarnings("PMD.SystemPrintln") // Intentional console output for clean formatting
  private void displayFormattedResults(final TestExecutionResult result) {
    final String summaryHeader = formatter.formatSummaryHeader();
    System.out.println(summaryHeader);

    final String summary =
        formatter.formatTestSummary(
            result.getTotalTests(),
            result.getPassedTests(),
            result.getFailedTests(),
            result.getErrorTests(),
            result.getSkippedTests(),
            result.getExecutionTime());

    System.out.println(summary);

    if (result.hasFailures() && detailedFailures) {
      final String failureHeader = formatter.formatFailureHeader();
      System.out.println(failureHeader);

      for (final String failure : result.getFailures()) {
        final String failureDetail = formatter.formatFailureDetail(failure);
        System.out.println(failureDetail);
      }
    }

    final String footer = formatter.formatFooter();
    System.out.println(footer);
  }

  // Helper classes to support the refactored code
  private static class ProcessStreams {
    private final InputStream inputStream;
    private final InputStream errorStream;

    protected ProcessStreams(final InputStream inputStream, final InputStream errorStream) {
      this.inputStream = inputStream;
      this.errorStream = errorStream;
    }

    protected InputStream getInputStream() {
      return inputStream;
    }

    protected InputStream getErrorStream() {
      return errorStream;
    }
  }

  private static class ThreadManager {
    private final Thread outputThread;
    private final Thread errorThread;

    protected ThreadManager(final Thread outputThread, final Thread errorThread) {
      this.outputThread = outputThread;
      this.errorThread = errorThread;
    }

    protected void startThreads() {
      outputThread.start();
      errorThread.start();
    }

    protected void waitForCompletion() throws InterruptedException {
      outputThread.join(5000);
      errorThread.join(5000);
    }
  }
}
