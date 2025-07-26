package com.sophodromos.plugin;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SophoDromos Test Mojo - Provides GradlDromus-style test formatting for Maven
 */
@Mojo(
    name = "test",
    defaultPhase = LifecyclePhase.TEST,
    requiresDependencyResolution = ResolutionScope.TEST,
    threadSafe = true
)
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

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skipTests) {
            getLog().info("Tests are skipped.");
            return;
        }

        try {
            formatter = new TestOutputFormatter(colorOutput, detailedFailures);
            interceptor = new TestExecutionInterceptor(project, session, getLog(), formatter);
            
            System.out.println(formatter.formatHeader("SophoDromos Test Runner (powered by GradlDromus)"));
            
            // Execute tests with interception
            TestExecutionResult result = executeTestsWithInterception();
            
            // Format and display results
            displayFormattedResults(result);
            
            // Fail build if tests failed
            if (result.hasFailures()) {
                throw new MojoFailureException("Tests failed: " + result.getFailureCount() + " failures, " + result.getErrorCount() + " errors");
            }
            
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to execute tests", e);
        }
    }

    private TestExecutionResult executeTestsWithInterception() throws Exception {
        getLog().info("Starting test execution...");
        
        // Create process builder for surefire execution
        ProcessBuilder pb = createSurefireProcessBuilder();
        
        // Start process and capture output
        Process process = pb.start();
        
        TestExecutionResult result = new TestExecutionResult();
        
        // Create output capture threads
        Thread outputThread = createOutputCaptureThread(process.getInputStream(), result);
        Thread errorThread = createErrorCaptureThread(process.getErrorStream(), result);
        
        outputThread.start();
        errorThread.start();
        
        // Wait for process completion
        int exitCode = process.waitFor();
        
        // Wait for output threads to complete
        outputThread.join(5000);
        errorThread.join(5000);
        
        result.setExitCode(exitCode);
        return result;
    }

    private ProcessBuilder createSurefireProcessBuilder() {
        List<String> command = new ArrayList<>();
        
        command.add("mvn");
        command.add("surefire:test");
        command.add("-q"); // Quiet mode to reduce noise
        command.add("-Dmaven.test.failure.ignore=true"); // Don't fail immediately on test failures
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(project.getBasedir());
        pb.redirectErrorStream(false);
        
        return pb;
    }

    private Thread createOutputCaptureThread(InputStream inputStream, TestExecutionResult result) {
        return new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String formattedLine = interceptor.interceptTestOutput(line);
                    if (formattedLine != null) {
                        result.addOutputLine(formattedLine);
                        
                        if (showProgress) {
                            System.out.println(formattedLine);
                        }
                    }
                }
            } catch (IOException e) {
                getLog().error("Error reading test output", e);
            }
        });
    }

    private Thread createErrorCaptureThread(InputStream errorStream, TestExecutionResult result) {
        return new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String formattedLine = interceptor.interceptErrorOutput(line);
                    if (formattedLine != null) {
                        result.addErrorLine(formattedLine);
                        System.err.println(formattedLine);
                    }
                }
            } catch (IOException e) {
                getLog().error("Error reading test error output", e);
            }
        });
    }

    private void displayFormattedResults(TestExecutionResult result) {
        System.out.println(formatter.formatSummaryHeader());
        
        String summary = formatter.formatTestSummary(
            result.getTotalTests(),
            result.getPassedTests(),
            result.getFailedTests(),
            result.getErrorTests(),
            result.getSkippedTests(),
            result.getExecutionTime()
        );
        
        System.out.println(summary);
        
        if (result.hasFailures() && detailedFailures) {
            System.out.println(formatter.formatFailureHeader());
            
            for (String failure : result.getFailures()) {
                System.out.println(formatter.formatFailureDetail(failure));
            }
        }
        
        System.out.println(formatter.formatFooter());
    }
}
