package io.github.clojang.sophodromos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.project.MavenProject;

/** Manages the creation and configuration of Maven Surefire test processes. */
@SuppressWarnings("PMD.TestClassWithoutTestCases") // This is not a test class
class TestProcessManager {
  private final MavenProject project;

  /**
   * Constructs a new TestProcessManager.
   *
   * @param project the Maven project
   */
  protected TestProcessManager(final MavenProject project) {
    this.project = project;
  }

  /**
   * Creates and starts a Surefire test process.
   *
   * @return the started process
   * @throws IOException if process creation fails
   */
  protected Process createSurefireProcess() throws IOException {
    final ProcessBuilder processBuilder = createSurefireProcessBuilder();
    return processBuilder.start();
  }

  private ProcessBuilder createSurefireProcessBuilder() {
    final List<String> command = new ArrayList<>();
    command.add("mvn");
    command.add("surefire:test");
    command.add("-Dmaven.test.failure.ignore=true");

    final ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.directory(project.getBasedir());
    processBuilder.redirectErrorStream(false);
    return processBuilder;
  }
}
