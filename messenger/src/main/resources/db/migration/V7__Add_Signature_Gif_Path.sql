-- Add gif_path column to signatures table for storing signature capture GIFs
ALTER TABLE signatures ADD COLUMN gif_path VARCHAR(2048) NULL;
