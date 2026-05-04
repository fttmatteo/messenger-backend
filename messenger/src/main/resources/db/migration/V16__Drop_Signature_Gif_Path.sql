-- ============================================================================
-- Migration: Drop Signature Gif Path
-- Version: V16
-- Description: Removes the gif_path column from signatures table as part of
--              the transition to exclusively static signatures.
-- ============================================================================

ALTER TABLE signatures DROP COLUMN IF EXISTS gif_path;
