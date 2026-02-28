ALTER TABLE users ADD COLUMN password varchar(255);
UPDATE users SET password = 'placeholder_password' WHERE password IS NULL;
ALTER TABLE users ALTER COLUMN password SET NOT NULL;
