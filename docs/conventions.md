# Team Conventions

This document captures the working agreements for the monorepo.

## Branching Strategy
- Default to trunk-based commits on `main` with short-lived feature branches
- Release tags follow `vYYYY.MM.DD` format
- Hotfixes should be tagged and merged back to main promptly

## Code Style
- Prefer spaces over tabs with a two-space indent for JavaScript/TypeScript
- Java code uses four-space indent
- Java code should target Java 22 language features but avoid preview APIs
- TypeScript should use explicit return types except when type inference is clear
- Legacy modules may have different formatting standards

## Tooling Expectations
- Run `./mvnw verify` before opening PRs to ensure the workspace compiles
- Frontend contributors should run `npm run lint` and `npm test` before committing
- Backend teams use IntelliJ inspections
- Frontend teams use ESLint/TypeScript diagnostics

## Pull Requests
- Require at least one review
- Link PRs to the corresponding backlog item when applicable
- Squash commits for cleaner history on main
- Include tests for new features and bug fixes

## Documentation
- Update `docs/runbook.md` whenever tooling or infrastructure changes
- Keep documentation up-to-date with implementation
- Use inline comments for complex business logic
- Maintain API documentation for public endpoints

## Testing
- Unit tests are required for business logic
- Integration tests for API endpoints
- Frontend components should have corresponding tests
- Maintain test coverage above 70%
