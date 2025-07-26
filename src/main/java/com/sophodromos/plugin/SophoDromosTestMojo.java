package com.sophodromos.plugin;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * SophoDromos Test Mojo - Intercepts and formats test execution output
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

        // Initialize ANSI console for colored output
        if (colorOutput) {
            AnsiConsole.systemInstall();
        }

        try {
            formatter = new TestOutputFormatter(colorOutput, detailedFailures);
            interceptor = new TestExecutionInterceptor(project, session, getLog());
            
            getLog().info(formatter.formatHeader("SophoDromos Test Runner"));
            
            // Execute tests with interception
            TestExecutionResult result = executeTestsWithInterception();
            
            // Format and display results
            displayFormattedResults(result);
            
            // Fail build if tests failed
            if (result.hasFailures()) {
                throw new MojoFailureException("Tests failed: " + result.getFailureCount() + " failures, " + result.getErrorCount() + " errors");
            }
            
        } finally {
            if (colorOutput) {
                AnsiConsole.systemUninstall();
            }
        }
    }

    private TestExecutionResult executeTestsWithInterception() throws MojoExecutionException {
        try {
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
            
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to execute tests", e);
        }
    }

    private ProcessBuilder createSurefireProcessBuilder() {
        List<String> command = new ArrayList<>();
        
        // TODO: Configure Maven command with surefire plugin
        // Add your custom Maven execution parameters here
        command.add("mvn");
        command.add("surefire:test");
        command.add("-q"); // Quiet mode to reduce noise
        
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
                    // TODO: Transform test output here to change its format
                    String formattedLine = interceptor.interceptTestOutput(line);
                    result.addOutputLine(formattedLine);
                    
                    if (showProgress) {
                        System.out.println(formatter.formatProgressLine(formattedLine));
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
                    // TODO: Transform error output here to change its format
                    String formattedLine = interceptor.interceptErrorOutput(line);
                    result.addErrorLine(formattedLine);
                    
                    System.err.println(formatter.formatErrorLine(formattedLine));
                }
            } catch (IOException e) {
                getLog().error("Error reading test error output", e);
            }
        });
    }

    private void displayFormattedResults(TestExecutionResult result) {
        getLog().info("");
        getLog().info(formatter.formatSummaryHeader());
        
        // TODO: Customize result summary formatting here
        String summary = formatter.formatTestSummary(
            result.getTotalTests(),
            result.getPassedTests(),
            result.getFailedTests(),
            result.getErrorTests(),
            result.getSkippedTests(),
            result.getExecutionTime()
        );
        
        getLog().info(summary);
        
        if (result.hasFailures() && detailedFailures) {
            getLog().info("");
            getLog().info(formatter.formatFailureHeader());
            
            for (String failure : result.getFailures()) {
                // TODO: Transform failure output here to change its format
                getLog().info(formatter.formatFailureDetail(failure));
            }
        }
        
        getLog().info(formatter.formatFooter());
    }
}
