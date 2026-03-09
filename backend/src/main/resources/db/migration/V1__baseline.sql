-- Baseline schema. Intentionally thin to keep Phase 1 lean.
CREATE TABLE IF NOT EXISTS schema_changelog_placeholder (
    id SERIAL PRIMARY KEY,
    comment TEXT
);
