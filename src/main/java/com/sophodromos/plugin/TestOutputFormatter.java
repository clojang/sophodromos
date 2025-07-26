package com.sophodromos.plugin;

import io.github.clojang.gradldromus.AnsiColors;
import io.github.clojang.gradldromus.CleanTerminalPrinter;
import io.github.clojang.gradldromus.GradlDromusExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Test output formatter that uses GradlDromus formatting classes
 * Delegates all formatting to the proven GradlDromus implementation
 */
public class TestOutputFormatter {
    
    private final GradlDromusExtension extension;
    private final AnsiColors colors;
    private final CleanTerminalPrinter printer;
    private final boolean detailedFailures;
    
    public TestOutputFormatter(boolean colorOutput, boolean detailedFailures) {
        this.detailedFailures = detailedFailures;
        
        // Create GradlDromus extension with our preferences
        this.extension = new GradlDromusExtension();
        this.extension.setUseColors(colorOutput);
        this.extension.setShowTimings(true);
        this.extension.setShowExceptions(detailedFailures);
        this.extension.setShowStackTraces(detailedFailures);
        
        this.colors = new AnsiColors(colorOutput);
        this.printer = new CleanTerminalPrinter(extension);
    }
    
    public String formatHeader(String title) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        
        printer.println(ps, "");
        printer.printHeading(ps, colors, "=", AnsiColors.BRIGHT_GREEN);
        printer.println(ps, colors.colorize("Running tests with " + title, AnsiColors.GREEN));
        printer.printHeading(ps, colors, "-", AnsiColors.BRIGHT_GREEN);
        
        return baos.toString();
    }
    
    public String formatProgressLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return line;
        }
        
        // Use GradlDromus color scheme for different test states
        if (line.contains("✅") || line.contains("PASS") || line.contains("OK")) {
            return colors.colorize("    " + line, AnsiColors.BRIGHT_GREEN);
        } else if (line.contains("❌") || line.contains("FAIL") || line.contains("ERROR")) {
            return colors.colorize("    " + line, AnsiColors.BRIGHT_RED);
        } else if (line.contains("⊝") || line.contains("SKIP")) {
            return colors.colorize("    " + line, AnsiColors.BRIGHT_CYAN);
        } else if (line.contains("🧪")) {
            return colors.colorize(line, AnsiColors.BOLD, AnsiColors.BRIGHT_YELLOW);
        }
        
        return colors.colorize("    " + line, AnsiColors.WHITE);
    }
    
    public String formatErrorLine(String line) {
        return colors.colorize("    → " + line, AnsiColors.RED);
    }
    
    public String formatSummaryHeader() {
        return colors.colorize("\nTest Summary:", AnsiColors.BLUE) + "\n" + 
               colors.colorize("─────────────", AnsiColors.BLUE);
    }
    
    public String formatTestSummary(int total, int passed, int failed, int errors, int skipped, long executionTimeMs) {
        StringBuilder summary = new StringBuilder();
        
        summary.append(colors.colorize("Total: " + total + " tests, ", AnsiColors.WHITE));
        summary.append(colors.colorize(extension.getPassSymbol() + " " + passed + " passed, ", AnsiColors.GREEN));
        summary.append(colors.colorize(extension.getFailSymbol() + " " + failed + " failed, ", AnsiColors.RED));
        summary.append(colors.colorize(extension.getSkipSymbol() + " " + skipped + " skipped", AnsiColors.CYAN));
        
        summary.append("\n");
        summary.append(colors.colorize("Time: ", AnsiColors.WHITE) + (executionTimeMs / 1000.0) + "s");
        
        if (failed == 0 && errors == 0) {
            summary.append("\n\n" + colors.colorize("✨ All tests passed!", AnsiColors.BRIGHT_GREEN));
        } else {
            summary.append("\n\n" + colors.colorize("❌ Some tests failed.", AnsiColors.BRIGHT_RED));
        }
        
        return summary.toString();
    }
    
    public String formatFailureHeader() {
        return colors.colorize("\nFailure Details:", AnsiColors.RED) + "\n" +
               colors.colorize("═══════════════", AnsiColors.RED);
    }
    
    public String formatFailureDetail(String failure) {
        return colors.colorize("    → " + failure, AnsiColors.RED);
    }
    
    public String formatFooter() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        
        printer.println(ps, "");
        printer.printHeading(ps, colors, "=", AnsiColors.BRIGHT_GREEN);
        
        return baos.toString();
    }
    
    public String formatTestResult(String className, String methodName, String status, long duration) {
        StringBuilder outputStr = new StringBuilder();
        
        // Indent
        outputStr.append("    ");
        
        // Class name (white)
        if (className != null) {
            String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
            outputStr.append(colors.colorize(simpleClassName + ".", AnsiColors.WHITE));
        }
        
        // Method name (yellow)
        outputStr.append(colors.colorize(methodName + " ", AnsiColors.YELLOW));
        
        // Calculate dots needed (similar to GradlDromus)
        int nameLength = 4; // indent
        if (className != null) {
            nameLength += className.substring(className.lastIndexOf('.') + 1).length() + 1;
        }
        nameLength += methodName.length() + 1;
        int dotsNeeded = Math.max(1, 76 - nameLength);
        outputStr.append(colors.colorize(".".repeat(dotsNeeded), AnsiColors.BRIGHT_BLACK));
        
        // Status symbol
        String symbol;
        String symbolColor;
        switch (status.toUpperCase()) {
            case "PASS":
            case "SUCCESS":
                symbol = extension.getPassSymbol();
                symbolColor = AnsiColors.BOLD + AnsiColors.BRIGHT_GREEN;
                break;
            case "FAIL":
            case "FAILURE":
                symbol = extension.getFailSymbol();
                symbolColor = AnsiColors.BOLD + AnsiColors.BRIGHT_RED;
                break;
            case "SKIP":
            case "SKIPPED":
                symbol = extension.getSkipSymbol();
                symbolColor = AnsiColors.BOLD + AnsiColors.BRIGHT_CYAN;
                break;
            default:
                symbol = "?";
                symbolColor = AnsiColors.YELLOW;
        }
        
        outputStr.append(colors.colorize(symbol, symbolColor));
        
        // Timing (if enabled)
        if (extension.isShowTimings() && duration > 0) {
            outputStr.append(" ").append(colors.colorize("(" + duration + "ms)", AnsiColors.BRIGHT_BLACK));
        }
        
        return outputStr.toString();
    }
    
    // Getters to access GradlDromus configuration
    public GradlDromusExtension getExtension() {
        return extension;
    }
    
    public AnsiColors getColors() {
        return colors;
    }
    
    public CleanTerminalPrinter getPrinter() {
        return printer;
    }
}
