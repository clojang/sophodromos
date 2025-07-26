package com.sophodromos.plugin;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Intercepts and processes test execution output
 */
public class TestExecutionInterceptor {
    
    private final MavenProject project;
    private final MavenSession session;
    private final Log log;
    
    // Patterns for common test output formats
    private static final Pattern TEST_RUNNING_PATTERN = Pattern.compile("^Running (.+)$");
    private static final Pattern TEST_RESULT_PATTERN = Pattern.compile("^Tests run: (\\d+), Failures: (\\d+), Errors: (\\d+), Skipped: (\\d+), Time elapsed: ([\\d.]+) sec");
    private static final Pattern FAILURE_PATTERN = Pattern.compile("^FAILURE:.*");
    private static final Pattern ERROR_PATTERN = Pattern.compile("^ERROR:.*");
    
    public TestExecutionInterceptor(MavenProject project, MavenSession session, Log log) {
        this.project = project;
        this.session = session;
        this.log = log;
    }
    
    public String interceptTestOutput(String line) {
        // TODO: Transform standard test output here to change its format
        
        // Detect test class execution
        Matcher runningMatcher = TEST_RUNNING_PATTERN.matcher(line);
        if (runningMatcher.matches()) {
            String testClass = runningMatcher.group(1);
            return formatTestClassExecution(testClass);
        }
        
        // Detect test results
        Matcher resultMatcher = TEST_RESULT_PATTERN.matcher(line);
        if (resultMatcher.matches()) {
            return formatTestResults(
                Integer.parseInt(resultMatcher.group(1)), // tests run
                Integer.parseInt(resultMatcher.group(2)), // failures
                Integer.parseInt(resultMatcher.group(3)), // errors
                Integer.parseInt(resultMatcher.group(4)), // skipped
                Double.parseDouble(resultMatcher.group(5)) // time
            );
        }
        
        // Pass through other lines with potential modifications
        return preprocessOutputLine(line);
    }
    
    public String interceptErrorOutput(String line) {
        // TODO: Transform error output here to change its format
        
        if (FAILURE_PATTERN.matcher(line).matches()) {
            return formatFailureLine(line);
        }
        
        if (ERROR_PATTERN.matcher(line).matches()) {
            return formatErrorLine(line);
        }
        
        return preprocessErrorLine(line);
    }
    
    private String formatTestClassExecution(String testClass) {
        // TODO: Customize how test class execution is displayed
        String simpleName = testClass.substring(testClass.lastIndexOf('.') + 1);
        return String.format("🧪 Executing %s...", simpleName);
    }
    
    private String formatTestResults(int testsRun, int failures, int errors, int skipped, double timeElapsed) {
        // TODO: Customize how test results are displayed
        StringBuilder result = new StringBuilder();
        
        if (failures == 0 && errors == 0) {
            result.append("✅ ");
        } else {
            result.append("❌ ");
        }
        
        result.append(String.format("Tests: %d", testsRun));
        
        if (failures > 0) {
            result.append(String.format(", Failures: %d", failures));
        }
        
        if (errors > 0) {
            result.append(String.format(", Errors: %d", errors));
        }
        
        if (skipped > 0) {
            result.append(String.format(", Skipped: %d", skipped));
        }
        
        result.append(String.format(" (%.3fs)", timeElapsed));
        
        return result.toString();
    }
    
    private String formatFailureLine(String line) {
        // TODO: Customize how failure lines are displayed
        return "💥 " + line;
    }
    
    private String formatErrorLine(String line) {
        // TODO: Customize how error lines are displayed
        return "🚨 " + line;
    }
    
    private String preprocessOutputLine(String line) {
        // TODO: Apply general transformations to output lines here
        
        // Example: Remove Maven noise
        if (line.contains("[INFO]") || line.contains("[DEBUG]")) {
            return null; // Skip these lines
        }
        
        // Example: Enhance assertion failures
        if (line.contains("AssertionError") || line.contains("Expected") || line.contains("Actual")) {
            return "📋 " + line.trim();
        }
        
        return line;
    }
    
    private String preprocessErrorLine(String line) {
        // TODO: Apply general transformations to error lines here
        
        // Example: Clean up stack traces
        if (line.trim().startsWith("at ") && !line.contains(project.getGroupId())) {
            return null; // Skip external stack trace lines
        }
        
        return line;
    }
}
