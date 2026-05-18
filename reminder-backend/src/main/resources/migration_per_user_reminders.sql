-- Database Setup and Migration for Per-User Reminders
-- Run these commands in MySQL before testing the updated application

USE reminder_db;

-- 1. Check current state
SELECT 'Current Users:' as info;
SELECT * FROM users;

SELECT 'Current Reminders (before cleanup):' as info;
SELECT * FROM reminders;

-- 2. IMPORTANT: If reminders table already exists and has no user_id column, add it:
-- Uncomment and run these if needed:
-- ALTER TABLE reminders ADD COLUMN user_id BIGINT;
-- UPDATE reminders SET user_id = 1 WHERE user_id IS NULL;
-- ALTER TABLE reminders MODIFY COLUMN user_id BIGINT NOT NULL;
-- ALTER TABLE reminders ADD CONSTRAINT fk_reminder_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- 3. Clear old test reminders (optional - only if you want fresh start)
-- DELETE FROM reminders;

-- 4. Verify final structure
SELECT 'Final Reminders table structure:' as info;
DESCRIBE reminders;

SELECT 'Users in database:' as info;
SELECT id, username, full_name, created_at FROM users;

SELECT 'Reminders per user:' as info;
SELECT u.id, u.username, COUNT(r.id) as reminder_count
FROM users u
LEFT JOIN reminders r ON u.id = r.user_id
GROUP BY u.id, u.username;
