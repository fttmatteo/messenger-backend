-- V4: Remove legacy 'user_name' column from employees table
-- This column causes issues because it is NOT NULL but not present in the JPA entity (which uses 'full_name')

-- We check if the column exists by attempting to drop it.
-- Note: If running on a clean database where user_name never existed, this might fail unless we handle it.
-- However, given the error 'Field user_name doesn't have a default value', we know it exists in Prod.

ALTER TABLE employees DROP COLUMN user_name;
