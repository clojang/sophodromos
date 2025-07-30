package io.github.clojang.sophodromos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

/**
 * Manages shared state across multiple module executions using Maven Session properties. Provides
 * thread-safe coordination between modules in a multi-module Maven build.
 */
@SuppressWarnings({
  "PMD.LawOfDemeter",
  "PMD.OnlyOneReturn",
  "PMD.DataClass",
  "PMD.GuardLogStatement",
  "PMD.AvoidLiteralsInIfCondition"
})
class MultiModuleStateManager {

  private static final String STATE_KEY = "sophodromos.multimodule.state";
  private static final String MODULES_KEY = "sophodromos.multimodule.modules";
  private static final String COMPLETED_KEY = "sophodromos.multimodule.completed";
  private static final String HEADER_SHOWN_KEY = "sophodromos.multimodule.header.shown";

  private final MavenSession session;
  private final String currentModuleId;
  private final Log log;
  private final ObjectMapper objectMapper = new ObjectMapper();

  /** Test results for a single module. */
  public static class ModuleTestResults implements Serializable {
    private static final long serialVersionUID = 1L;

    private String moduleId;
    private List<String> testOutput = new ArrayList<>();
    private long testsRun;
    private long testsPassed;
    private long testsFailed;
    private long testsSkipped;
    private double timeElapsed;

    public ModuleTestResults() {} // For JSON deserialization

    public ModuleTestResults(final String moduleId) {
      this.moduleId = moduleId;
    }

    // Getters and setters for JSON serialization
    public String getModuleId() {
      return moduleId;
    }

    public void setModuleId(final String moduleId) {
      this.moduleId = moduleId;
    }

    public List<String> getTestOutput() {
      return testOutput == null ? new ArrayList<>() : new ArrayList<>(testOutput);
    }

    public void setTestOutput(final List<String> testOutput) {
      this.testOutput = testOutput == null ? new ArrayList<>() : new ArrayList<>(testOutput);
    }

    public long getTestsRun() {
      return testsRun;
    }

    public void setTestsRun(final long testsRun) {
      this.testsRun = testsRun;
    }

    public long getTestsPassed() {
      return testsPassed;
    }

    public void setTestsPassed(final long testsPassed) {
      this.testsPassed = testsPassed;
    }

    public long getTestsFailed() {
      return testsFailed;
    }

    public void setTestsFailed(final long testsFailed) {
      this.testsFailed = testsFailed;
    }

    public long getTestsSkipped() {
      return testsSkipped;
    }

    public void setTestsSkipped(final long testsSkipped) {
      this.testsSkipped = testsSkipped;
    }

    public double getTimeElapsed() {
      return timeElapsed;
    }

    public void setTimeElapsed(final double timeElapsed) {
      this.timeElapsed = timeElapsed;
    }

    public void addTestOutput(final String line) {
      if (testOutput == null) {
        testOutput = new ArrayList<>();
      }
      testOutput.add(line);
    }

    public void setTestResults(
        final long run,
        final long passed,
        final long failed,
        final long skipped,
        final double elapsed) {
      this.testsRun = run;
      this.testsPassed = passed;
      this.testsFailed = failed;
      this.testsSkipped = skipped;
      this.timeElapsed = elapsed;
    }
  }

  /** Aggregated state for all modules. */
  public static class MultiModuleState implements Serializable {
    private static final long serialVersionUID = 1L;

    private ConcurrentMap<String, ModuleTestResults> moduleResults = new ConcurrentHashMap<>();
    private long totalTestsRun;
    private long totalTestsPassed;
    private long totalTestsFailed;
    private long totalTestsSkipped;
    private double totalTimeElapsed;

    public ConcurrentMap<String, ModuleTestResults> getModuleResults() {
      return moduleResults == null
          ? new ConcurrentHashMap<>()
          : new ConcurrentHashMap<>(moduleResults);
    }

    public void setModuleResults(final ConcurrentMap<String, ModuleTestResults> moduleResults) {
      this.moduleResults =
          moduleResults == null
              ? new ConcurrentHashMap<>()
              : new ConcurrentHashMap<>(moduleResults);
    }

    public long getTotalTestsRun() {
      return totalTestsRun;
    }

    public void setTotalTestsRun(final long totalTestsRun) {
      this.totalTestsRun = totalTestsRun;
    }

    public long getTotalTestsPassed() {
      return totalTestsPassed;
    }

    public void setTotalTestsPassed(final long totalTestsPassed) {
      this.totalTestsPassed = totalTestsPassed;
    }

    public long getTotalTestsFailed() {
      return totalTestsFailed;
    }

    public void setTotalTestsFailed(final long totalTestsFailed) {
      this.totalTestsFailed = totalTestsFailed;
    }

    public long getTotalTestsSkipped() {
      return totalTestsSkipped;
    }

    public void setTotalTestsSkipped(final long totalTestsSkipped) {
      this.totalTestsSkipped = totalTestsSkipped;
    }

    public double getTotalTimeElapsed() {
      return totalTimeElapsed;
    }

    public void setTotalTimeElapsed(final double totalTimeElapsed) {
      this.totalTimeElapsed = totalTimeElapsed;
    }

    public void addModuleResults(final ModuleTestResults results) {
      if (moduleResults == null) {
        moduleResults = new ConcurrentHashMap<>();
      }
      moduleResults.put(results.getModuleId(), results);
      updateTotals();
    }

    private void updateTotals() {
      totalTestsRun = 0;
      totalTestsPassed = 0;
      totalTestsFailed = 0;
      totalTestsSkipped = 0;
      totalTimeElapsed = 0.0;

      for (final ModuleTestResults result : moduleResults.values()) {
        totalTestsRun += result.getTestsRun();
        totalTestsPassed += result.getTestsPassed();
        totalTestsFailed += result.getTestsFailed();
        totalTestsSkipped += result.getTestsSkipped();
        totalTimeElapsed += result.getTimeElapsed();
      }
    }
  }

  /**
   * Creates a new multi-module state manager.
   *
   * @param session the Maven session
   * @param currentProject the current Maven project
   * @param log Maven logger
   */
  protected MultiModuleStateManager(
      final MavenSession session, final MavenProject currentProject, final Log log) {
    this.session = session;
    this.currentModuleId = currentProject.getGroupId() + ":" + currentProject.getArtifactId();
    this.log = log;
  }

  /**
   * Initializes the shared state with expected modules.
   *
   * @param expectedModules set of module IDs that are expected to run
   */
  protected synchronized void initializeState(final Set<String> expectedModules) {
    final Properties userProperties = session.getUserProperties();

    // Store expected modules as comma-separated string
    final String modulesStr = String.join(",", expectedModules);
    userProperties.setProperty(MODULES_KEY, modulesStr);

    // Initialize empty completed set
    userProperties.setProperty(COMPLETED_KEY, "");

    // Initialize state
    final MultiModuleState state = new MultiModuleState();
    saveState(state);

    if (log.isDebugEnabled()) {
      log.debug("Initialized multi-module state with modules: " + modulesStr);
    }
  }

  /**
   * Marks the current module as completed and stores its results.
   *
   * @param results the test results for this module
   * @return true if this is the last module to complete
   */
  protected synchronized boolean completeModule(final ModuleTestResults results) {
    final Properties userProperties = session.getUserProperties();

    // Add to completed modules
    final String completedStr = userProperties.getProperty(COMPLETED_KEY, "");
    final Set<String> completed = parseStringSet(completedStr);
    completed.add(currentModuleId);
    userProperties.setProperty(COMPLETED_KEY, String.join(",", completed));

    // Update state with results
    final MultiModuleState state = loadState();
    state.addModuleResults(results);
    saveState(state);

    // Check if all modules are complete
    final String expectedStr = userProperties.getProperty(MODULES_KEY, "");
    final Set<String> expected = parseStringSet(expectedStr);
    final boolean isLastModule = !expected.isEmpty() && completed.containsAll(expected);

    if (log.isDebugEnabled()) {
      log.debug("Module " + currentModuleId + " completed. Last module: " + isLastModule);
    }
    return isLastModule;
  }

  /**
   * Checks if the header should be shown for the current module. Only the first module should show
   * the header.
   *
   * @return true if this module should show the header
   */
  protected synchronized boolean shouldShowHeader() {
    final Properties userProperties = session.getUserProperties();
    final String headerShown = userProperties.getProperty(HEADER_SHOWN_KEY, "false");

    if (!"true".equals(headerShown)) {
      userProperties.setProperty(HEADER_SHOWN_KEY, "true");
      if (log.isDebugEnabled()) {
        log.debug("Module " + currentModuleId + " will show header");
      }
      return true;
    }

    if (log.isDebugEnabled()) {
      log.debug("Module " + currentModuleId + " will not show header");
    }
    return false;
  }

  /**
   * Gets the current shared state.
   *
   * @return the current multi-module state
   */
  protected MultiModuleState getCurrentState() {
    return loadState();
  }

  /**
   * Gets the set of expected modules.
   *
   * @return set of expected module IDs
   */
  protected Set<String> getExpectedModules() {
    final String modulesStr = session.getUserProperties().getProperty(MODULES_KEY, "");
    return parseStringSet(modulesStr);
  }

  /**
   * Gets the set of completed modules.
   *
   * @return set of completed module IDs
   */
  protected Set<String> getCompletedModules() {
    final String completedStr = session.getUserProperties().getProperty(COMPLETED_KEY, "");
    return parseStringSet(completedStr);
  }

  private MultiModuleState loadState() {
    final String stateJson = session.getUserProperties().getProperty(STATE_KEY, "{}");
    try {
      return objectMapper.readValue(stateJson, MultiModuleState.class);
    } catch (final JsonProcessingException e) {
      if (log.isWarnEnabled()) {
        log.warn("Could not deserialize multi-module state, creating new: " + e.getMessage());
      }
      return new MultiModuleState();
    }
  }

  private void saveState(final MultiModuleState state) {
    try {
      final String stateJson = objectMapper.writeValueAsString(state);
      session.getUserProperties().setProperty(STATE_KEY, stateJson);
    } catch (final JsonProcessingException e) {
      if (log.isErrorEnabled()) {
        log.error("Could not serialize multi-module state: " + e.getMessage(), e);
      }
    }
  }

  private Set<String> parseStringSet(final String str) {
    final Set<String> result = new HashSet<>();
    if (str != null && !str.isBlank()) {
      for (final String item : str.split(",")) {
        final String trimmed = item.trim();
        if (!trimmed.isEmpty()) {
          result.add(trimmed);
        }
      }
    }
    return result;
  }
}
