# AGENTS.md — Agent Guidelines & Architecture Reference

This document serves as the guide for AI agents working on `quronest_backend`. It documents the system architecture, conventions, and engineering constraints. Update this document as design patterns evolve.

---

## 1. Project Overview

`quronest_backend` is a Spring Boot application (Java 25, Spring Boot 4.x) that powers the Quronest personalized learning platform.

### Core Capabilities
- **User Journey & Profiles**: Manages user learning goals, academic/personal background, and dynamic journey tracking.
- **Daily Plans & Tasks**: Generates, schedules, and tracks daily learning plans consisting of various task types (e.g., `READING`, `QUIZ`, `NOTE`).
- **Async LLM Pipeline**: Uses RabbitMQ for producing and consuming asynchronous LLM tasks (plan generation, task content generation, evaluation), with real-time WebSocket notifications sent to clients upon completion.
- **Persistence & Migrations**: PostgreSQL with JSONB columns for flexible task contents and performance logs, managed via Liquibase migrations.

---

## 2. Core Architectural & Code Conventions

- **Layered Architecture**: Strictly adhere to `Controller` → `Service` → `Repository` flow. Keep controllers thin and business logic inside domain-specific services.
- **Model Hierarchy**:
  - `model/table`: JPA entities mapped to database tables (e.g., `DailyTask`, `DailyTaskLog`, `User`).
  - `model`: JSONB payload models and value objects (e.g., `ReadingTaskContent`, `QuizTaskContent`, `QuizQuestion`).
  - `model/enums`: Domain enumerations (e.g., `DailyTaskType`, `DailyTaskStatus`).
- **DTOs**: Keep requests, responses, and LLM event payloads in `dto` packages with explicit Jackson property mappings.
- **Transactions & Concurrency**: Ensure repository and state mutations are consistent, handling null-safety across legacy and newly generated JSON payloads.

---

## 3. Engineering Guidelines for Agents

### 1. Do Not Write Unnecessary Code
- Implement only what is directly required by the user prompt or task specification.
- Avoid speculative features, premature abstractions, or utility methods that are not actively used.

### 2. Do Not Create Unnecessary Aliases
- Never create duplicate convenience methods, redundant wrapper aliases, or multiple routing paths unless explicitly instructed.
- Keep the public API surface area clean, focused, and intentional.

### 3. Maintain Proper Modular Structure & Low-Level Design (LLD)
- Keep classes focused on a single responsibility (SRP).
- Re-use shared domain logic (e.g., quiz/MCQ verification, scoring calculations) cleanly through service delegation rather than code duplication.
- Ensure loose coupling and high cohesion across packages.

### 4. Consult Before Making Major or Structural Decisions
- Always ask the user for confirmation before:
  - Making multi-level architecture changes or introducing new patterns.
  - Modifying database schemas, Liquibase change logs, or core entity relationships.
  - Refactoring established cross-cutting layers (e.g., security, messaging pipelines).

### 5. No Unnecessary or AI-Style Comments
- **Self-Documenting Code**: Code must be readable through clean variable, method, and class naming.
- **Do Not Add**:
  - Redundant step-by-step narration (e.g., `// 1. Fetch user`, `// 2. Set status`, `// Return response`).
  - Conversational, boilerplate, or obvious comments that repeat what the code already expresses.
- Reserve comments strictly for non-obvious business logic, domain invariants, or edge-case explanations.

### 6. Maintain High Code Quality & Green Tests
- Every change should be backed by clean, focused unit and integration tests.
- Verify test suites and builds before completing tasks (`./mvnw test`).
- Maintain existing coding conventions and style across all files.
