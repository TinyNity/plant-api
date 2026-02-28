ALTER TABLE users ADD COLUMN password VARCHAR(255);

-- On met une valeur par défaut pour les utilisateurs existants pour que la contrainte NOT NULL passe
UPDATE users SET password = 'default_password' WHERE password IS NULL;

-- On ajoute la contrainte NOT NULL
ALTER TABLE users ALTER COLUMN password SET NOT NULL;
