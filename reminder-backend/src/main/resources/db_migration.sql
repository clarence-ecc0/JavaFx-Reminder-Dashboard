-- SQL Migration: Add user_id to reminders table
-- This will be run by Hibernate automatically with ddl-auto=update

-- Note: If reminders table already exists, manually run:
-- ALTER TABLE reminders ADD COLUMN user_id BIGINT NOT NULL DEFAULT 1;
-- ALTER TABLE reminders ADD CONSTRAINT fk_reminder_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- For fresh installs, Hibernate will create the table with user_id automatically
