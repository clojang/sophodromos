package com.sophodromos.plugin;

import org.fusesource.jansi.Ansi;

/**
 * Formats test output with colors and custom styling
 */
public class TestOutputFormatter {
    
    private final boolean colorOutput;
    private final boolean detailedFailures;
    
    public TestOutputFormatter(boolean colorOutput, boolean detailedFailures) {
        this.colorOutput = colorOutput;
        this.detailedFailures = detailedFailures;
    }
    
    public String formatHeader(String title) {
        if (!colorOutput) {
            return "=== " + title + " ===";
        }
        
        // TODO: Customize header formatting here
        return Ansi.ansi()
            .fg(Ansi.Color.CYAN)
            .a("╔══════════════════════════════════════════════════════════════════════╗")
            .newline()
            .a("║")
            .fg(Ansi.Color.YELLOW)
            .bold()
            .a("  " + centerText(title, 66) + "  ")
            .boldOff()
            .fg(Ansi.Color.CYAN)
            .a("║")
            .newline()
            .a("╚══════════════════════════════════════════════════════════════════════╝")
            .reset()
            .toString();
    }
    
    public String formatProgressLine(String line) {
        if (!colorOutput) {
            return line;
        }
        
        // TODO: Transform progress output here to change its appearance
        if (line.contains("PASS") || line.contains("OK")) {
            return Ansi.ansi().fg(Ansi.Color.GREEN).a("✓ ").reset().a(line).toString();
        } else if (line.contains("FAIL") || line.contains("ERROR")) {
            return Ansi.ansi().fg(Ansi.Color.RED).a("✗ ").reset().a(line).toString();
        } else if (line.contains("SKIP")) {
            return Ansi.ansi().fg(Ansi.Color.YELLOW).a("⊝ ").reset().a(line).toString();
        }
        
        return Ansi.ansi().fg(Ansi.Color.BLUE).a("→ ").reset().a(line).toString();
    }
    
    public String formatErrorLine(String line) {
        if (!colorOutput) {
            return line;
        }
        
        // TODO: Transform error output here to change its appearance
        return Ansi.ansi()
            .fg(Ansi.Color.RED)
            .bold()
            .a("ERROR: ")
            .boldOff()
            .a(line)
            .reset()
            .toString();
    }
    
    public String formatSummaryHeader() {
        if (!colorOutput) {
            return "=== Test Results ===";
        }
        
        // TODO: Customize summary header here
        return Ansi.ansi()
            .fg(Ansi.Color.MAGENTA)
            .bold()
            .a("┌─────────────────────────────────────────────────────────────┐")
            .newline()
            .a("│  ")
            .fg(Ansi.Color.WHITE)
            .a("TEST EXECUTION SUMMARY")
            .fg(Ansi.Color.MAGENTA)
            .a(String.format("%" + (37) + "s", ""))
            .a("  │")
            .newline()
            .a("└─────────────────────────────────────────────────────────────┘")
            .reset()
            .toString();
    }
    
    public String formatTestSummary(int total, int passed, int failed, int errors, int skipped, long executionTimeMs) {
        StringBuilder summary = new StringBuilder();
        
        if (colorOutput) {
            // TODO: Customize test summary formatting here
            summary.append(Ansi.ansi().fg(Ansi.Color.WHITE).bold().a("Tests run: ").boldOff());
            summary.append(Ansi.ansi().fg(Ansi.Color.CYAN).a(total).reset());
            
            if (passed > 0) {
                summary.append(Ansi.ansi().a(", ").fg(Ansi.Color.GREEN).a("Passed: ").a(passed).reset());
            }
            
            if (failed > 0) {
                summary.append(Ansi.ansi().a(", ").fg(Ansi.Color.RED).bold().a("Failed: ").a(failed).boldOff().reset());
            }
            
            if (errors > 0) {
                summary.append(Ansi.ansi().a(", ").fg(Ansi.Color.RED).bold().a("Errors: ").a(errors).boldOff().reset());
            }
            
            if (skipped > 0) {
                summary.append(Ansi.ansi().a(", ").fg(Ansi.Color.YELLOW).a("Skipped: ").a(skipped).reset());
            }
            
            summary.append(Ansi.ansi().a(" - Time: ").fg(Ansi.Color.CYAN).a(formatTime(executionTimeMs)).reset());
        } else {
            summary.append(String.format("Tests run: %d, Passed: %d, Failed: %d, Errors: %d, Skipped: %d - Time: %s",
                total, passed, failed, errors, skipped, formatTime(executionTimeMs)));
        }
        
        return summary.toString();
    }
    
    public String formatFailureHeader() {
        if (!colorOutput) {
            return "=== Failures ===";
        }
        
        // TODO: Customize failure header here
        return Ansi.ansi()
            .fg(Ansi.Color.RED)
            .bold()
            .a("╔═══════════════════════════════════════════════════════════╗")
            .newline()
            .a("║  ")
            .fg(Ansi.Color.WHITE)
            .a("FAILURE DETAILS")
            .fg(Ansi.Color.RED)
            .a(String.format("%" + (45) + "s", ""))
            .a("  ║")
            .newline()
            .a("╚═══════════════════════════════════════════════════════════╝")
            .reset()
            .toString();
    }
    
    public String formatFailureDetail(String failure) {
        if (!colorOutput) {
            return failure;
        }
        
        // TODO: Transform failure details here to change their format
        return Ansi.ansi()
            .fg(Ansi.Color.RED)
            .a("▶ ")
            .fg(Ansi.Color.WHITE)
            .a(failure)
            .reset()
            .toString();
    }
    
    public String formatFooter() {
        if (!colorOutput) {
            return "=== End ===";
        }
        
        // TODO: Customize footer here
        return Ansi.ansi()
            .fg(Ansi.Color.CYAN)
            .a("═".repeat(70))
            .reset()
            .toString();
    }
    
    private String centerText(String text, int width) {
        int padding = (width - text.length()) / 2;
        return " ".repeat(Math.max(0, padding)) + text + " ".repeat(Math.max(0, width - text.length() - padding));
    }
    
    private String formatTime(long milliseconds) {
        if (milliseconds < 1000) {
            return milliseconds + "ms";
        } else if (milliseconds < 60000) {
            return String.format("%.2fs", milliseconds / 1000.0);
        } else {
            long minutes = milliseconds / 60000;
            long seconds = (milliseconds % 60000) / 1000;
            return String.format("%dm %ds", minutes, seconds);
        }
    }
}
