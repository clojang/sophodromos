package io.github.clojang.sophodromos;

import java.util.Map;

/**
 * Formats the final summary output for multi-module builds. Provides a consolidated view of all
 * test results across modules.
 */
@SuppressWarnings({
  "PMD.LawOfDemeter",
  "PMD.AppendCharacterWithChar",
  "PMD.ConsecutiveLiteralAppends",
  "PMD.ConsecutiveAppendsShouldReuse",
  "PMD.InsufficientStringBufferDeclaration"
})
class MultiModuleSummaryFormatter {

  private static final String SEPARATOR =
      "================================================================================";
  private static final String HEADER_LINE =
      "--------------------------------------------------------------------------------";

  /** Default constructor. */
  protected MultiModuleSummaryFormatter() {
    // Default constructor for utility class
  }

  /**
   * Formats the final summary for a multi-module build.
   *
   * @param state the aggregated state from all modules
   * @return formatted summary string
   */
  protected String formatFinalSummary(final MultiModuleStateManager.MultiModuleState state) {
    final StringBuilder summary = new StringBuilder(1024);

    // Header
    summary
        .append('\n')
        .append(SEPARATOR)
        .append('\n')
        .append("SophoDromos Multi-Module Test Summary")
        .append('\n')
        .append(HEADER_LINE)
        .append("\n\n");

    // Module-by-module results
    final Map<String, MultiModuleStateManager.ModuleTestResults> moduleResults =
        state.getModuleResults();

    if (moduleResults.isEmpty()) {
      summary.append("No test results found.\n\n");
    } else {
      for (final MultiModuleStateManager.ModuleTestResults result : moduleResults.values()) {
        formatModuleResult(summary, result);
      }
    }

    // Overall summary
    formatOverallSummary(summary, state);

    summary.append(SEPARATOR).append('\n');

    return summary.toString();
  }

  private void formatModuleResult(
      final StringBuilder summary, final MultiModuleStateManager.ModuleTestResults result) {
    final String moduleId = result.getModuleId();
    final String moduleName = extractModuleName(moduleId);

    summary.append(moduleName).append('\n');

    // Show test output if available
    if (!result.getTestOutput().isEmpty()) {
      for (final String line : result.getTestOutput()) {
        summary.append("  ").append(line).append('\n');
      }
    }

    // Module summary
    summary.append('\n');
    formatModuleSummary(summary, result);
    summary.append('\n');
  }

  private void formatModuleSummary(
      final StringBuilder summary, final MultiModuleStateManager.ModuleTestResults result) {
    summary.append("Module Summary:\n─────────────\n");
    summary.append(String.format("Total: %d tests, ", result.getTestsRun()));

    if (result.getTestsPassed() > 0) {
      summary.append(String.format("💚 %d passed", result.getTestsPassed()));
    }

    if (result.getTestsFailed() > 0) {
      if (result.getTestsPassed() > 0) {
        summary.append(", ");
      }
      summary.append(String.format("💔 %d failed", result.getTestsFailed()));
    }

    if (result.getTestsSkipped() > 0) {
      if (result.getTestsPassed() > 0 || result.getTestsFailed() > 0) {
        summary.append(", ");
      }
      summary.append(String.format("💤 %d skipped", result.getTestsSkipped()));
    }

    summary.append('\n');
    summary.append(String.format("Time: %.1fs%n", result.getTimeElapsed()));

    // Module status
    if (result.getTestsFailed() > 0) {
      summary.append("❌ Module had test failures\n");
    } else if (result.getTestsRun() > 0) {
      summary.append("✨ All module tests passed!\n");
    } else {
      summary.append("ℹ️  No tests run in this module\n");
    }
  }

  private void formatOverallSummary(
      final StringBuilder summary, final MultiModuleStateManager.MultiModuleState state) {
    summary.append("Overall Test Summary:\n═══════════════════\n");
    summary.append(
        String.format(
            "Total: %d tests across %d modules, ",
            state.getTotalTestsRun(), state.getModuleResults().size()));

    if (state.getTotalTestsPassed() > 0) {
      summary.append(String.format("💚 %d passed", state.getTotalTestsPassed()));
    }

    if (state.getTotalTestsFailed() > 0) {
      if (state.getTotalTestsPassed() > 0) {
        summary.append(", ");
      }
      summary.append(String.format("💔 %d failed", state.getTotalTestsFailed()));
    }

    if (state.getTotalTestsSkipped() > 0) {
      if (state.getTotalTestsPassed() > 0 || state.getTotalTestsFailed() > 0) {
        summary.append(", ");
      }
      summary.append(String.format("💤 %d skipped", state.getTotalTestsSkipped()));
    }

    summary.append('\n');
    summary.append(String.format("Total Time: %.1fs%n%n", state.getTotalTimeElapsed()));

    // Overall status
    if (state.getTotalTestsFailed() > 0) {
      summary.append("❌ BUILD HAD TEST FAILURES\n\n");
    } else if (state.getTotalTestsRun() > 0) {
      summary.append("✨ ALL TESTS PASSED!\n\n");
    } else {
      summary.append("ℹ️  NO TESTS WERE RUN\n\n");
    }
  }

  private String extractModuleName(final String moduleId) {
    // Extract just the artifactId from "groupId:artifactId"
    final int colonIndex = moduleId.lastIndexOf(':');
    return colonIndex >= 0 ? moduleId.substring(colonIndex + 1) : moduleId;
  }
}
