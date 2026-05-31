-- =====================================================================
-- Migration: Add IN_PROGRESS to upload_sessions.status
-- =====================================================================
-- Background:
--   UploadStatus previously only had PENDING / COMPLETED / FAILED.
--   PENDING semantically covered both "session initialised, no data yet"
--   and "upload underway", which made it impossible to distinguish the two.
--
--   A new IN_PROGRESS state is introduced:
--     PENDING      → session created, staging file allocated, 0 bytes received
--     IN_PROGRESS  → at least one chunk written successfully
--     COMPLETED    → file committed to permanent storage
--     FAILED       → session expired or unrecoverable error
--
--   Java stores enum values as VARCHAR strings (@Enumerated(EnumType.STRING)).
--   MySQL column is already VARCHAR(16), which fits "IN_PROGRESS" (11 chars).
--   No DDL change is required; only data is migrated.
-- =====================================================================

USE helloworld;

-- Confirm the column is wide enough for IN_PROGRESS (11 chars < 16).
-- This is a no-op DDL that makes the intent explicit and serves as a safety net.
ALTER TABLE upload_sessions
    MODIFY COLUMN status VARCHAR(16) NOT NULL;

-- Migrate existing PENDING rows that already have data written:
-- received_bytes > 0 means at least one chunk arrived, so the correct
-- state is IN_PROGRESS, not PENDING.
UPDATE upload_sessions
   SET status = 'IN_PROGRESS'
 WHERE status  = 'PENDING'
   AND received_bytes > 0;

SELECT CONCAT(
    'Migration complete. Rows updated to IN_PROGRESS: ',
    ROW_COUNT()
) AS Status;
