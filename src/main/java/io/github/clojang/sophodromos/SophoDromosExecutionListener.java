package io.github.clojang.sophodromos;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.maven.execution.AbstractExecutionListener;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;

/**
 * Maven execution listener that coordinates multi-module sophodromos execution. Handles
 * initialization of expected modules and final summary generation.
 */
@SuppressWarnings({
  "PMD.LawOfDemeter",
  "PMD.OnlyOneReturn",
  "PMD.SystemPrintln",
  "PMD.TooManyMethods",
  "PMD.AvoidLiteralsInIfCondition",
  "PMD.UselessParentheses",
  "PMD.AtLeastOneConstructor"
})
public class SophoDromosExecutionListener extends AbstractExecutionListener {

  private static final String ENABLED_KEY = "sophodromos.multimodule.enabled";
  private static final String INIT_DONE_KEY = "sophodromos.multimodule.initialized";
  private static final String TRUE_VALUE = "true";

  @Override
  public void sessionStarted(final ExecutionEvent event) {
    final MavenSession session = event.getSession();

    // Only initialize if this is a multi-module build with sophodromos
    if (isMultiModuleBuildWithSophodromos(session)) {
      initializeMultiModuleState(session);
      // Show single warning for multi-module builds
      showMultiModuleWarning(session);
    }
  }

  @Override
  public void sessionEnded(final ExecutionEvent event) {
    final MavenSession session = event.getSession();

    // Only handle if sophodromos multi-module was enabled
    if (isSophoDromosMultiModuleEnabled(session)) {
      showFinalSummary(session);
    }
  }

  @Override
  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  public void projectStarted(final ExecutionEvent event) {
    final MavenSession session = event.getSession();
    final MavenProject project = event.getProject();

    // Mark that sophodromos should suppress individual module output for ALL modules
    // Only the final consolidated summary should be shown
    if (isSophoDromosMultiModuleEnabled(session)) {
      final String moduleId = getModuleId(project);
      session
          .getUserProperties()
          .setProperty("sophodromos.suppress.output." + moduleId, TRUE_VALUE);
    }
  }

  private boolean isMultiModuleBuildWithSophodromos(final MavenSession session) {
    final List<MavenProject> projects = session.getProjects();

    // Check if this is a multi-module build (more than one project)
    if (projects.size() <= 1) {
      return false;
    }

    // Check if any project has sophodromos plugin configured
    for (final MavenProject project : projects) {
      if (hasSophoDromosPlugin(project)) {
        return true;
      }
    }

    return false;
  }

  private boolean hasSophoDromosPlugin(final MavenProject project) {
    // Check if the project has sophodromos plugin in build/plugins or build/pluginManagement
    return project.getBuildPlugins().stream()
            .anyMatch(
                plugin ->
                    "io.github.clojang".equals(plugin.getGroupId())
                        && "sophodromos".equals(plugin.getArtifactId()))
        || (project.getPluginManagement() != null
            && project.getPluginManagement().getPlugins().stream()
                .anyMatch(
                    plugin ->
                        "io.github.clojang".equals(plugin.getGroupId())
                            && "sophodromos".equals(plugin.getArtifactId())));
  }

  private void initializeMultiModuleState(final MavenSession session) {
    // Avoid double initialization
    if (TRUE_VALUE.equals(session.getUserProperties().getProperty(INIT_DONE_KEY))) {
      return;
    }

    // Mark sophodromos multi-module as enabled
    session.getUserProperties().setProperty(ENABLED_KEY, TRUE_VALUE);

    // Get all expected modules that have sophodromos
    final Set<String> expectedModules = new HashSet<>();
    for (final MavenProject project : session.getProjects()) {
      if (hasSophoDromosPlugin(project)) {
        expectedModules.add(getModuleId(project));
      }
    }

    // Initialize state through a dummy state manager
    if (!expectedModules.isEmpty()) {
      final MavenProject firstProject = session.getProjects().get(0);
      final MultiModuleStateManager stateManager =
          new MultiModuleStateManager(session, firstProject, new QuietLog());
      stateManager.initializeState(expectedModules);
    }

    session.getUserProperties().setProperty(INIT_DONE_KEY, TRUE_VALUE);
  }

  private void showFinalSummary(final MavenSession session) {
    // Get the final state and display summary
    final MavenProject firstProject = session.getProjects().get(0);
    final MultiModuleStateManager stateManager =
        new MultiModuleStateManager(session, firstProject, new QuietLog());

    final MultiModuleStateManager.MultiModuleState finalState = stateManager.getCurrentState();

    // Create and display consolidated summary
    final MultiModuleSummaryFormatter formatter = new MultiModuleSummaryFormatter();
    final String summary = formatter.formatFinalSummary(finalState);

    // Output to console
    System.out.println(summary);
  }

  private boolean isSophoDromosMultiModuleEnabled(final MavenSession session) {
    return TRUE_VALUE.equals(session.getUserProperties().getProperty(ENABLED_KEY));
  }

  private String getModuleId(final MavenProject project) {
    return project.getGroupId() + ":" + project.getArtifactId();
  }

  @SuppressWarnings({"PMD.UnusedFormalParameter", "PMD.DataflowAnomalyAnalysis"})
  private void showMultiModuleWarning(final MavenSession session) {
    // Use a quiet log since we want to use System.out for clean output
    System.out.println("⚠️  Multi-module SophoDromos execution detected");
    System.out.println(
        "💡 Individual module output will be suppressed - final summary will be shown at the end");
  }

  /** Simple logger that doesn't output anything - for internal operations. */
  private static class QuietLog implements org.apache.maven.plugin.logging.Log {
    @Override
    public boolean isDebugEnabled() {
      return false;
    }

    @Override
    public void debug(final CharSequence content) {}

    @Override
    public void debug(final CharSequence content, final Throwable error) {}

    @Override
    public void debug(final Throwable error) {}

    @Override
    public boolean isInfoEnabled() {
      return false;
    }

    @Override
    public void info(final CharSequence content) {}

    @Override
    public void info(final CharSequence content, final Throwable error) {}

    @Override
    public void info(final Throwable error) {}

    @Override
    public boolean isWarnEnabled() {
      return false;
    }

    @Override
    public void warn(final CharSequence content) {}

    @Override
    public void warn(final CharSequence content, final Throwable error) {}

    @Override
    public void warn(final Throwable error) {}

    @Override
    public boolean isErrorEnabled() {
      return false;
    }

    @Override
    public void error(final CharSequence content) {}

    @Override
    public void error(final CharSequence content, final Throwable error) {}

    @Override
    public void error(final Throwable error) {}
  }
}
