# Makefile for SophoDromos Maven Plugin
# Provides convenient commands for development workflow

.PHONY: help build clean test lint format publish-local check-types install deps

# Default target
.DEFAULT_GOAL := help

# Colors for output
BLUE := \033[1;34m
GREEN := \033[1;32m
YELLOW := \033[1;33m
RED := \033[1;31m
RESET := \033[0m

# Maven wrapper or system maven
MVN := $(shell which mvn)
ifeq ($(MVN),)
$(error Maven is not installed or not in PATH)
endif

# Java version check
JAVA_VERSION := $(shell java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1-2)
REQUIRED_JAVA := 17

help: ## Show this help message
	@echo "$(BLUE)SophoDromos Maven Plugin - Available Commands$(RESET)"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "$(GREEN)%-15s$(RESET) %s\n", $$1, $$2}'
	@echo ""
	@echo "$(YELLOW)Prerequisites:$(RESET)"
	@echo "  - Java $(REQUIRED_JAVA)+ (currently using: $(JAVA_VERSION))"
	@echo "  - Maven 3.8+"
	@echo ""

deps: ## Install/update dependencies
	@echo "$(BLUE)Installing dependencies...$(RESET)"
	$(MVN) dependency:resolve
	$(MVN) dependency:resolve-sources
	@echo "$(GREEN)✅ Dependencies installed$(RESET)"

clean: ## Clean build artifacts
	@echo "$(BLUE)Cleaning build artifacts...$(RESET)"
	$(MVN) clean
	@echo "$(GREEN)✅ Clean completed$(RESET)"

compile: ## Compile source code
	@echo "$(BLUE)Compiling source code...$(RESET)"
	$(MVN) compile
	@echo "$(GREEN)✅ Compilation completed$(RESET)"

build: clean compile ## Full build (clean + compile)
	@echo "$(BLUE)Building project...$(RESET)"
	$(MVN) package -DskipTests
	@echo "$(GREEN)✅ Build completed$(RESET)"

test: ## Run all tests
	@echo "$(BLUE)Running tests...$(RESET)"
	$(MVN) test
	@echo "$(GREEN)✅ Tests completed$(RESET)"

test-verbose: ## Run tests with verbose output
	@echo "$(BLUE)Running tests with verbose output...$(RESET)"
	$(MVN) test -X
	@echo "$(GREEN)✅ Verbose tests completed$(RESET)"

test-single: ## Run a single test class (usage: make test-single TEST=TestClassName)
ifndef TEST
	@echo "$(RED)Error: Please specify a test class with TEST=TestClassName$(RESET)"
	@exit 1
endif
	@echo "$(BLUE)Running single test: $(TEST)...$(RESET)"
	$(MVN) test -Dtest=$(TEST)
	@echo "$(GREEN)✅ Single test completed$(RESET)"

lint: ## Run code quality checks (Checkstyle, SpotBugs, PMD)
	@echo "$(BLUE)Running code quality checks...$(RESET)"
	$(MVN) checkstyle:check
	$(MVN) spotbugs:check
	$(MVN) pmd:check
	@echo "$(GREEN)✅ Linting completed$(RESET)"

format: ## Format code with Spotless
	@echo "$(BLUE)Formatting code...$(RESET)"
	$(MVN) spotless:apply
	@echo "$(GREEN)✅ Code formatted$(RESET)"

format-check: ## Check if code is properly formatted
	@echo "$(BLUE)Checking code formatting...$(RESET)"
	$(MVN) spotless:check
	@echo "$(GREEN)✅ Format check completed$(RESET)"

check-types: ## Validate types and compilation
	@echo "$(BLUE)Checking types and compilation...$(RESET)"
	$(MVN) compiler:compile
	$(MVN) compiler:testCompile
	@echo "$(GREEN)✅ Type checking completed$(RESET)"

verify: ## Run full verification (compile, test, lint)
	@echo "$(BLUE)Running full verification...$(RESET)"
	$(MVN) verify
	@echo "$(GREEN)✅ Verification completed$(RESET)"

install: ## Install to local repository (~/.m2)
	@echo "$(BLUE)Installing to local repository...$(RESET)"
	$(MVN) clean install
	@echo "$(GREEN)✅ Installed to ~/.m2/repository$(RESET)"

publish-local: install ## Alias for install (publish to ~/.m2)

package: ## Create JAR package
	@echo "$(BLUE)Creating package...$(RESET)"
	$(MVN) package
	@echo "$(GREEN)✅ Package created$(RESET)"

site: ## Generate project site and reports
	@echo "$(BLUE)Generating project site...$(RESET)"
	$(MVN) site
	@echo "$(GREEN)✅ Site generated in target/site/$(RESET)"

dependency-tree: ## Show dependency tree
	@echo "$(BLUE)Showing dependency tree...$(RESET)"
	$(MVN) dependency:tree

dependency-updates: ## Check for dependency updates
	@echo "$(BLUE)Checking for dependency updates...$(RESET)"
	$(MVN) versions:display-dependency-updates

plugin-updates: ## Check for plugin updates
	@echo "$(BLUE)Checking for plugin updates...$(RESET)"
	$(MVN) versions:display-plugin-updates

security-check: ## Run security vulnerability check
	@echo "$(BLUE)Running security vulnerability check...$(RESET)"
	$(MVN) org.owasp:dependency-check-maven:check
	@echo "$(GREEN)✅ Security check completed$(RESET)"

release-prepare: ## Prepare release (update versions, create tag)
	@echo "$(BLUE)Preparing release...$(RESET)"
	$(MVN) release:prepare
	@echo "$(GREEN)✅ Release prepared$(RESET)"

release-perform: ## Perform release (deploy to repository)
	@echo "$(BLUE)Performing release...$(RESET)"
	$(MVN) release:perform
	@echo "$(GREEN)✅ Release performed$(RESET)"

quick: ## Quick build and test
	@echo "$(BLUE)Running quick build and test...$(RESET)"
	$(MVN) clean compile test -q
	@echo "$(GREEN)✅ Quick build completed$(RESET)"

ci: clean lint check-types test package ## CI pipeline (clean, lint, check-types, test, package)
	@echo "$(GREEN)✅ CI pipeline completed successfully$(RESET)"

dev-setup: deps ## Setup development environment
	@echo "$(BLUE)Setting up development environment...$(RESET)"
	@echo "$(YELLOW)Checking Java version...$(RESET)"
	@java -version
	@echo "$(YELLOW)Checking Maven version...$(RESET)"
	@$(MVN) --version
	@echo "$(GREEN)✅ Development environment ready$(RESET)"

watch: ## Watch for changes and run tests
	@echo "$(BLUE)Watching for changes...$(RESET)"
	@echo "$(YELLOW)Note: This uses fswatch. Install with: brew install fswatch (macOS) or apt-get install fswatch (Linux)$(RESET)"
	@which fswatch > /dev/null || (echo "$(RED)Error: fswatch not found$(RESET)" && exit 1)
	fswatch -o src/ pom.xml | xargs -n1 -I{} make quick

clean-all: clean ## Clean everything including IDE files
	@echo "$(BLUE)Cleaning all files...$(RESET)"
	rm -rf .idea/
	rm -rf *.iml
	rm -rf .vscode/
	rm -rf .classpath
	rm -rf .project
	rm -rf .settings/
	@echo "$(GREEN)✅ Everything cleaned$(RESET)"

info: ## Show project information
	@echo "$(BLUE)Project Information$(RESET)"
	@echo "==================="
	@echo "$(YELLOW)Project:$(RESET) SophoDromos Maven Plugin"
	@echo "$(YELLOW)Java Version:$(RESET) $(JAVA_VERSION)"
	@echo "$(YELLOW)Maven Version:$(RESET) $(shell $(MVN) --version | head -n 1)"
	@echo "$(YELLOW)Target Directory:$(RESET) target/"
	@echo "$(YELLOW)Local Repository:$(RESET) ~/.m2/repository"
	@echo ""
	@$(MVN) help:evaluate -Dexpression=project.groupId -q -DforceStdout | xargs -I {} echo "$(YELLOW)Group ID:$(RESET) {}"
	@$(MVN) help:evaluate -Dexpression=project.artifactId -q -DforceStdout | xargs -I {} echo "$(YELLOW)Artifact ID:$(RESET) {}"
	@$(MVN) help:evaluate -Dexpression=project.version -q -DforceStdout | xargs -I {} echo "$(YELLOW)Version:$(RESET) {}"

# Development shortcuts
dev: quick ## Alias for quick
all: ci ## Alias for ci
check: verify ## Alias for verify